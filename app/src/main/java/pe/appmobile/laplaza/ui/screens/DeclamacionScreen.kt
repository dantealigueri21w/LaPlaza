package pe.appmobile.laplaza.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.audio.CapturadorVoz
import pe.appmobile.laplaza.audio.EstadoCapturaEnVivo
import pe.appmobile.laplaza.audio.FuenteDeAudioReal
import pe.appmobile.laplaza.audio.ResultadoEnVivo
import pe.appmobile.laplaza.domain.engine.MotorPuntajeAudiencia
import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.NivelAtencion
import pe.appmobile.laplaza.domain.model.Pregon
import pe.appmobile.laplaza.domain.model.ResultadoAcustico
import pe.appmobile.laplaza.ui.LaPlazaViewModel
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.Chirri
import pe.appmobile.laplaza.ui.components.EstadoChirri
import pe.appmobile.laplaza.ui.components.PanelDePlaza
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo
import pe.appmobile.laplaza.ui.theme.RosaBerenjena
import pe.appmobile.laplaza.ui.theme.VioletaAtardecer

/**
 * Estado real del permiso de microfono, tal como lo necesita esta pantalla -- no el
 * `PackageManager.PERMISSION_GRANTED/DENIED` crudo de Android, que no distingue "todavia
 * no se pidio" de "el usuario lo nego". Esa tercera opcion ([NoSolicitado]) es la que
 * decide si tocar "Hablar" dispara el dialogo del sistema o arranca la captura
 * directamente.
 */
sealed interface EstadoPermisoMicrofono {
    data object NoSolicitado : EstadoPermisoMicrofono
    data object Concedido : EstadoPermisoMicrofono
    data object Denegado : EstadoPermisoMicrofono
}

/** Cuanto dura sostener el circulo de ensayo para llegar a la energia maxima sintetica.
 * Ver [sintetizarResultadoEnsayo]: 5 s es un sostenido largo pero razonable para un
 * nino, ni instantaneo ni agotador. */
private const val DURACION_ENSAYO_COMPLETO_MS = 5_000L

/**
 * La pantalla de declamacion real: el nino lee en voz alta el discurso que armo, un
 * indicador en vivo (NO Chirri -- ver el comentario de [pe.appmobile.laplaza.ui.components.Chirri],
 * la plaza es la protagonista mientras habla) reacciona a su voz, y al terminar Chirri
 * reacciona y se guarda un pregon real en el Cuaderno de Pregones.
 *
 * Envoltura con estado (Context, ActivityResultLauncher, AudioRecord real a traves de
 * [CapturadorVoz]) alrededor de [ContenidoDeclamacion], la parte pura y probable con
 * JUnit/Robolectric sin tocar hardware real -- el mismo patron pantalla-con-estado /
 * contenido-puro que el resto de esta app.
 */
