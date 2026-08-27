package pe.appmobile.laplaza.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.ui.screens.HomeScreen
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Prueba de navegacion real (no solo "la pantalla compone"): monta HomeScreen dentro de
 * un NavHost real de verdad (mismas rutas de [Rutas] que usa LaPlazaNavHost en la app),
 * toca una zona del mapa, y confirma que el NavController de verdad cambio de destino a
 * la ruta parametrizada del rincon con el id real -- no un espia de callback, sino el
 * backstack real de androidx.navigation.
 *
 * No usa LaPlazaViewModel ni Room a proposito: probar la navegacion en si no necesita
 * datos persistidos, y evita la complejidad de esperar a corutinas async del ViewModel en
 * esta prueba (esas quedan cubiertas aparte por LaPlazaViewModelTest).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class NavegacionDesdeHomeTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `tocar El Balcon en el mapa navega de verdad a la ruta de ese rincon`() {
        lateinit var navController: NavHostController

        compose.setContent {
            navController = rememberNavController()
            LaPlazaTheme {
                NavHost(navController = navController, startDestination = Rutas.HOME) {
                    composable(Rutas.HOME) {
                        HomeScreen(
                            perfil = PerfilEntity(id = 1, alias = "Ana", avatarId = 2),
                            rincones = rinconesDeMentira(),
                            racha = null,
                            onNavegarRincon = { id -> navController.navigate(Rutas.rinconRuta(id)) },
                            onNavegarPerfil = {},
                            onNavegarAjustes = {},
                            onNavegarCuaderno = {}
                        )
                    }
                    composable(
                        route = Rutas.RINCON,
                        arguments = listOf(navArgument(Rutas.ARG_RINCON_ID) { type = NavType.StringType })
                    ) { backStackEntry ->
                        // destino minimo, solo para que la ruta exista en el grafo de prueba.
                        Text(backStackEntry.arguments?.getString(Rutas.ARG_RINCON_ID).orEmpty())
                    }
                }
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("El Balcón, disponible").performClick()
        compose.waitForIdle()

        assertEquals(Rutas.RINCON, navController.currentDestination?.route)
        assertEquals("BALCON", navController.currentBackStackEntry?.arguments?.getString(Rutas.ARG_RINCON_ID))
    }

    private fun rinconesDeMentira() = listOf(
        RinconEntity(id = "BALCON", nombre = "El Balcón", descripcion = "Presentarte", orden = 1),
        RinconEntity(id = "KIOSCO", nombre = "El Kiosco", descripcion = "Contar algo", orden = 2)
    )
}
