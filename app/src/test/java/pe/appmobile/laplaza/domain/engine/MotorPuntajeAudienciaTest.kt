package pe.appmobile.laplaza.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.appmobile.laplaza.domain.model.NivelAtencion
import pe.appmobile.laplaza.domain.model.Pausa
import pe.appmobile.laplaza.domain.model.ResultadoAcustico

class MotorPuntajeAudienciaTest {

    @Test
    fun `calcularPuntajeVentana en silencio da cero`() {
        val puntaje = MotorPuntajeAudiencia.calcularPuntajeVentana(volumen = 0.8f, variacionEntonacion = 5f, esVoz = false)
        assertEquals(0f, puntaje, 0.001f)
    }

    @Test
    fun `calcularPuntajeVentana con volumen alto y buena variacion da puntaje alto`() {
        val puntaje = MotorPuntajeAudiencia.calcularPuntajeVentana(volumen = 0.8f, variacionEntonacion = 6f, esVoz = true)
        assertTrue("Puntaje esperado mayor a 0.7, fue $puntaje", puntaje > 0.7f)
    }

    @Test
    fun `calcularPuntajeVentana con volumen bajo da puntaje bajo`() {
        val alto = MotorPuntajeAudiencia.calcularPuntajeVentana(volumen = 0.8f, variacionEntonacion = 3f, esVoz = true)
        val bajo = MotorPuntajeAudiencia.calcularPuntajeVentana(volumen = 0.1f, variacionEntonacion = 3f, esVoz = true)
        assertTrue(bajo < alto)
    }

    @Test
    fun `calcularPuntajeRitmo dentro del rango ideal da 1`() {
        assertEquals(1f, MotorPuntajeAudiencia.calcularPuntajeRitmo(120f), 0.001f)
    }

    @Test
    fun `calcularPuntajeRitmo muy lento da menos de 1`() {
        assertTrue(MotorPuntajeAudiencia.calcularPuntajeRitmo(30f) < 1f)
    }

    @Test
    fun `calcularPuntajeRitmo muy rapido da menos de 1`() {
        assertTrue(MotorPuntajeAudiencia.calcularPuntajeRitmo(260f) < 1f)
    }

    @Test
    fun `calcularPuntajeRitmo de cero da cero`() {
        assertEquals(0f, MotorPuntajeAudiencia.calcularPuntajeRitmo(0f), 0.001f)
    }

    @Test
    fun `calcularPuntajeCompuesto con buenos valores da nivel ENTUSIASMADA`() {
        val resultado = ResultadoAcustico(
            volumenPromedio = 0.8f,
            variacionEntonacionSemitonos = 6f,
            ritmoSilabasPorMinuto = 120f,
            pausas = emptyList(),
            duracionTotalMs = 5000L
        )
        val puntaje = MotorPuntajeAudiencia.calcularPuntajeCompuesto(resultado, fluidez = 1f)
        assertEquals(NivelAtencion.ENTUSIASMADA, puntaje.nivel)
    }

    @Test
    fun `calcularPuntajeCompuesto con valores bajos da nivel DISTRAIDA`() {
        val resultado = ResultadoAcustico(
            volumenPromedio = 0.05f,
            variacionEntonacionSemitonos = 0.5f,
            ritmoSilabasPorMinuto = 20f,
            pausas = listOf(Pausa(0L, 2000L)),
            duracionTotalMs = 5000L
        )
        val puntaje = MotorPuntajeAudiencia.calcularPuntajeCompuesto(resultado, fluidez = 0.2f)
        assertEquals(NivelAtencion.DISTRAIDA, puntaje.nivel)
    }
}