@Composable
fun DeclamacionScreen(
    viewModel: LaPlazaViewModel,
    temaId: Long,
    onContinuar: () -> Unit,
    viaRinconLibre: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var discurso by remember(temaId) { mutableStateOf<DiscursoArmado?>(null) }
    var nombreTema by remember(temaId) { mutableStateOf("") }
    var nombreRincon by remember(temaId) { mutableStateOf("") }
    var esRepaso by remember(temaId) { mutableStateOf(false) }

    // LaunchedEffect(Unit), no LaunchedEffect(temaId): el discurso pendiente se consume
    // UNA sola vez por entrada real a esta pantalla (ver LaPlazaViewModel.tomarDiscursoPendiente),
    // nunca de nuevo si algo mas recompone con el mismo temaId.
    LaunchedEffect(Unit) {
        discurso = viewModel.tomarDiscursoPendiente()
        val tema = viewModel.tema(temaId)
        nombreTema = tema?.titulo.orEmpty()
        nombreRincon = tema?.let { t -> viewModel.rincones.value.find { it.id == t.rinconId }?.nombre }.orEmpty()
        esRepaso = viewModel.esRepasoDe(temaId)
    }

    var estadoPermiso by remember {
        mutableStateOf(
            if (permisoMicrofonoConcedido(context)) {
                EstadoPermisoMicrofono.Concedido
            } else {
                EstadoPermisoMicrofono.NoSolicitado
            }
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        estadoPermiso = if (concedido) EstadoPermisoMicrofono.Concedido else EstadoPermisoMicrofono.Denegado
    }

    // Se reconstruye solo cuando cambia estadoPermiso (no en cada recomposicion): abrir
    // el microfono es un efecto costoso y con estado real (AudioRecord), no algo para
    // repetir porque si. FuenteDeAudioReal.crear ya devuelve null (nunca lanza) tanto si
    // falta el permiso como si el hardware no pudo inicializar -- en cualquiera de los
    // dos casos capturador queda null y la UI cae sola al modo de ensayo silencioso.
    val fuente = remember(estadoPermiso) {
        if (estadoPermiso == EstadoPermisoMicrofono.Concedido) FuenteDeAudioReal.crear(context) else null
    }
    val capturador = remember(fuente) { fuente?.let { CapturadorVoz(it, scope) } }

    // El microfono nunca debe quedar abierto: ni al salir de esta pantalla, ni al
    // reconstruirse capturador porque el permiso cambio de estado.
    DisposableEffect(capturador) {
        onDispose { capturador?.detener() }
    }

    val estadoVacio = remember { MutableStateFlow<EstadoCapturaEnVivo>(EstadoCapturaEnVivo.SinIniciar) }
    val estadoCapturaReal by (capturador?.estado ?: estadoVacio).collectAsState()

    var estadoEnsayo by remember { mutableStateOf<EstadoCapturaEnVivo>(EstadoCapturaEnVivo.SinIniciar) }
    var jobEnsayo by remember { mutableStateOf<Job?>(null) }

    // Se usa el ensayo silencioso siempre que NO haya un capturador real utilizable --
    // ya sea porque el permiso se nego, o porque FuenteDeAudioReal.crear fallo pese a
    // tener el permiso (hardware ocupado o sin inicializar). Ver el comentario de
    // FuenteDeAudioReal.crear: es a proposito el mismo gancho para los dos casos.
    val usaEnsayo = capturador == null
    val estadoCaptura = if (usaEnsayo) estadoEnsayo else estadoCapturaReal

    // Lo que ContenidoDeclamacion realmente necesita para elegir entre el boton
    // "Hablar" y el circulo de ensayo: si hay que caer al ensayo (por lo que sea), se ve
    // igual que un permiso denegado, aunque el permiso de Android en si siga concedido.
    val estadoPermisoEfectivo = if (estadoPermiso == EstadoPermisoMicrofono.Concedido && usaEnsayo) {
        EstadoPermisoMicrofono.Denegado
    } else {
        estadoPermiso
    }

    var pregonFinal by remember(temaId) { mutableStateOf<Pregon?>(null) }

    // Cuando la captura real (microfono) termina, orquesta el cierre real: puntajes,
    // intento, pregon, rincon completado. La clave es el booleano "ya esta Detenido", no
    // estadoCapturaReal entero: ese cambia en cada bloque de audio procesado (varias
    // veces por segundo mientras escucha) y usarlo como clave reiniciaria esta corrutina
    // sin necesidad en cada uno. pregonFinal == null como guarda adicional evita
    // recalcular si esta LaunchedEffect se vuelve a lanzar por otro cambio de discurso.
    LaunchedEffect(estadoCapturaReal is EstadoCapturaEnVivo.Detenido, discurso) {
        val actual = estadoCapturaReal
        val discursoActual = discurso
        if (!usaEnsayo && actual is EstadoCapturaEnVivo.Detenido && discursoActual != null && pregonFinal == null) {
            pregonFinal = viewModel.finalizarDeclamacion(
                discurso = discursoActual,
                resultado = actual.resultado,
                nombreTema = nombreTema,
                nombreRincon = nombreRincon,
                esRepaso = esRepaso,
                viaRinconLibre = viaRinconLibre
            )
        }
    }

    fun onPresionarEnsayo() {
        jobEnsayo?.cancel()
        pregonFinal = null
        estadoEnsayo = EstadoCapturaEnVivo.Escuchando(0f, sintetizarResultadoEnsayo(0f))
        jobEnsayo = scope.launch {
            val inicio = System.currentTimeMillis()
            while (isActive) {
                delay(120L)
                val factor = (System.currentTimeMillis() - inicio) / DURACION_ENSAYO_COMPLETO_MS.toFloat()
                estadoEnsayo = EstadoCapturaEnVivo.Escuchando(0f, sintetizarResultadoEnsayo(factor))
            }
        }
    }

    fun onSoltarEnsayo() {
        jobEnsayo?.cancel()
        jobEnsayo = null
        val resultadoFinal = (estadoEnsayo as? EstadoCapturaEnVivo.Escuchando)?.resultado
            ?: sintetizarResultadoEnsayo(0f)
        estadoEnsayo = EstadoCapturaEnVivo.Detenido(0f, resultadoFinal)
        val discursoActual = discurso
        if (discursoActual != null) {
            scope.launch {
                pregonFinal = viewModel.finalizarDeclamacion(
                    discurso = discursoActual,
                    resultado = resultadoFinal,
                    nombreTema = nombreTema,
                    nombreRincon = nombreRincon,
                    esRepaso = esRepaso,
                    viaRinconLibre = viaRinconLibre
                )
            }
        }
    }

    val discursoActual = discurso
    if (discursoActual == null) {
        PantallaCargandoDeclamacion(modifier)
        return
    }

    ContenidoDeclamacion(
        discurso = discursoActual,
        estadoPermiso = estadoPermisoEfectivo,
        estadoCaptura = estadoCaptura,
        onSolicitarPermiso = { lanzadorPermiso.launch(Manifest.permission.RECORD_AUDIO) },
        onEmpezar = { capturador?.iniciar() },
        onTerminar = { capturador?.detener() },
        onPresionarEnsayo = { onPresionarEnsayo() },
        onSoltarEnsayo = { onSoltarEnsayo() },
        onContinuar = onContinuar,
        resultado = pregonFinal,
        modifier = modifier
    )
}

/** Igual al chequeo en linea de [FuenteDeAudioReal.crear] (mismo `ContextCompat.checkSelfPermission`),
 * pero este no dispara el lint `MissingPermission` porque no rodea ninguna llamada a una
 * API de Android que exija el permiso -- solo lee el estado actual para decidir que UI
 * mostrar. No se toca el patron ya verificado de [FuenteDeAudioReal.crear]. */
private fun permisoMicrofonoConcedido(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

/**
 * Un [ResultadoEnVivo] sintetico para el modo de ensayo silencioso, a partir de cuanto
 * lleva el nino sosteniendo el circulo ([factor] 0..1, ver [DURACION_ENSAYO_COMPLETO_MS]).
 * Reutiliza los motores de dominio reales ([MotorPuntajeAudiencia]) sobre valores
 * acusticos inventados -pero coherentes: mientras mas sostiene, "más fuerte y firme" se
 * lee la energia- en vez de inventar un puntaje aparte: el resultado final se siente
 * funcional, no un relleno fijo.
 */
private fun sintetizarResultadoEnsayo(factor: Float): ResultadoEnVivo {
    val f = factor.coerceIn(0f, 1f)
    val acustico = ResultadoAcustico(
        volumenPromedio = f,
        variacionEntonacionSemitonos = f * 6f,
        ritmoSilabasPorMinuto = 70f + f * 60f,
        pausas = emptyList(),
        duracionTotalMs = (f * DURACION_ENSAYO_COMPLETO_MS).toLong()
    )
    val fluidez = (0.4f + f * 0.6f).coerceIn(0f, 1f)
    val puntaje = MotorPuntajeAudiencia.calcularPuntajeCompuesto(acustico, fluidez)
    return ResultadoEnVivo(acustico, fluidez, puntaje)
}

@Composable
private fun PantallaCargandoDeclamacion(modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.declamacion_cargando_descripcion)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Chirri(estado = EstadoChirri.NEUTRAL, modifier = Modifier.size(140.dp))
    }
}

