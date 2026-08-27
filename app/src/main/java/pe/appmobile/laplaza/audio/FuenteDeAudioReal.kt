package pe.appmobile.laplaza.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat

/**
 * Envoltura real de `android.media.AudioRecord`. Es la única clase de este paquete que toca
 * Android de verdad; todo lo demás (calibración, orquestación con los motores, modo de
 * ensayo silencioso) es JVM pura y se prueba sin ella.
 *
 * No hay forma de construir esta clase sin pasar por [crear]: el constructor es privado a
 * propósito para que sea imposible tener una instancia con un `AudioRecord` no inicializado
 * o creado sin permiso.
 */
class FuenteDeAudioReal private constructor(
    private val audioRecord: AudioRecord,
    override val sampleRateHz: Int,
    private val muestrasPorBloque: Int
) : FuenteDeAudio {

    private var liberado = false

    override fun leerSiguienteBloque(): ShortArray {
        if (liberado) return ShortArray(0)
        val bloque = ShortArray(muestrasPorBloque)
        val leidas = audioRecord.read(bloque, 0, muestrasPorBloque)
        // AudioRecord.read puede devolver 0 o un código de error negativo (ERROR_INVALID_OPERATION,
        // etc.) además de la cantidad real leída; en cualquier caso que no sea "leyó todo o parte
        // de un bloque real" se trata como "no hay más datos" en vez de propagar un ShortArray a
        // medio llenar con basura del array recién creado.
        return if (leidas > 0) bloque.copyOf(leidas) else ShortArray(0)
    }

    override fun liberar() {
        if (liberado) return
        liberado = true
        try {
            audioRecord.stop()
        } catch (_: IllegalStateException) {
            // AudioRecord.stop() exige que ya esté grabando; si algo lo detuvo antes (error de
            // hardware, ya liberado por otra vía) no es un caso que deba reventar la salida del
            // rincón, solo se ignora y se sigue liberando.
        }
        audioRecord.release()
    }

    companion object {
        /** 16 kHz mono es suficiente para voz humana (rango 70-400 Hz que ya asume
         * [pe.appmobile.laplaza.domain.engine.MotorAcustico]) y es la tasa que usan sus propias
         * pruebas (`MotorAcusticoTest`), así que se usa la misma aquí para que el comportamiento
         * en producción coincida con lo ya probado. */
        const val SAMPLE_RATE_HZ = 16_000

        /** Cadencia de actualización "relajada" (no muestra a muestra): el mismo enfoque de
         * ~150-200 ms ya validado para el motor acústico en tiempo real de este mismo lote de
         * apps (ficha de El Gran Telón, que comparte el riesgo técnico pero no código). Se fija
         * en 200 ms, el extremo superior del rango, para dar a cada bloque suficientes ciclos de
         * una voz grave (70 Hz) como para que `calcularF0` tenga margen de autocorrelación. */
        const val DURACION_BLOQUE_MS = 200L

        private val MUESTRAS_POR_BLOQUE = (SAMPLE_RATE_HZ * DURACION_BLOQUE_MS / 1000L).toInt()

        /**
         * Construye la fuente real, o `null` si falta el permiso o el hardware no pudo
         * inicializarse (nunca lanza una excepción por esos dos casos: son esperables -permiso
         * denegado, micrófono ocupado por otra app- y quien llama debe poder pasar al modo de
         * ensayo silencioso en vez de recibir un crash).
         *
         * El chequeo de permiso está escrito en línea, inmediatamente antes de construir
         * `AudioRecord` -a propósito, no por descuido: el lint de Android (`MissingPermission`)
         * no rastrea el permiso a través de una función auxiliar separada (por ejemplo, un
         * `tienePermiso()` invocado desde otro lado); solo lo reconoce cuando
         * `ContextCompat.checkSelfPermission(...) == PERMISSION_GRANTED` aparece escrito aquí
         * mismo, dominando el flujo hasta la llamada que necesita el permiso. Verificado con
         * `lintDebug` en este mismo cambio.
         */
        fun crear(context: Context): FuenteDeAudioReal? {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }

            val tamanoMinimoBytes = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (tamanoMinimoBytes == AudioRecord.ERROR || tamanoMinimoBytes == AudioRecord.ERROR_BAD_VALUE) {
                return null
            }
            // Al menos el doble del bloque relajado (200 ms) en bytes (2 bytes/muestra), para
            // que el driver tenga margen y no se sobrescriba antes de que se llame a read().
            val tamanoBufferBytes = maxOf(tamanoMinimoBytes, MUESTRAS_POR_BLOQUE * 2 * 2)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                tamanoBufferBytes
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord.release()
                return null
            }

            audioRecord.startRecording()
            return FuenteDeAudioReal(audioRecord, SAMPLE_RATE_HZ, MUESTRAS_POR_BLOQUE)
        }
    }
}
