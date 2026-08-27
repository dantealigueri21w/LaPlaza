package pe.appmobile.laplaza.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.appmobile.laplaza.domain.model.IdRincon
import pe.appmobile.laplaza.domain.model.IntentoParaInsignias

/**
 * Las 12 insignias de la ficha (24-LA-PLAZA.md, seccion "Insignias"), evaluadas desde
 * cero sobre TODO el historial cada vez (nunca un contador incremental que se pueda
 * desincronizar de Room) -- ver el comentario de [MotorInsignias.insigniasGanadas].
 */
class MotorInsigniasTest {

    private fun intento(
        temaId: Long = 1L,
        rinconId: String = "BALCON",
        viaRinconLibre: Boolean = false,
        puntajeVolumen: Float = 0.3f,
        puntajeCompuesto: Float = 0.5f,
        puntajeFluidez: Float = 0.5f
    ) = IntentoParaInsignias(
        temaId = temaId,
        rinconId = rinconId,
        viaRinconLibre = viaRinconLibre,
        puntajeVolumen = puntajeVolumen,
        puntajeCompuesto = puntajeCompuesto,
        puntajeFluidez = puntajeFluidez
    )

    @Test
    fun `sin intentos, ninguna insignia`() {
        val ganadas = MotorInsignias.insigniasGanadas(emptyList(), rinconesCompletados = emptySet())
        assertTrue(ganadas.isEmpty())
    }

    @Test
    fun `un solo intento real gana Primera Voz`() {
        val ganadas = MotorInsignias.insigniasGanadas(listOf(intento()), rinconesCompletados = emptySet())
        assertTrue("PRIMERA_VOZ" in ganadas)
    }

    @Test
    fun `un intento solo via Rincon Libre NO gana Primera Voz`() {
        val ganadas = MotorInsignias.insigniasGanadas(listOf(intento(viaRinconLibre = true)), rinconesCompletados = emptySet())
        assertFalse("PRIMERA_VOZ" in ganadas)
    }

    @Test
    fun `4 gancho fuertes todavia no alcanzan Gancho que Prende, el quinto si`() {
        val cuatro = List(4) { intento(temaId = it.toLong(), puntajeVolumen = 0.85f) }
        assertFalse("GANCHO_QUE_PRENDE" in MotorInsignias.insigniasGanadas(cuatro, emptySet()))

        val cinco = List(5) { intento(temaId = it.toLong(), puntajeVolumen = 0.85f) }
        assertTrue("GANCHO_QUE_PRENDE" in MotorInsignias.insigniasGanadas(cinco, emptySet()))
    }

    @Test
    fun `un gancho debil no cuenta para Gancho que Prende`() {
        val cinco = List(5) { intento(temaId = it.toLong(), puntajeVolumen = 0.4f) }
        assertFalse("GANCHO_QUE_PRENDE" in MotorInsignias.insigniasGanadas(cinco, emptySet()))
    }

    @Test
    fun `2 aplausos altos todavia no alcanzan Cierre de Aplausos, el tercero si`() {
        val dos = List(2) { intento(temaId = it.toLong(), puntajeCompuesto = 0.8f) }
        assertFalse("CIERRE_DE_APLAUSOS" in MotorInsignias.insigniasGanadas(dos, emptySet()))

        val tres = List(3) { intento(temaId = it.toLong(), puntajeCompuesto = 0.8f) }
        assertTrue("CIERRE_DE_APLAUSOS" in MotorInsignias.insigniasGanadas(tres, emptySet()))
    }

    @Test
    fun `un solo intento con fluidez muy alta gana Sin Cortes`() {
        val ganadas = MotorInsignias.insigniasGanadas(listOf(intento(puntajeFluidez = 0.95f)), rinconesCompletados = emptySet())
        assertTrue("SIN_CORTES" in ganadas)
    }

    @Test
    fun `fluidez mediana no gana Sin Cortes`() {
        val ganadas = MotorInsignias.insigniasGanadas(listOf(intento(puntajeFluidez = 0.6f)), rinconesCompletados = emptySet())
        assertFalse("SIN_CORTES" in ganadas)
    }

    @Test
    fun `los 3 temas de El Balcon con intento ganan Balcon Dominado, con solo 2 no`() {
        val dosTemas = listOf(
            intento(temaId = 1L, rinconId = "BALCON"),
            intento(temaId = 2L, rinconId = "BALCON")
        )
        assertFalse("BALCON_DOMINADO" in MotorInsignias.insigniasGanadas(dosTemas, emptySet()))

        val tresTemas = dosTemas + intento(temaId = 3L, rinconId = "BALCON")
        assertTrue("BALCON_DOMINADO" in MotorInsignias.insigniasGanadas(tresTemas, emptySet()))
    }

    @Test
    fun `rincon completado otorga su insignia de completar`() {
        val mapa = mapOf(
            IdRincon.KIOSCO to "VOZ_DEL_KIOSCO",
            IdRincon.MOSTRADOR to "BUEN_VENDEDOR",
            IdRincon.MIRADOR to "SE_OYO_EN_TODA_LA_PLAZA",
            IdRincon.JARDIN to "GRATITUD_SINCERA",
            IdRincon.FUENTE to "BUEN_CONSUELO",
            IdRincon.TARIMA_MAYOR to "LA_TARIMA_ES_TUYA"
        )
        mapa.forEach { (rincon, insigniaId) ->
            val ganadas = MotorInsignias.insigniasGanadas(emptyList(), rinconesCompletados = setOf(rincon))
            assertTrue("$rincon deberia otorgar $insigniaId", insigniaId in ganadas)
        }
    }

    @Test
    fun `sin ese rincon completado, su insignia no se otorga`() {
        val ganadas = MotorInsignias.insigniasGanadas(emptyList(), rinconesCompletados = emptySet())
        assertFalse("VOZ_DEL_KIOSCO" in ganadas)
    }

    @Test
    fun `9 usos de Rincon Libre todavia no alcanzan Companero de Chirri, el decimo si`() {
        val nueve = List(9) { intento(temaId = it.toLong(), viaRinconLibre = true) }
        assertFalse("COMPANERO_DE_CHIRRI" in MotorInsignias.insigniasGanadas(nueve, emptySet()))

        val diez = List(10) { intento(temaId = it.toLong(), viaRinconLibre = true) }
        assertTrue("COMPANERO_DE_CHIRRI" in MotorInsignias.insigniasGanadas(diez, emptySet()))
    }

    @Test
    fun `un intento normal aislado no otorga nada mas que Primera Voz`() {
        val ganadas = MotorInsignias.insigniasGanadas(listOf(intento()), rinconesCompletados = emptySet())
        assertEquals(setOf("PRIMERA_VOZ"), ganadas)
    }
}