/**
 * La parte pura de la pantalla de declamacion: nada de Context, permisos reales ni
 * `AudioRecord` -solo datos y callbacks-, para poder probarla con Robolectric sin tocar
 * hardware. [DeclamacionScreen] es quien orquesta el estado real y llama a esta funcion.
 *
 * El indicador en vivo durante [EstadoCapturaEnVivo.Escuchando] es deliberadamente NO
 * Chirri (ver el comentario de [Chirri]): la plaza reacciona, Chirri aparece antes y
 * despues.
 */
@Composable
fun ContenidoDeclamacion(
    discurso: DiscursoArmado,
    estadoPermiso: EstadoPermisoMicrofono,
    estadoCaptura: EstadoCapturaEnVivo,
    onSolicitarPermiso: () -> Unit,
    onEmpezar: () -> Unit,
    onTerminar: () -> Unit,
    onPresionarEnsayo: () -> Unit,
    onSoltarEnsayo: () -> Unit,
    onContinuar: () -> Unit,
    resultado: Pregon?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            resultado != null -> {
                val nivel = (estadoCaptura as? EstadoCapturaEnVivo.Detenido)?.resultado?.puntaje?.nivel
                    ?: NivelAtencion.ATENTA
                PantallaResultadoDeclamacion(pregon = resultado, nivel = nivel, onContinuar = onContinuar)
            }

            estadoPermiso == EstadoPermisoMicrofono.Denegado -> {
                TeleprompterDiscurso(discurso, modifier = Modifier.weight(1f, fill = false))
                PantallaEnsayoSilencioso(
                    estadoCaptura = estadoCaptura,
                    onPresionarEnsayo = onPresionarEnsayo,
                    onSoltarEnsayo = onSoltarEnsayo
                )
            }

            else -> when (estadoCaptura) {
                EstadoCapturaEnVivo.SinIniciar -> {
                    TeleprompterDiscurso(discurso, modifier = Modifier.weight(1f, fill = false))
                    PantallaListaParaHablar(
                        estadoPermiso = estadoPermiso,
                        onSolicitarPermiso = onSolicitarPermiso,
                        onEmpezar = onEmpezar
                    )
                }

                is EstadoCapturaEnVivo.Calibrando -> PantallaCalibrando(estadoCaptura.progreso)

                is EstadoCapturaEnVivo.Escuchando -> PantallaEscuchando(
                    resultado = estadoCaptura.resultado,
                    onTerminar = onTerminar
                )

                is EstadoCapturaEnVivo.Detenido -> PantallaGuardandoDeclamacion()
            }
        }
    }
}

