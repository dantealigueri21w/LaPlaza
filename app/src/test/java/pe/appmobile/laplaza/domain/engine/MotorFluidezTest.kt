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
}
