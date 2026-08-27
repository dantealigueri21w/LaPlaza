package pe.appmobile.laplaza.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.appmobile.laplaza.domain.model.Pausa

class MotorFluidezTest {

    @Test
    fun `sin pausas la fluidez es 1`() {
        assertEquals(1f, MotorFluidez.calcularFluidez(emptyList(), 5000L), 0.001f)
    }

    @Test
    fun `una sola pausa limpia (entre 200 y 900 ms) no penaliza`() {
        val pausas = listOf(Pausa(1000L, 400L))
        assertEquals(1f, MotorFluidez.calcularFluidez(pausas, 5000L), 0.001f)
    }

    @Test
    fun `una pausa demasiado corta penaliza un poco`() {
        val pausas = listOf(Pausa(1000L, 60L))
        assertTrue(MotorFluidez.calcularFluidez(pausas, 5000L) < 1f)
    }

    @Test
    fun `varios cortes agrupados (titubeo) penalizan mas que una pausa limpia aislada`() {
        val titubeo = listOf(Pausa(1000L, 60L), Pausa(1200L, 60L), Pausa(1400L, 60L))
        val limpia = listOf(Pausa(1000L, 400L))
        val fluidezTitubeo = MotorFluidez.calcularFluidez(titubeo, 5000L)
        val fluidezLimpia = MotorFluidez.calcularFluidez(limpia, 5000L)
        assertTrue(fluidezTitubeo < fluidezLimpia)
    }

    @Test
    fun `agruparPausasCercanas agrupa pausas separadas por menos de la ventana`() {
        val pausas = listOf(Pausa(1000L, 60L), Pausa(1200L, 60L))
        val grupos = MotorFluidez.agruparPausasCercanas(pausas, ventanaMs = 1500L)
        assertEquals(1, grupos.size)
        assertEquals(2, grupos[0].size)
    }

    @Test
    fun `agruparPausasCercanas separa en grupos distintos pausas lejanas`() {
        val pausas = listOf(Pausa(1000L, 60L), Pausa(5000L, 60L))
        val grupos = MotorFluidez.agruparPausasCercanas(pausas, ventanaMs = 1500L)
        assertEquals(2, grupos.size)
    }

    @Test
    fun `duracion total cero no revienta y da cero`() {
        assertEquals(0f, MotorFluidez.calcularFluidez(listOf(Pausa(0L, 100L)), 0L), 0.001f)
    }

    // Bug real (27/08/2026): Rodrigo se quedo en silencio total durante una declamacion real
    // en su celular y la app le dijo "Dijiste... sin cortes, de principio a fin" -- el mismo
    // caso se reprodujo en el emulador de la Fase 2 (grabacion con -no-audio, silencio de
    // principio a fin). La causa: detectarPausas agrupa todo el silencio en una sola Pausa
    // que dura la grabacion entera, y antes de esta correccion esa unica pausa "sucia" solo
    // penalizaba 0.05 sin importar su duracion real, dejando fluidez en ~0.95 (por encima del
    // umbral de 0.9 que otorga la insignia Sin Cortes).
    @Test
    fun `silencio total durante toda la declamacion NO cuenta como fluidez alta`() {
        val silencioCompleto = listOf(Pausa(0L, 8000L))
        val fluidez = MotorFluidez.calcularFluidez(silencioCompleto, 8000L)
        assertTrue("silencio total dio fluidez=$fluidez, deberia ser muy baja", fluidez < 0.2f)
    }

    @Test
    fun `un discurso a medias (silencio en la mayor parte) da fluidez baja, no alta`() {
        // Los primeros 2s con voz (sin pausa registrada ahi), los ultimos 6s de silencio total.
        val discursoIncompleto = listOf(Pausa(2000L, 6000L))
        val fluidez = MotorFluidez.calcularFluidez(discursoIncompleto, 8000L)
        assertTrue("discurso a medias dio fluidez=$fluidez, deberia penalizar fuerte", fluidez < 0.5f)
    }

    @Test
    fun `una pausa limpia aislada sigue sin penalizar, sin importar la correccion del silencio total`() {
        // No debe romper el caso normal ya cubierto arriba: una pausa real de respiracion
        // (200-900ms) dentro de un intento normal sigue en fluidez 1.
        val pausas = listOf(Pausa(3000L, 700L))
        assertEquals(1f, MotorFluidez.calcularFluidez(pausas, 8000L), 0.001f)
    }
}
