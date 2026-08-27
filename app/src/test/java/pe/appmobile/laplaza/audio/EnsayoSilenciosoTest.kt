package pe.appmobile.laplaza.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas del modo de ensayo silencioso: nunca toca [FuenteDeAudio] ni Android, así que son
 * pruebas de JVM puras sobre funciones sin estado.
 */
class EnsayoSilenciosoTest {

    @Test
    fun `un toque largo y sostenido da un puntaje compuesto mayor que uno corto e interrumpido`() {
        val corto = EnsayoSilencioso.simularPuntaje(duracionPresionadoMs = 400L, soltadoAntesDeTiempo = true)
        val largo = EnsayoSilencioso.simularPuntaje(duracionPresionadoMs = 6000L, soltadoAntesDeTiempo = false)

        assertTrue(
            "un ensayo largo y firme deberia dar mas puntaje que uno corto e interrumpido: " +
                "corto=${corto.puntajeCompuesto} largo=${largo.puntajeCompuesto}",
            largo.puntajeCompuesto > corto.puntajeCompuesto
        )
    }

    @Test
    fun `soltar antes de tiempo registra una pausa, sostener hasta el final no`() {
        val interrumpido = EnsayoSilencioso.simular(duracionPresionadoMs = 3000L, soltadoAntesDeTiempo = true)
        val sostenido = EnsayoSilencioso.simular(duracionPresionadoMs = 3000L, soltadoAntesDeTiempo = false)

        assertTrue(interrumpido.pausas.isNotEmpty())
        assertTrue(sostenido.pausas.isEmpty())
    }

    @Test
    fun `un toque mas largo da mas volumen que uno mas corto, ambos sostenidos hasta el final`() {
        val corto = EnsayoSilencioso.simular(duracionPresionadoMs = 500L, soltadoAntesDeTiempo = false)
        val largo = EnsayoSilencioso.simular(duracionPresionadoMs = 5000L, soltadoAntesDeTiempo = false)

        assertTrue(
            "corto=${corto.volumenPromedio} largo=${largo.volumenPromedio}",
            largo.volumenPromedio > corto.volumenPromedio
        )
    }

    @Test
    fun `la duracion efectiva nunca supera el tope maximo util, aunque el toque sea muy largo`() {
        val resultado = EnsayoSilencioso.simular(duracionPresionadoMs = 60_000L, soltadoAntesDeTiempo = false)
        assertTrue(resultado.duracionTotalMs <= 8000L)
    }

    @Test
    fun `un toque de cero milisegundos no revienta y da el volumen minimo`() {
        val resultado = EnsayoSilencioso.simular(duracionPresionadoMs = 0L, soltadoAntesDeTiempo = false)
        assertEquals(0f, resultado.volumenPromedio, 0.001f)
        assertEquals(0f, resultado.ritmoSilabasPorMinuto, 0.001f)
    }

    @Test
    fun `una duracion negativa no revienta, se trata como cero`() {
        val resultado = EnsayoSilencioso.simular(duracionPresionadoMs = -500L, soltadoAntesDeTiempo = false)
        assertEquals(0f, resultado.volumenPromedio, 0.001f)
        assertEquals(0L, resultado.duracionTotalMs)
    }
}
