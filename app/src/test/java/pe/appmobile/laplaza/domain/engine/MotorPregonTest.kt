package pe.appmobile.laplaza.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.appmobile.laplaza.domain.model.DatosIntento

class MotorPregonTest {

    private fun datos(volumen: Float, entonacion: Float, ritmo: Float, fluidez: Float) = DatosIntento(
        nombreTema = "Preséntate a la plaza",
        nombreRincon = "El Balcón",
        puntajeVolumen = volumen,
        puntajeEntonacion = entonacion,
        puntajeRitmo = ritmo,
        puntajeFluidez = fluidez
    )

    @Test
    fun `cuando el volumen es la variable mas alta, el titular la menciona`() {
        val pregon = MotorPregon.generar(datos(volumen = 0.9f, entonacion = 0.3f, ritmo = 0.3f, fluidez = 0.3f))
        assertEquals("volumen", pregon.variableDestacada)
        assertEquals(
            "Tu voz se escuchó en todo El Balcón durante \"Preséntate a la plaza\"",
            pregon.titular
        )
    }

    @Test
    fun `cuando la entonacion es la mas alta, el titular la menciona`() {
        val pregon = MotorPregon.generar(datos(volumen = 0.3f, entonacion = 0.9f, ritmo = 0.3f, fluidez = 0.3f))
        assertEquals("entonacion", pregon.variableDestacada)
    }

    @Test
    fun `cuando el ritmo es el mas alto, el titular lo menciona`() {
        val pregon = MotorPregon.generar(datos(volumen = 0.3f, entonacion = 0.3f, ritmo = 0.9f, fluidez = 0.3f))
        assertEquals("ritmo", pregon.variableDestacada)
    }

    @Test
    fun `cuando la fluidez es la mas alta, el titular la menciona`() {
        val pregon = MotorPregon.generar(datos(volumen = 0.3f, entonacion = 0.3f, ritmo = 0.3f, fluidez = 0.9f))
        assertEquals("fluidez", pregon.variableDestacada)
    }

    @Test
    fun `con valores empatados, elige una variable de forma determinista (siempre la misma)`() {
        val empate = datos(volumen = 0.5f, entonacion = 0.5f, ritmo = 0.5f, fluidez = 0.5f)
        val primero = MotorPregon.generar(empate)
        val segundo = MotorPregon.generar(empate)
        assertEquals(primero.variableDestacada, segundo.variableDestacada)
    }
}
