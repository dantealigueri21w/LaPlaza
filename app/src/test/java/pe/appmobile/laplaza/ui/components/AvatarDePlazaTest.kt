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

/** Que los 8 avatares compongan sin reventar (una silueta distinta cada uno, ver
 * AvatarDePlaza.kt) y que la descripcion de accesibilidad sea la real que se le pasa. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AvatarDePlazaTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `los 8 avatares componen sin reventar en una sola composicion`() {
        compose.setContent {
            LaPlazaTheme {
                Column {
                    for (id in 1..8) {
                        AvatarDePlaza(avatarId = id, descripcion = "Avatar $id")
                    }
                }
            }
        }
        compose.waitForIdle()

        for (id in 1..8) {
            compose.onNodeWithContentDescription("Avatar $id").assertExists()
        }
    }

    @Test
    fun `usa la descripcion real que recibe`() {
        compose.setContent {
            LaPlazaTheme {
                AvatarDePlaza(avatarId = 4, descripcion = "Avatar 4")
            }
        }
        compose.waitForIdle()

        compose.onNodeWithContentDescription("Avatar 4").assertExists()
    }
}
