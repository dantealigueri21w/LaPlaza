package pe.appmobile.laplaza.audio

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Prueba del único punto de esta clase que de verdad necesita un `Context` de Android -el
 * chequeo de permiso antes de construir `AudioRecord`. El resto de FuenteDeAudioReal (leer
 * bloques, liberar) no se prueba aquí: en una JVM no hay hardware de micrófono real que leer,
 * y ejercitarlo de forma significativa exigiría un `AudioRecord` real (ver el comentario de
 * [FuenteDeAudio]) -por eso el resto de la lógica de este paquete se probó desacoplada de
 * Android, contra la fuente falsa de `CapturadorVozTest`.
 *
 * @Config(sdk = [35]) fuerza el SDK simulado: el proyecto compila con targetSdk 37, pero
 * Robolectric 4.14 solo soporta hasta el 35 (mismo ajuste que AppDatabaseTest y
 * LaPlazaRepositoryTest en `data/`).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FuenteDeAudioRealTest {

    @Test
    fun `crear devuelve null si el permiso RECORD_AUDIO no fue concedido, sin tocar AudioRecord`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).denyPermissions(Manifest.permission.RECORD_AUDIO)

        val fuente = FuenteDeAudioReal.crear(app)

        assertNull(fuente)
    }

    @Test
    fun `crear con el permiso denegado no revienta al llamarlo varias veces`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).denyPermissions(Manifest.permission.RECORD_AUDIO)

        assertNull(FuenteDeAudioReal.crear(app))
        assertNull(FuenteDeAudioReal.crear(app))
    }
}
