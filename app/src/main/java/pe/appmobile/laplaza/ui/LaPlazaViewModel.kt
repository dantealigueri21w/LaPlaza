package pe.appmobile.laplaza.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.appmobile.laplaza.audio.ResultadoEnVivo
import pe.appmobile.laplaza.data.local.entity.BloqueContenidoEntity
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.data.local.entity.TemaEntity
import pe.appmobile.laplaza.data.repository.LaPlazaRepository
import pe.appmobile.laplaza.domain.engine.MotorPregon
import pe.appmobile.laplaza.domain.engine.MotorPuntajeAudiencia
import pe.appmobile.laplaza.domain.model.DatosIntento
import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.Pregon
import pe.appmobile.laplaza.domain.model.SugerenciaRepaso

/**
 * El unico ViewModel de esta tarea (el "shell": navegacion, perfil, home, ajustes). No
 * expone nada del motor de audio ni de la mecanica de un rincon real -- eso es de una
 * tarea posterior, sobre la pantalla de declamacion.
 *
 * Construccion manual (sin Hilt/Koin, ver [LaPlazaViewModelFactory]): quien arma la app
 * (MainActivity, a traves de LaPlazaApplication) pasa el [LaPlazaRepository] ya construido.
 */
class LaPlazaViewModel(private val repositorio: LaPlazaRepository) : ViewModel() {

    /**
     * true recien despues de la PRIMERA lectura real de `perfil` en Room. Se necesita
     * porque un StateFlow con valor inicial `null` no distingue "todavia no se leyo la
     * base de datos" de "se leyo y no hay perfil creado" -- y la decision de a que
     * pantalla ir al abrir la app (crear perfil vs. home) depende de saber cual de los
     * dos casos es real, no del valor inicial de relleno.
     */
    private val _perfilListo = MutableStateFlow(false)

    /** El perfil actual, o null mientras no se haya creado ninguno (primer arranque). */
    val perfil: StateFlow<PerfilEntity?> = repositorio.obtenerPerfil()
        .onEach { _perfilListo.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Eagerly (no WhileSubscribed): estos dos son datos chicos y locales que la app
    // necesita apenas arranca (el mapa de la plaza los muestra de inmediato en Home), y
    // WhileSubscribed solo empieza a coleccionar cuando alguien ya esta suscrito -- eso
    // deja el StateFlow pegado en su valor inicial vacio si algo (o una prueba) lee
    // `.value` antes de que la UI misma dispare la primera suscripcion.
    val rincones: StateFlow<List<RinconEntity>> = repositorio.obtenerRincones()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val racha: StateFlow<RachaEntity?> = repositorio.obtenerRacha()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _semillaLista = MutableStateFlow(false)

    /**
     * true cuando ya se sabe, con certeza, si hay o no un perfil creado (para decidir la
     * pantalla inicial de la navegacion) Y ya se sembraron los 7 rincones (para que el
     * mapa de la plaza no aparezca vacio un instante en el primer arranque). El NavHost
     * debe esperar a este flag antes de construir el grafo de navegacion.
     */
    val listoParaNavegar: StateFlow<Boolean> = combine(_perfilListo, _semillaLista) { perfilListo, semillaLista ->
        perfilListo && semillaLista
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            repositorio.sembrarSiEsNecesario()
            _semillaLista.value = true
        }
    }

    suspend fun crearPerfil(alias: String, avatarId: Int): Long =
        repositorio.crearPerfil(alias = alias, avatarId = avatarId)

    suspend fun actualizarPerfil(perfil: PerfilEntity) = repositorio.actualizarPerfil(perfil)

    // ---------- Rincon / temas / armado del discurso ----------
    //
    // A diferencia de perfil/rincones/racha, estas no son StateFlow: se cargan una sola
    // vez por visita a la pantalla (TemasDeRinconScreen o ArmarDiscursoScreen), no se
    // observan de forma continua. El NavHost las llama dentro de un LaunchedEffect y
    // guarda el resultado en un remember local (ver LaPlazaNavHost.kt).

    /** Los temas de UN rincon real (no sirve para Rincon Libre, ver [todosLosTemas]). */
    suspend fun temasDe(rinconId: String): List<TemaEntity> =
        repositorio.obtenerTemasDe(rinconId).first()

    /** Los 21 temas de los 7 rincones juntos, para Rincon Libre. */
    suspend fun todosLosTemas(): List<TemaEntity> =
        repositorio.obtenerTodosLosTemas().first()

    suspend fun tema(id: Long): TemaEntity? = repositorio.obtenerTema(id)

    suspend fun bloquesDe(temaId: Long): List<BloqueContenidoEntity> =
        repositorio.obtenerBloquesDe(temaId)

    /** La sugerencia de repaso real de Chirri (nunca inventada), solo relevante en
     * Rincon Libre -- ver ficha 24-LA-PLAZA.md, seccion "Repaso". */
    suspend fun sugerenciaRepaso(): SugerenciaRepaso? = repositorio.sugerirRepaso()

