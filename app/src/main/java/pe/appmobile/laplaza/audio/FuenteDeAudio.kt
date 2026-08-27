package pe.appmobile.laplaza.audio

/**
 * Fuente de bloques de audio PCM 16 bits (mono), desacoplada de Android.
 *
 * [MotorAcustico] ya se probó contra buffers sintéticos (ver `MotorAcusticoTest`), nunca
 * contra un `AudioRecord` real: `AudioRecord` no se puede ejercitar de forma significativa
 * en una prueba unitaria de JVM. Esta interfaz es el punto de inyección que permite que
 * [CapturadorVoz] se pruebe igual de aislado -con una implementación falsa en los tests- y
 * en producción reciba [FuenteDeAudioReal], la única que de verdad toca `AudioRecord`.
 */
interface FuenteDeAudio {

    /** Tasa de muestreo real de esta fuente, en Hz. [MotorAcustico.calcularF0] la necesita
     * para convertir períodos de autocorrelación a frecuencia. */
    val sampleRateHz: Int

    /**
     * Devuelve el siguiente bloque de muestras ya capturado, o un [ShortArray] vacío si la
     * fuente no tiene más datos (fin de la captura -por ejemplo, tras llamar a [liberar]).
     *
     * Se espera que bloquee al llamador hasta tener el bloque completo, igual que
     * `AudioRecord.read(...)` en modo bloqueante: por eso [CapturadorVoz] la llama siempre
     * desde una corrutina en un dispatcher pensado para trabajo bloqueante, nunca desde el
     * hilo que la invoca directamente.
     */
    fun leerSiguienteBloque(): ShortArray

    /** Libera el recurso subyacente. En [FuenteDeAudioReal] detiene y libera el `AudioRecord`
     * real -nunca debe quedar un micrófono abierto una vez que el niño sale del rincón. */
    fun liberar()
}