@Composable
private fun TeleprompterDiscurso(discurso: DiscursoArmado, modifier: Modifier = Modifier) {
    val ordenados = remember(discurso) { discurso.bloques.sortedBy { it.franja } }
    val descripcion = stringResource(R.string.declamacion_discurso_descripcion)
    PanelDePlaza(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = descripcion }
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ordenados.forEach { bloque ->
                Text(text = bloque.texto, style = MaterialTheme.typography.bodyLarge, color = IndigoProfundo)
            }
        }
    }
}

@Composable
private fun PantallaListaParaHablar(
    estadoPermiso: EstadoPermisoMicrofono,
    onSolicitarPermiso: () -> Unit,
    onEmpezar: () -> Unit
) {
    Chirri(estado = EstadoChirri.ANIMANDO, modifier = Modifier.size(120.dp))
    BotonDePlaza(
        label = stringResource(R.string.declamacion_hablar),
        onClick = if (estadoPermiso == EstadoPermisoMicrofono.Concedido) onEmpezar else onSolicitarPermiso
    )
}

@Composable
private fun PantallaCalibrando(progreso: Float) {
    val descripcion = stringResource(R.string.declamacion_calibrando_descripcion)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = descripcion }
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Chirri(estado = EstadoChirri.ANIMANDO, modifier = Modifier.size(140.dp))
        Text(
            text = stringResource(R.string.declamacion_calibrando_texto),
            style = MaterialTheme.typography.bodyMedium,
            color = IndigoProfundo,
            textAlign = TextAlign.Center
        )
        LinearProgressIndicator(
            progress = { progreso.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = AmbarFarol,
            trackColor = VioletaAtardecer.copy(alpha = 0.25f)
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ColumnScope.PantallaEscuchando(resultado: ResultadoEnVivo, onTerminar: () -> Unit) {
    Spacer(modifier = Modifier.weight(1f))
    IndicadorEnVivo(resultado = resultado, modifier = Modifier.size(220.dp))
    SiluetasPublico(puntaje = resultado.puntaje.puntajeCompuesto, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.weight(1f))
    BotonDePlaza(label = stringResource(R.string.declamacion_terminar), onClick = onTerminar)
}

@Composable
private fun PantallaEnsayoSilencioso(
    estadoCaptura: EstadoCapturaEnVivo,
    onPresionarEnsayo: () -> Unit,
    onSoltarEnsayo: () -> Unit
) {
    val explicacion = stringResource(R.string.declamacion_ensayo_explicacion)
    val descripcionIdle = stringResource(R.string.declamacion_ensayo_boton_descripcion)
    val descripcionActivo = stringResource(R.string.declamacion_ensayo_sosteniendo_descripcion)
    val descripcion = if (estadoCaptura is EstadoCapturaEnVivo.Escuchando) descripcionActivo else descripcionIdle

    Chirri(estado = EstadoChirri.ANIMANDO, modifier = Modifier.size(110.dp))
    Text(
        text = explicacion,
        style = MaterialTheme.typography.bodyMedium,
        color = IndigoProfundo,
        textAlign = TextAlign.Center
    )
    Box(
        modifier = Modifier
            .size(180.dp)
            // Un unico gesto continuo: onPress arranca al bajar el dedo, tryAwaitRelease
            // suspende hasta que lo levanta -pase lo que pase adentro mientras tanto
            // (el estado cambia de SinIniciar a Escuchando y la UI interna cambia con
            // el), este Box y su pointerInput(Unit) nunca se recomponen fuera de la
            // composicion, asi que el gesto no se corta a la mitad.
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onPresionarEnsayo()
                    tryAwaitRelease()
                    onSoltarEnsayo()
                })
            }
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        when (estadoCaptura) {
            is EstadoCapturaEnVivo.Escuchando -> IndicadorEnVivo(
                resultado = estadoCaptura.resultado,
                modifier = Modifier.fillMaxSize()
            )

            is EstadoCapturaEnVivo.Detenido -> CircularProgressIndicator(color = IndigoProfundo)

            else -> Canvas(modifier = Modifier.fillMaxSize()) { dibujarCirculoListo() }
        }
    }
}

