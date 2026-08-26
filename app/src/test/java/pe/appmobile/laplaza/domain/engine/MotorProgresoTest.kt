package pe.appmobile.laplaza.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.appmobile.laplaza.domain.model.IdRincon

class MotorProgresoTest {

    @Test
    fun `al inicio solo Balcon y Libre estan desbloqueados`() {
        val desbloqueados = MotorProgreso.rinconesDesbloqueados(emptySet())
        assertEquals(setOf(IdRincon.BALCON, IdRincon.LIBRE), desbloqueados)
    }

    @Test
    fun `completar Balcon desbloquea Kiosco`() {
        val desbloqueados = MotorProgreso.rinconesDesbloqueados(setOf(IdRincon.BALCON))
        assertTrue(IdRincon.KIOSCO in desbloqueados)
    }

    @Test
    fun `completar Mostrador desbloquea Jardin y Fuente a la vez`() {
        val completados = setOf(IdRincon.BALCON, IdRincon.KIOSCO, IdRincon.MOSTRADOR)
        val desbloqueados = MotorProgreso.rinconesDesbloqueados(completados)
        assertTrue(IdRincon.JARDIN in desbloqueados)
        assertTrue(IdRincon.FUENTE in desbloqueados)
    }

    @Test
    fun `completar solo Jardin ya desbloquea Mirador`() {
        val completados = setOf(IdRincon.BALCON, IdRincon.KIOSCO, IdRincon.MOSTRADOR, IdRincon.JARDIN)
        val desbloqueados = MotorProgreso.rinconesDesbloqueados(completados)
        assertTrue(IdRincon.MIRADOR in desbloqueados)
    }

    @Test
    fun `completar los seis rincones anteriores desbloquea la Tarima Mayor`() {
        val completados = setOf(
            IdRincon.BALCON, IdRincon.KIOSCO, IdRincon.MOSTRADOR,
            IdRincon.JARDIN, IdRincon.FUENTE, IdRincon.MIRADOR
        )
        val desbloqueados = MotorProgreso.rinconesDesbloqueados(completados)
        assertTrue(IdRincon.TARIMA_MAYOR in desbloqueados)
    }

    @Test
    fun `la racha cuenta dias consecutivos terminando hoy`() {
        val hoy = 20000L
        val fechas = listOf(hoy, hoy - 1, hoy - 2)
        assertEquals(3, MotorProgreso.calcularRacha(fechas, hoy))
    }

    @Test
    fun `la racha se corta si falta un dia intermedio`() {
        val hoy = 20000L
        val fechas = listOf(hoy, hoy - 2)
        assertEquals(1, MotorProgreso.calcularRacha(fechas, hoy))
    }

    @Test
    fun `sugerirRepaso recomienda el tema con menor puntaje reciente`() {
        val intentos = listOf(
            Triple(1L, "Preséntate a la plaza", 0.9f),
            Triple(2L, "Pide algo con buenas razones", 0.3f),
            Triple(3L, "Cuenta algo que pasó", 0.7f)
        )
        val sugerencia = MotorProgreso.sugerirRepaso(intentos)
        assertEquals(2L, sugerencia?.temaId)
    }

    @Test
    fun `sugerirRepaso con lista vacia da null`() {
        assertNull(MotorProgreso.sugerirRepaso(emptyList()))
    }
}
