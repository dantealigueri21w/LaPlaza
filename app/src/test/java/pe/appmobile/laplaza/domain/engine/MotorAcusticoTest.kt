package pe.appmobile.laplaza.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.appmobile.laplaza.domain.model.MuestraAcustica
import kotlin.math.PI
import kotlin.math.sin

class MotorAcusticoTest {

    private fun generarSenoidal(
        frecuenciaHz: Double,
        sampleRate: Int,
        duracionMs: Int,
        amplitud: Double = 0.8
    ): ShortArray {
        val totalMuestras = sampleRate * duracionMs / 1000
        return ShortArray(totalMuestras) { i ->
            val t = i.toDouble() / sampleRate
            (amplitud * Short.MAX_VALUE * sin(2 * PI * frecuenciaHz * t)).toInt().toShort()
        }
    }

    private fun generarSilencio(sampleRate: Int, duracionMs: Int): ShortArray =
        ShortArray(sampleRate * duracionMs / 1000)

    @Test
    fun `calcularRms de silencio total da cero`() {
        val buffer = generarSilencio(16000, 100)
        assertEquals(0f, MotorAcustico.calcularRms(buffer), 0.001f)
    }

    @Test
    fun `calcularRms de una onda a amplitud maxima da cerca de 0,70 (RMS de una senoidal pura)`() {
        val buffer = generarSenoidal(220.0, 16000, 100, amplitud = 1.0)
        val rms = MotorAcustico.calcularRms(buffer)
        assertTrue("RMS esperado entre 0.65 y 0.75, fue $rms", rms in 0.65f..0.75f)
    }

    @Test
    fun `calcularRms de una onda a media amplitud da menos que a amplitud maxima`() {
        val bufferBajo = generarSenoidal(220.0, 16000, 100, amplitud = 0.3)
        val bufferAlto = generarSenoidal(220.0, 16000, 100, amplitud = 1.0)
        assertTrue(MotorAcustico.calcularRms(bufferBajo) < MotorAcustico.calcularRms(bufferAlto))
    }

    @Test
    fun `calcularRms de un buffer vacio no revienta y da cero`() {
        assertEquals(0f, MotorAcustico.calcularRms(ShortArray(0)), 0.001f)
    }

    @Test
    fun `calcularF0 detecta una senoidal de 220 Hz dentro de un margen de 10 Hz`() {
        val buffer = generarSenoidal(220.0, 16000, 100)
        val f0 = MotorAcustico.calcularF0(buffer, 16000)
        assertTrue("F0 esperado cerca de 220 Hz, fue $f0", f0 in 210f..230f)
    }

    @Test
    fun `calcularF0 detecta una senoidal de 110 Hz dentro de un margen de 10 Hz`() {
        val buffer = generarSenoidal(110.0, 16000, 100)
        val f0 = MotorAcustico.calcularF0(buffer, 16000)
        assertTrue("F0 esperado cerca de 110 Hz, fue $f0", f0 in 100f..120f)
    }

    @Test
    fun `calcularF0 de silencio da cero`() {
        val buffer = generarSilencio(16000, 100)
        assertEquals(0f, MotorAcustico.calcularF0(buffer, 16000), 0.001f)
    }

    @Test
    fun `calcularF0 de un buffer demasiado corto no revienta y da cero`() {
        val buffer = shortArrayOf(1, 2, 3)
        assertEquals(0f, MotorAcustico.calcularF0(buffer, 16000), 0.001f)
    }

    @Test
    fun `analizarVentana marca esVoz verdadero cuando el RMS supera el umbral`() {
        val buffer = generarSenoidal(220.0, 16000, 50, amplitud = 0.8)
        val muestra = MotorAcustico.analizarVentana(buffer, 16000)
        assertTrue(muestra.esVoz)
        assertTrue(muestra.f0Hz > 0f)
    }

    @Test
    fun `analizarVentana marca esVoz falso en silencio`() {
        val buffer = generarSilencio(16000, 50)
        val muestra = MotorAcustico.analizarVentana(buffer, 16000)
        assertEquals(false, muestra.esVoz)
        assertEquals(0f, muestra.f0Hz, 0.001f)
    }

    @Test
    fun `analizarVentana respeta un umbral de calibracion distinto al por defecto`() {
        // ruido de fondo alto: con el umbral por defecto contaria como voz, con uno
        // calibrado a esta sesion (mas exigente) no deberia contar como voz
        val buffer = generarSenoidal(220.0, 16000, 50, amplitud = 0.05)
        val conUmbralPorDefecto = MotorAcustico.analizarVentana(buffer, 16000)
        val conUmbralCalibrado = MotorAcustico.analizarVentana(buffer, 16000, umbralRms = 0.5f)
        assertTrue(conUmbralPorDefecto.esVoz)
        assertEquals(false, conUmbralCalibrado.esVoz)
    }

    @Test
    fun `calcularRitmoSilabasPorMinuto convierte silabas y duracion a una tasa por minuto`() {
        val ritmo = MotorAcustico.calcularRitmoSilabasPorMinuto(silabas = 10, duracionTotalMs = 5000L)
        assertEquals(120f, ritmo, 0.01f)
    }

    @Test
    fun `calcularRitmoSilabasPorMinuto con duracion cero no revienta y da cero`() {
        assertEquals(0f, MotorAcustico.calcularRitmoSilabasPorMinuto(silabas = 5, duracionTotalMs = 0L), 0.001f)
    }

    @Test
    fun `detectarPausas encuentra una pausa cuando hay una racha de silencio mayor al umbral`() {
        val muestras = listOf(
            MuestraAcustica(0.5f, 200f, true),
            MuestraAcustica(0f, 0f, false),
            MuestraAcustica(0f, 0f, false),
            MuestraAcustica(0f, 0f, false),
            MuestraAcustica(0.5f, 200f, true)
        )
        val pausas = MotorAcustico.detectarPausas(muestras, duracionVentanaMs = 30L, umbralPausaMs = 60L)
        assertEquals(1, pausas.size)
        assertEquals(90L, pausas[0].duracionMs)
    }

    @Test
    fun `detectarPausas no cuenta una racha corta bajo el umbral`() {
        val muestras = listOf(
            MuestraAcustica(0.5f, 200f, true),
            MuestraAcustica(0f, 0f, false),
            MuestraAcustica(0.5f, 200f, true)
        )
        val pausas = MotorAcustico.detectarPausas(muestras, duracionVentanaMs = 30L, umbralPausaMs = 60L)
        assertTrue(pausas.isEmpty())
    }

    @Test
    fun `detectarPausas con lista vacia no revienta y da lista vacia`() {
        assertTrue(MotorAcustico.detectarPausas(emptyList(), 30L, 60L).isEmpty())
    }

    @Test
    fun `contarSilabas cuenta transiciones de silencio a voz como picos`() {
        val muestras = listOf(
            MuestraAcustica(0.5f, 200f, true),
            MuestraAcustica(0f, 0f, false),
            MuestraAcustica(0.5f, 200f, true),
            MuestraAcustica(0.5f, 200f, true),
            MuestraAcustica(0f, 0f, false),
            MuestraAcustica(0.5f, 200f, true)
        )
        assertEquals(3, MotorAcustico.contarSilabas(muestras))
    }
}
