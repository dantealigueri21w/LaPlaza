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

    // Bug real (27/08/2026): Rodrigo se quedo en silencio total en su celular, incluso
    // despues de la primera correccion de MotorFluidez, y el pregon seguia diciendo "Tu
    // voz se escucho en todo el Mostrador". Causa: sin voz real las 4 variables quedan en
    // 0,0,0,0 (un empate), y variableMasFuerte elegia "volumen" solo por ser la primera
    // clave del mapa -- nunca fue de verdad la variable "mas fuerte".
    @Test
    fun `silencio total (las 4 variables en 0) no genera un pregon de elogio falso`() {
        val silencioTotal = datos(volumen = 0f, entonacion = 0f, ritmo = 0f, fluidez = 0f)
        val pregon = MotorPregon.generar(silencioTotal)
        assertEquals("silencio", pregon.variableDestacada)
        assertEquals(
            "No te escuchamos esta vez en El Balcón. ¿Lo intentamos de nuevo?",
            pregon.titular
        )
    }

    @Test
    fun `una senal minima real (por encima del umbral) no cae al mensaje de silencio`() {
        val vozMuyBaja = datos(volumen = 0.06f, entonacion = 0f, ritmo = 0f, fluidez = 0f)
        val pregon = MotorPregon.generar(vozMuyBaja)
        assertEquals("volumen", pregon.variableDestacada)
    }

    @Test
    fun `huboVozReal es false solo cuando las 4 variables estan por debajo del umbral`() {
        assertEquals(false, MotorPregon.huboVozReal(datos(0f, 0f, 0f, 0f)))
        assertEquals(false, MotorPregon.huboVozReal(datos(0.02f, 0.01f, 0f, 0.03f)))
        assertEquals(true, MotorPregon.huboVozReal(datos(0.1f, 0f, 0f, 0f)))
    }
}
