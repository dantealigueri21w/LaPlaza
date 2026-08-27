package pe.appmobile.laplaza.data.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas puras (sin Room, sin Robolectric) sobre la FORMA de los datos semilla: cantidades
 * exactas y consistencia entre SemillaTemas y SemillaBloques. El contenido en si (los textos
 * reales en español) se ejercita indirectamente en LaPlazaRepositoryTest al sembrar sobre
 * una base real.
 */
class SemillaTest {

    @Test
    fun `hay exactamente 7 rincones sembrados y ninguno es LIBRE`() {
        assertEquals(7, SemillaRincones.rincones.size)
        assertTrue("LIBRE no debe tener fila propia: se maneja aparte", SemillaRincones.rincones.none { it.id == "LIBRE" })
    }

    @Test
    fun `hay exactamente 21 temas, 3 por cada uno de los 7 rincones`() {
        assertEquals(21, SemillaTemas.temas.size)
        val porRincon = SemillaTemas.temas.groupBy { it.rinconId }
        assertEquals(7, porRincon.size)
        porRincon.values.forEach { temasDelRincon -> assertEquals(3, temasDelRincon.size) }
    }

    @Test
    fun `cada tema tiene una clave unica con su entrada correspondiente en SemillaBloques`() {
        val claves = SemillaTemas.temas.map { it.clave }
        assertEquals("no debe haber claves repetidas", claves.size, claves.toSet().size)
        claves.forEach { clave ->
            assertTrue("Falta SemillaBloques.porTema[$clave]", SemillaBloques.porTema.containsKey(clave))
        }
    }

    @Test
    fun `hay exactamente 189 bloques en total, 9 por tema con 3 de cada franja`() {
        assertEquals(21, SemillaBloques.porTema.size)
        assertEquals(189, SemillaBloques.porTema.values.sumOf { it.size })
        SemillaBloques.porTema.forEach { (clave, bloques) ->
            assertEquals("$clave debe tener 9 bloques", 9, bloques.size)
            assertEquals("$clave debe tener 3 GANCHO", 3, bloques.count { it.franja == "GANCHO" })
            assertEquals("$clave debe tener 3 CUERPO", 3, bloques.count { it.franja == "CUERPO" })
            assertEquals("$clave debe tener 3 CIERRE", 3, bloques.count { it.franja == "CIERRE" })
            // orden 1..3 dentro de cada franja, sin repetir
            listOf("GANCHO", "CUERPO", "CIERRE").forEach { franja ->
                val ordenes = bloques.filter { it.franja == franja }.map { it.orden }.sorted()
                assertEquals(listOf(1, 2, 3), ordenes)
            }
        }
    }

    @Test
    fun `La Tarima Mayor es MEDIO-DIFICIL-DIFICIL a proposito, ningun tema facil en el capstone`() {
        val tarima = SemillaTemas.temas.filter { it.rinconId == "TARIMA_MAYOR" }.sortedBy { it.orden }
        assertEquals(3, tarima.size)
        assertEquals(listOf("MEDIO", "DIFICIL", "DIFICIL"), tarima.map { it.dificultad })
    }

    @Test
    fun `los otros 6 rincones son FACIL-MEDIO-DIFICIL en orden`() {
        val otros = listOf("BALCON", "KIOSCO", "MOSTRADOR", "JARDIN", "FUENTE", "MIRADOR")
        otros.forEach { rinconId ->
            val temas = SemillaTemas.temas.filter { it.rinconId == rinconId }.sortedBy { it.orden }
            assertEquals(rinconId, listOf("FACIL", "MEDIO", "DIFICIL"), temas.map { it.dificultad })
        }
    }

    @Test
    fun `hay exactamente 12 insignias`() {
        assertEquals(12, SemillaInsignias.insignias.size)
    }

    @Test
    fun `las frases de Chirri suman 30, 10 por categoria`() {
        assertEquals(10, FrasesChirri.avisos.size)
        assertEquals(10, FrasesChirri.animo.size)
        assertEquals(10, FrasesChirri.celebraciones.size)
    }

    @Test
    fun `hay 8 avatares disponibles, del 1 al 8`() {
        assertEquals((1..8).toList(), Avatares.idsDisponibles)
    }
}
