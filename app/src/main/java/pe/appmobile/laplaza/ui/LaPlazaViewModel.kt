package pe.appmobile.laplaza.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.data.repository.LaPlazaRepository

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
}