    // ---------- Declamacion (microfono real, indicador en vivo, pregon) ----------

    /**
     * Traspaso de un solo uso entre ArmarDiscursoScreen y DeclamacionScreen, NO un
     * StateFlow a proposito: el discurso armado no es un dato que ninguna pantalla
     * observe de forma continua, es un valor que viaja una sola vez, en el instante
     * exacto de navegar de una pantalla a la otra. Modelarlo como StateFlow implicaria
     * que cualquier futuro colector (por ejemplo, tras una recomposicion o una
     * reconfiguracion) volveria a recibir el mismo discurso ya consumido, lo cual no
     * tiene sentido para un dato que se usa una unica vez y se descarta. La ruta de
     * navegacion solo lleva el [DiscursoArmado.temaId] (un Long, lo unico que Navigation
     * Compose puede llevar como argumento); la lista real de bloques elegidos viaja por
     * aqui.
     */
    private var discursoPendiente: DiscursoArmado? = null

    /** Llamado por el NavHost justo antes de navegar a [pe.appmobile.laplaza.ui.navigation.Rutas.DECLAMACION]. */
    fun prepararDeclamacion(discurso: DiscursoArmado) {
        discursoPendiente = discurso
    }

    /** Consume (y limpia) el discurso preparado por [prepararDeclamacion]. La pantalla de
     * declamacion lo lee una sola vez, desde un `LaunchedEffect(Unit)`. Devuelve null si
     * se abrio esta pantalla sin pasar por [prepararDeclamacion] antes (por ejemplo, el
     * proceso se recreo entre medio) -- ese caso lo maneja la propia pantalla, no aqui. */
    fun tomarDiscursoPendiente(): DiscursoArmado? {
        val discurso = discursoPendiente
        discursoPendiente = null
        return discurso
    }

    /** true si ya existe al menos un intento previo real de este tema -- la definicion
     * de "repaso" que usa [finalizarDeclamacion]: no es una casilla que el nino marque,
     * es un hecho que ya esta en la base de datos. */
    suspend fun esRepasoDe(temaId: Long): Boolean =
        repositorio.obtenerIntentosRecientesDe(temaId, limite = 1).isNotEmpty()

    /**
     * Orquesta el cierre real de una declamacion: de las 4 variables en vivo de
     * [resultado] deriva los 4 sub-puntajes 0..1 que espera Room (via
     * [MotorPuntajeAudiencia], la misma formula que ya usa el compuesto -- ver el
     * comentario de esa clase), registra el intento, genera el pregon real con
     * [MotorPregon] (nunca inventado en la UI), lo guarda en el Cuaderno de Pregones, y
     * marca el rincon del tema como completado.
     *
     * Decision de diseno: el rincon se marca completado en CUALQUIER declamacion
     * terminada con exito (no solo cuando los 3 temas del rincon ya se declamaron). Es
     * la lectura mas simple de la ficha que sigue atada a una accion real del nino -- la
     * ficha no fija de forma explicita cual de las dos lecturas es la correcta.
     */
    suspend fun finalizarDeclamacion(
        discurso: DiscursoArmado,
        resultado: ResultadoEnVivo,
        nombreTema: String,
        nombreRincon: String,
        esRepaso: Boolean
    ): Pregon {
        val puntajeVolumen = MotorPuntajeAudiencia.calcularPuntajeVolumen(resultado.acustico.volumenPromedio)
        val puntajeEntonacion = MotorPuntajeAudiencia.calcularPuntajeEntonacion(resultado.acustico.variacionEntonacionSemitonos)
        val puntajeRitmo = MotorPuntajeAudiencia.calcularPuntajeRitmo(resultado.acustico.ritmoSilabasPorMinuto)
        val puntajeFluidez = resultado.fluidez

        val intentoId = repositorio.registrarIntento(
            temaId = discurso.temaId,
            esRepaso = esRepaso,
            puntajeVolumen = puntajeVolumen,
            puntajeEntonacion = puntajeEntonacion,
            puntajeRitmo = puntajeRitmo,
            puntajeFluidez = puntajeFluidez,
            puntajeCompuesto = resultado.puntaje.puntajeCompuesto
        )

        val pregon = MotorPregon.generar(
            DatosIntento(
                nombreTema = nombreTema,
                nombreRincon = nombreRincon,
                puntajeVolumen = puntajeVolumen,
                puntajeEntonacion = puntajeEntonacion,
                puntajeRitmo = puntajeRitmo,
                puntajeFluidez = puntajeFluidez
            )
        )
        repositorio.guardarPregon(intentoId, pregon)

        val rinconIdDeEseTema = repositorio.obtenerTema(discurso.temaId)?.rinconId
        if (rinconIdDeEseTema != null) {
            repositorio.marcarRinconCompletado(rinconIdDeEseTema)
        }

        return pregon
    }
}
