package pe.appmobile.laplaza.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.ui.LaPlazaViewModel
import pe.appmobile.laplaza.ui.components.Chirri
import pe.appmobile.laplaza.ui.components.EstadoChirri
import pe.appmobile.laplaza.ui.screens.AjustesScreen
import pe.appmobile.laplaza.ui.screens.CrearPerfilScreen
import pe.appmobile.laplaza.ui.screens.HomeScreen
import pe.appmobile.laplaza.ui.screens.PantallaMarcador
import pe.appmobile.laplaza.ui.screens.PerfilScreen
import pe.appmobile.laplaza.ui.theme.BlancoRosado

/**
 * El grafo de navegacion de las 12 pantallas de la ficha (7 rincones + Rincon Libre +
 * home + Cuaderno de Pregones + perfil + ajustes) mas la creacion de perfil de primer
 * arranque. Solo home, perfil y ajustes tienen contenido real en esta tarea; el resto son
 * destinos marcador (ver [PantallaMarcador]) que una tarea posterior reemplaza uno por uno.
 *
 * [navController] es un parametro con valor por defecto (no algo que la app real necesite
 * pasar nunca) solo para que las pruebas puedan inspeccionar la navegacion real despues
 * de un tap, sin duplicar este grafo en el codigo de prueba.
 *
 * Espera a [LaPlazaViewModel.listoParaNavegar] antes de decidir la pantalla inicial:
 * un StateFlow recien creado no distingue "todavia no se leyo Room" de "no hay perfil",
 * y esa distincion es justo la que decide si la app abre en crear-perfil o en home.
 */
@Composable
fun LaPlazaNavHost(
    viewModel: LaPlazaViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val listo by viewModel.listoParaNavegar.collectAsState()

    if (!listo) {
        PantallaDeCarga(modifier)
        return
    }

    val scope = rememberCoroutineScope()
    val perfilInicial by viewModel.perfil.collectAsState()
    val destinoInicial = if (perfilInicial == null) Rutas.CREAR_PERFIL else Rutas.HOME

    NavHost(navController = navController, startDestination = destinoInicial, modifier = modifier) {
        composable(Rutas.CREAR_PERFIL) {
            CrearPerfilScreen(
                onCrear = { alias, avatarId ->
                    scope.launch {
                        viewModel.crearPerfil(alias, avatarId)
                        navController.navigate(Rutas.HOME) {
                            popUpTo(Rutas.CREAR_PERFIL) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Rutas.HOME) {
            val perfil by viewModel.perfil.collectAsState()
            val rincones by viewModel.rincones.collectAsState()
            val racha by viewModel.racha.collectAsState()
            val perfilActual = perfil
            if (perfilActual != null) {
                HomeScreen(
                    perfil = perfilActual,
                    rincones = rincones,
                    racha = racha,
                    onNavegarRincon = { id -> navController.navigate(Rutas.rinconRuta(id)) },
                    onNavegarPerfil = { navController.navigate(Rutas.PERFIL) },
                    onNavegarAjustes = { navController.navigate(Rutas.AJUSTES) },
                    onNavegarCuaderno = { navController.navigate(Rutas.CUADERNO_DE_PREGONES) }
                )
            }
        }

        composable(Rutas.PERFIL) {
            val perfil by viewModel.perfil.collectAsState()
            val perfilActual = perfil
            if (perfilActual != null) {
                PerfilScreen(
                    perfil = perfilActual,
                    onGuardar = { alias, avatarId ->
                        scope.launch {
                            viewModel.actualizarPerfil(perfilActual.copy(alias = alias, avatarId = avatarId))
                        }
                        navController.popBackStack()
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Rutas.AJUSTES) {
            val perfil by viewModel.perfil.collectAsState()
            val perfilActual = perfil
            if (perfilActual != null) {
                AjustesScreen(
                    sonidoActivado = perfilActual.sonidoActivado,
                    onCambiarSonido = { activado ->
                        scope.launch {
                            viewModel.actualizarPerfil(perfilActual.copy(sonidoActivado = activado))
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(Rutas.CUADERNO_DE_PREGONES) {
            PantallaMarcador(
                titulo = stringResource(R.string.cuaderno_titulo),
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.RINCON,
            arguments = listOf(navArgument(Rutas.ARG_RINCON_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val rinconId = backStackEntry.arguments?.getString(Rutas.ARG_RINCON_ID).orEmpty()
            val rincones by viewModel.rincones.collectAsState()
            val nombreLibre = stringResource(R.string.home_rincon_libre)
            val titulo = rincones.find { it.id == rinconId }?.nombre ?: nombreLibre
            PantallaMarcador(
                titulo = titulo,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PantallaDeCarga(modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.cargando_descripcion)
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