@Composable
private fun ColumnScope.PantallaGuardandoDeclamacion() {
    val descripcion = stringResource(R.string.declamacion_guardando_descripcion)
    Spacer(modifier = Modifier.weight(1f))
    Box(
        modifier = Modifier
            .size(80.dp)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = IndigoProfundo)
    }
    Spacer(modifier = Modifier.weight(1f))
}

@Composable
private fun ColumnScope.PantallaResultadoDeclamacion(pregon: Pregon, nivel: NivelAtencion, onContinuar: () -> Unit) {
    val estadoChirri = when (nivel) {
        NivelAtencion.ENTUSIASMADA -> EstadoChirri.CELEBRANDO
        NivelAtencion.ATENTA -> EstadoChirri.SALUDANDO
        // ANIMANDO, no PREOCUPADO: Chirri sigue animando al nino incluso con un
        // resultado bajo -su rol es alentar, no reflejar aqui su propio miedo (eso es
        // solo PREOCUPADO en su propia historia, ver el comentario de EstadoChirri).
        NivelAtencion.DISTRAIDA -> EstadoChirri.ANIMANDO
    }
    val descripcionPregon = stringResource(R.string.declamacion_pregon_descripcion, pregon.titular)

    Spacer(modifier = Modifier.weight(1f))
    Chirri(estado = estadoChirri, modifier = Modifier.size(160.dp))
    PanelDePlaza(modifier = Modifier.fillMaxWidth().semantics { contentDescription = descripcionPregon }) {
        Text(
            text = pregon.titular,
            style = MaterialTheme.typography.titleMedium,
            color = IndigoProfundo,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = Modifier.weight(1f))
    BotonDePlaza(label = stringResource(R.string.declamacion_continuar), onClick = onContinuar)
}

/**
 * El indicador en vivo: NO Chirri (ver el comentario grande de [ContenidoDeclamacion]).
 * Una capa de "aura" organica (un Path cerrado con `quadraticTo`, no un circulo perfecto)
 * mas un nucleo con degradado radial/lineal y un anillo de contorno -varias capas y
 * curvas, no una sola forma plana- que crecen y cambian de color con
 * [ResultadoEnVivo.puntaje]. Anima con [animateFloatAsState] para no saltar entre
 * valores en cada bloque de audio procesado.
 */
@Composable
fun IndicadorEnVivo(resultado: ResultadoEnVivo, modifier: Modifier = Modifier) {
    val puntajeAnimado by animateFloatAsState(
        targetValue = resultado.puntaje.puntajeCompuesto.coerceIn(0f, 1f),
        label = "indicadorEnVivoPuntaje"
    )
    val descripcion = when (resultado.puntaje.nivel) {
        NivelAtencion.DISTRAIDA -> stringResource(R.string.declamacion_indicador_nivel_distraida)
        NivelAtencion.ATENTA -> stringResource(R.string.declamacion_indicador_nivel_atenta)
        NivelAtencion.ENTUSIASMADA -> stringResource(R.string.declamacion_indicador_nivel_entusiasmada)
    }
    val nivel = resultado.puntaje.nivel
    Canvas(
        modifier = modifier
            .defaultMinSize(minWidth = 120.dp, minHeight = 120.dp)
            .semantics { contentDescription = descripcion }
    ) {
        dibujarIndicadorEnVivo(puntajeAnimado, nivel)
    }
}

private fun colorDeNivel(nivel: NivelAtencion): Color = when (nivel) {
    NivelAtencion.DISTRAIDA -> VioletaAtardecer.copy(alpha = 0.55f)
    NivelAtencion.ATENTA -> VioletaAtardecer
    NivelAtencion.ENTUSIASMADA -> AmbarFarol
}

private fun DrawScope.dibujarIndicadorEnVivo(puntaje: Float, nivel: NivelAtencion) {
    val color = colorDeNivel(nivel)
    val centro = Offset(size.width / 2f, size.height / 2f)
    val radioBase = size.minDimension * (0.24f + 0.14f * puntaje)

    // sombra de contacto: un segundo circulo semi-transparente, desplazado, para dar
    // sensacion de que el indicador "flota" sobre la plaza (seccion 4.0 del maestro).
    drawCircle(
        color = IndigoProfundo.copy(alpha = 0.15f),
        radius = radioBase * 1.05f,
        center = centro + Offset(0f, radioBase * 0.14f)
    )

    dibujarAuraOrganica(centro, radioBase, puntaje, color)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.65f), Color.Transparent),
            center = centro,
            radius = radioBase * 2.1f
        ),
        radius = radioBase * 2.1f,
        center = centro
    )

    drawCircle(
        brush = Brush.linearGradient(colors = listOf(color, color.copy(alpha = 0.75f))),
        radius = radioBase,
        center = centro
    )
    drawCircle(color = IndigoProfundo, radius = radioBase, center = centro, style = Stroke(width = size.minDimension * 0.012f))

    if (nivel == NivelAtencion.ENTUSIASMADA) {
        drawCircle(
            brush = Brush.sweepGradient(colors = listOf(AmbarFarol, RosaBerenjena, AmbarFarol)),
            radius = radioBase * 1.18f,
            center = centro,
            style = Stroke(width = size.minDimension * 0.02f)
        )
    }
}

