package pe.appmobile.laplaza.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.appmobile.laplaza.domain.model.BloqueDiscurso
import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.Franja
import pe.appmobile.laplaza.domain.model.ResultadoValidacion

class MotorDiscursoTest {

    private fun bloque(id: Long, temaId: Long, franja: Franja) =
        BloqueDiscurso(id, temaId, franja, "texto $id")

    @Test
    fun `discurso con gancho, cuerpo y cierre en orden es valido`() {
        val discurso = DiscursoArmado(
            temaId = 1L,
            bloques = listOf(
                bloque(1, 1L, Franja.GANCHO),
                bloque(2, 1L, Franja.CUERPO),
                bloque(3, 1L, Franja.CIERRE)
            )
        )
        assertEquals(ResultadoValidacion.Valido, MotorDiscurso.validar(discurso))
    }

    @Test
    fun `discurso vacio es invalido`() {
        val discurso = DiscursoArmado(temaId = 1L, bloques = emptyList())
        assertTrue(MotorDiscurso.validar(discurso) is ResultadoValidacion.Invalido)
    }

    @Test
    fun `discurso con el cierre primero es invalido`() {
        val discurso = DiscursoArmado(
            temaId = 1L,
            bloques = listOf(
                bloque(1, 1L, Franja.CIERRE),
                bloque(2, 1L, Franja.CUERPO),
                bloque(3, 1L, Franja.GANCHO)
            )
        )
        assertTrue(MotorDiscurso.validar(discurso) is ResultadoValidacion.Invalido)
    }

    @Test
    fun `discurso con un bloque de otro tema es invalido`() {
        val discurso = DiscursoArmado(
            temaId = 1L,
            bloques = listOf(
                bloque(1, 1L, Franja.GANCHO),
                bloque(2, 2L, Franja.CUERPO),
                bloque(3, 1L, Franja.CIERRE)
            )
        )
        assertTrue(MotorDiscurso.validar(discurso) is ResultadoValidacion.Invalido)
    }

    @Test
    fun `discurso con solo dos bloques (falta cierre) es invalido`() {
        val discurso = DiscursoArmado(
            temaId = 1L,
            bloques = listOf(
                bloque(1, 1L, Franja.GANCHO),
                bloque(2, 1L, Franja.CUERPO)
            )
        )
        assertTrue(MotorDiscurso.validar(discurso) is ResultadoValidacion.Invalido)
    }

    @Test
    fun `discurso con dos ganchos y un cierre (franja repetida) es invalido`() {
        val discurso = DiscursoArmado(
            temaId = 1L,
            bloques = listOf(
                bloque(1, 1L, Franja.GANCHO),
                bloque(2, 1L, Franja.GANCHO),
                bloque(3, 1L, Franja.CIERRE)
            )
        )
        assertTrue(MotorDiscurso.validar(discurso) is ResultadoValidacion.Invalido)
    }
}
