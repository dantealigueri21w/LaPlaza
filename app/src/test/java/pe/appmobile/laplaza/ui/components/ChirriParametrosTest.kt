package pe.appmobile.laplaza.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba pura del mapeo estado -> [ParametrosChirri] de Chirri.kt. No usa Robolectric a
 * proposito: [ParametrosChirri] y [ColorChirri] no dependen de Android ni de la runtime
 * de Compose (nada de androidx.compose.ui.graphics.Color aqui), asi que este mapeo se
 * puede verificar con JUnit puro.
 */
class ChirriParametrosTest {

    @Test
    fun `parametrosDeEstado es deterministico para los cinco estados`() {
        EstadoChirri.entries.forEach { estado ->
            assertEquals(parametrosDeEstado(estado), parametrosDeEstado(estado))
        }
    }

    @Test
    fun `PREOCUPADO tiene ojos bien abiertos y antenas caidas, segun la ficha`() {
        val p = parametrosDeEstado(EstadoChirri.PREOCUPADO)
        assertEquals(1f, p.aperturaOjos, 0.001f)
        assertTrue("la antena izquierda deberia estar caida (angulo negativo)", p.anguloAntenaIzquierda < 0f)
        assertTrue("la antena derecha deberia estar caida (angulo negativo)", p.anguloAntenaDerecha < 0f)
        assertTrue("la boca deberia leer como preocupada, no como sonrisa", p.curvaBoca < 0f)
        assertFalse("preocupado no lleva chispa de celebracion", p.mostrarAcento)
    }

    @Test
    fun `CELEBRANDO muestra la chispa de acento en ambar farol`() {
        val p = parametrosDeEstado(EstadoChirri.CELEBRANDO)
        assertTrue(p.mostrarAcento)
        assertEquals(ColorChirri.AMBAR_FAROL, p.colorAcento)
        assertTrue("celebrando deberia tener la sonrisa mas grande de los cinco estados", p.curvaBoca > 0.9f)
    }

    @Test
    fun `SALUDANDO levanta mas una antena que la otra, como un saludo`() {
        val p = parametrosDeEstado(EstadoChirri.SALUDANDO)
        assertTrue(p.anguloAntenaIzquierda != p.anguloAntenaDerecha)
        assertTrue(p.mostrarAcento)
        assertEquals(ColorChirri.ROSA_BERENJENA, p.colorAcento)
    }

    @Test
    fun `ANIMANDO no muestra acento, mantiene el foco en la postura`() {
        val p = parametrosDeEstado(EstadoChirri.ANIMANDO)
        assertFalse(p.mostrarAcento)
        assertTrue(p.curvaBoca > 0f)
        assertEquals(p.anguloAntenaIzquierda, p.anguloAntenaDerecha, 0.001f)
    }

    @Test
    fun `NEUTRAL es el estado mas neutro de sonrisa y sin acento`() {
        val p = parametrosDeEstado(EstadoChirri.NEUTRAL)
        assertFalse(p.mostrarAcento)
        assertTrue(p.curvaBoca in 0f..0.3f)
    }

    @Test
    fun `cada estado tiene una descripcion no vacia y distinta, para el contentDescription`() {
        val descripciones = EstadoChirri.entries.map { parametrosDeEstado(it).descripcion }
        descripciones.forEach { assertTrue(it.isNotBlank()) }
        assertEquals("las 5 descripciones deberian ser todas distintas", 5, descripciones.toSet().size)
    }

    @Test
    fun `PREOCUPADO es el unico estado con los ojos completamente abiertos`() {
        val otros = EstadoChirri.entries.filter { it != EstadoChirri.PREOCUPADO }
        otros.forEach { estado ->
            assertTrue(
                "el estado $estado no deberia tener apertura de ojos maxima como PREOCUPADO",
                parametrosDeEstado(estado).aperturaOjos < 1f
            )
        }
    }
}