/** Un aura con forma organica (no un circulo perfecto): 8 puntos alrededor del centro
 * cuyo radio ondula con `sin`, conectados con `quadraticTo`, mas notoria cuanto mayor es
 * [puntaje]. Es la capa que le da a todo el indicador su textura de "energia viva". */
private fun DrawScope.dibujarAuraOrganica(centro: Offset, radioBase: Float, puntaje: Float, color: Color) {
    val puntos = 8
    val variacion = radioBase * (0.10f + 0.14f * puntaje)
    fun puntoDelAura(indice: Int): Offset {
        val angulo = (2.0 * PI * indice / puntos).toFloat()
        val radio = radioBase * 1.3f + variacion * sin(angulo * 3f + puntaje * 9f)
        return Offset(centro.x + radio * cos(angulo), centro.y + radio * sin(angulo))
    }

    val path = Path().apply {
        val inicio = puntoDelAura(0)
        moveTo(inicio.x, inicio.y)
        for (i in 1..puntos) {
            val anterior = puntoDelAura(i - 1)
            val actual = puntoDelAura(i % puntos)
            val control = Offset((anterior.x + actual.x) / 2f, (anterior.y + actual.y) / 2f)
            quadraticTo(anterior.x, anterior.y, control.x, control.y)
        }
        close()
    }
    drawPath(
        path,
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.45f), Color.Transparent),
            center = centro,
            radius = radioBase * 2f
        )
    )
}

