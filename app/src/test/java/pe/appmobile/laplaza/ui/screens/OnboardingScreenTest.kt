package pe.appmobile.laplaza.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * El onboarding real (seccion 5 del maestro: "3 o 4 pantallas como maximo, una sola
 * vez"): 3 pasos internos -proposito, el mundo y Chirri, como avanzar mas el aviso del
 * microfono- que se recorren con "Siguiente" y terminan en [onTerminar], nunca antes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w480dp-h1200dp")
class OnboardingScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `empieza en el primer paso, explicando el proposito`() {
        compose.setContent {
            LaPlazaTheme { OnboardingScreen(onTerminar = {}) }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Siguiente").assertExists()
    }

    @Test
    fun `recorrer los 3 pasos con Siguiente y terminar solo en el ultimo dispara onTerminar`() {
        var termino = false
        compose.setContent {
            LaPlazaTheme { OnboardingScreen(onTerminar = { termino = true }) }
        }
        compose.waitForIdle()

        compose.onNodeWithText("Siguiente").performClick()
        compose.waitForIdle()
        assertTrue("no debe terminar en el paso 2 de 3", !termino)

        compose.onNodeWithText("Siguiente").performClick()
        compose.waitForIdle()
        assertTrue("no debe terminar antes del ultimo boton", !termino)

        compose.onNodeWithText("¡Vamos a la plaza!").performClick()
        compose.waitForIdle()

        assertTrue(termino)
    }
}
