package pe.appmobile.laplaza.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

/**
 * Prueba de humo de Chirri en sus cinco estados: que componga sin reventar y que el
 * Canvas exponga el contentDescription correcto para cada uno -- el proyecto no acepta
 * un Canvas mudo para el lector de pantalla (ver seccion "Riesgos tecnicos" de la
 * ficha, punto 5: solo 2 de 17 apps hermanas lo hicieron bien en la auditoria).
 *
 * @Config(sdk = [35]): el proyecto compila con targetSdk 37, pero Robolectric 4.14 solo
 * soporta hasta el 35.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ChirriTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `Chirri compone sin reventar en cada uno de sus cinco estados`() {
        // Un solo setContent (ComposeContentTestRule solo permite llamarlo una vez por
        // prueba): se componen los cinco estados a la vez, uno debajo del otro.
        compose.setContent {
            LaPlazaTheme {
                Column {
                    EstadoChirri.entries.forEach { estado -> Chirri(estado = estado) }
                }
            }
        }
        compose.waitForIdle()

        EstadoChirri.entries.forEach { estado ->
            compose.onNodeWithContentDescription(parametrosDeEstado(estado).descripcion).assertExists()
        }
    }
}