/**
 * El publico simplificado: 4 siluetas que se inclinan hacia adelante (interes) o hacia
 * atras (distraccion) segun [puntaje]. A proposito MVP-simple -sin vecinos con nombre ni
 * mas detalle- ver la nota de la tarea: el arte real de publico es trabajo futuro.
 */
@Composable
fun SiluetasPublico(puntaje: Float, modifier: Modifier = Modifier) {
    val puntajeAnimado by animateFloatAsState(targetValue = puntaje.coerceIn(0f, 1f), label = "siluetasPublico")
    val descripcion = stringResource(R.string.declamacion_publico_descripcion)
    Canvas(
        modifier = modifier
            .height(60.dp)
            .semantics { contentDescription = descripcion }
    ) {
        dibujarSiluetasPublico(puntajeAnimado)
    }
}

private fun DrawScope.dibujarSiluetasPublico(puntaje: Float) {
    val cantidad = 4
    val anchoCada = size.width / cantidad
    val colorSilueta = IndigoProfundo.copy(alpha = 0.5f + 0.3f * puntaje)
    for (i in 0 until cantidad) {
        val cx = anchoCada * (i + 0.5f)
        // Inclinacion: hacia atras (numero negativo) cuando el puntaje es bajo, hacia
        // adelante (interes) cuando es alto.
        val inclinacionGrados = -18f + puntaje * 28f
        rotate(degrees = inclinacionGrados, pivot = Offset(cx, size.height)) {
            drawCircle(color = colorSilueta, radius = size.height * 0.15f, center = Offset(cx, size.height * 0.24f))
            val cuerpo = Path().apply {
                moveTo(cx - size.height * 0.17f, size.height)
                quadraticTo(cx - size.height * 0.20f, size.height * 0.5f, cx, size.height * 0.34f)
                quadraticTo(cx + size.height * 0.20f, size.height * 0.5f, cx + size.height * 0.17f, size.height)
                close()
            }
            drawPath(cuerpo, color = colorSilueta)
        }
    }
}

/** El circulo "listo para sostener" del ensayo silencioso, antes de que el nino lo
 * presione: un degradado radial suave, no un circulo plano. */
private fun DrawScope.dibujarCirculoListo() {
    val centro = Offset(size.width / 2f, size.height / 2f)
    val radio = size.minDimension * 0.42f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(VioletaAtardecer.copy(alpha = 0.35f), VioletaAtardecer.copy(alpha = 0.08f)),
            center = centro,
            radius = radio
        ),
        radius = radio,
        center = centro
    )
    drawCircle(color = IndigoProfundo, radius = radio, center = centro, style = Stroke(width = size.minDimension * 0.02f))
}
