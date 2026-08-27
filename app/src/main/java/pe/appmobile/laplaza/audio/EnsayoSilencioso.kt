package pe.appmobile.laplaza.audio

import pe.appmobile.laplaza.domain.engine.MotorFluidez
import pe.appmobile.laplaza.domain.engine.MotorPuntajeAudiencia
import pe.appmobile.laplaza.domain.model.Pausa
import pe.appmobile.laplaza.domain.model.PuntajeAudiencia
import pe.appmobile.laplaza.domain.model.ResultadoAcustico

/**
 * Modo de ensayo cuando no hay micrófono disponible -permiso `RECORD_AUDIO` denegado, o el
 * niño elige "ensayar" sin hablar en voz alta. Nunca toca [FuenteDeAudio] ni `AudioRecord`:
 * en vez de una voz real, usa cuánto tiempo mantuvo presionado el botón de hablar (y si lo
 * soltó antes de tiempo) como el único proxy disponible de energía.
 *
 * Principio de honestidad de esta simulación (pedido explícito de la ficha): para una
 * variable donde el toque sí da una señal razonable -volumen, como proxy de "se animó a
 * sostenerlo"- se deriva de la duración real. Para las que no hay ninguna señal real de tono
 * de voz -la entonación, sobre todo- se usa un valor neutro fijo en vez de inventar una
 * lectura que parezca medida y no lo sea. Documentado variable por variable abajo.
 */
object EnsayoSilencioso {

    /** Por debajo de esto, sostener el botón no se distingue de un toque accidental: no se
     * llegó a "decir" nada. */
    private const val DURACION_MINIMA_CON_SENAL_MS = 300L

    /** Tope de duración útil: sostener el botón más de esto no representa más energía real
     * -un discurso corto de este proyecto dura unos pocos segundos, no minutos-, así que la
     * proporción de energía se satura aquí en vez de seguir creciendo sin límite. */
    private const val DURACION_MAXIMA_UTIL_MS = 8_000L

    /** Entonación neutra-baja: ni "monótono total" (0 semitonos) ni "muy expresivo" (6
     * semitonos, el techo que ya usa [MotorPuntajeAudiencia]). No hay ninguna señal real de
     * tono de voz en un toque de pantalla, así que se fija un valor neutro en vez de fabricar
     * una variación que no se midió. */
    private const val VARIACION_ENTONACION_NEUTRA = 1.5f

    /** Ritmo plausible de un toque sostenido hasta soltarlo con calma, dentro del rango
     * "ideal" que reconoce [MotorPuntajeAudiencia.calcularPuntajeRitmo] (90-160 sílabas/min). */
    private const val RITMO_TOQUE_SOSTENIDO = 110f

    /** Ritmo plausible de un toque interrumpido antes de tiempo: más bajo que el ideal, como
     * proxy de un discurso que se cortó, no de uno fluido. */
    private const val RITMO_TOQUE_INTERRUMPIDO = 70f

    /** Duración fija de la pausa "sucia" que se registra cuando el toque se suelta antes de
     * tiempo, colocada cerca del final del ensayo (proxy de titubeo justo antes de soltar). */
    private const val DURACION_PAUSA_POR_INTERRUPCION_MS = 1200L

    /**
     * Traduce un toque sostenido a un [ResultadoAcustico] plausible, con la misma forma que
     * usaría un intento con micrófono real -así el resto de la tubería (puntaje, y más
     * adelante `LaPlazaRepository.registrarIntento`) no necesita un camino de código aparte
     * para este modo.
     *
     * @param duracionPresionadoMs cuánto tiempo mantuvo presionado el botón de hablar.
     * @param soltadoAntesDeTiempo si soltó antes de lo esperado para el discurso (proxy de
     *   "se cortó a mitad de camino"), en vez de soltarlo con calma al terminar.
     */
    fun simular(duracionPresionadoMs: Long, soltadoAntesDeTiempo: Boolean): ResultadoAcustico {
        val duracionEfectivaMs = duracionPresionadoMs.coerceIn(0L, DURACION_MAXIMA_UTIL_MS)

        // Volumen: único valor con una señal real detrás. Un toque más largo y sostenido es
        // el proxy más honesto disponible de "se animó a hablar fuerte y sin cortar"; por
        // debajo del mínimo con señal, se trata como silencio (no llegó a "decir" nada).
        val volumenPromedio = if (duracionEfectivaMs < DURACION_MINIMA_CON_SENAL_MS) {
            0f
        } else {
            (duracionEfectivaMs.toFloat() / DURACION_MAXIMA_UTIL_MS).coerceIn(0f, 1f)
        }

        val ritmoSilabasPorMinuto = if (duracionEfectivaMs < DURACION_MINIMA_CON_SENAL_MS) {
            0f
        } else if (soltadoAntesDeTiempo) {
            RITMO_TOQUE_INTERRUMPIDO
        } else {
            RITMO_TOQUE_SOSTENIDO
        }

        val pausas: List<Pausa> = if (soltadoAntesDeTiempo && duracionEfectivaMs >= DURACION_MINIMA_CON_SENAL_MS) {
            val inicioMs = (duracionEfectivaMs * 0.7).toLong()
            listOf(Pausa(inicioMs = inicioMs, duracionMs = DURACION_PAUSA_POR_INTERRUPCION_MS))
        } else {
            emptyList()
        }

        return ResultadoAcustico(
            volumenPromedio = volumenPromedio,
            variacionEntonacionSemitonos = if (volumenPromedio > 0f) VARIACION_ENTONACION_NEUTRA else 0f,
            ritmoSilabasPorMinuto = ritmoSilabasPorMinuto,
            pausas = pausas,
            duracionTotalMs = duracionEfectivaMs
        )
    }

    /** Igual que [simular], pero ya combinado en el puntaje compuesto de audiencia -lo que de
     * verdad necesita una pantalla para mover a Chirri y a la plaza tras un ensayo sin voz
     * real. */
    fun simularPuntaje(duracionPresionadoMs: Long, soltadoAntesDeTiempo: Boolean): PuntajeAudiencia {
        val resultado = simular(duracionPresionadoMs, soltadoAntesDeTiempo)
        val fluidez = MotorFluidez.calcularFluidez(resultado.pausas, resultado.duracionTotalMs)
        return MotorPuntajeAudiencia.calcularPuntajeCompuesto(resultado, fluidez)
    }
}
