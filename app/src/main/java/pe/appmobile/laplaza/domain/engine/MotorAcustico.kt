package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.MuestraAcustica
import pe.appmobile.laplaza.domain.model.Pausa
import kotlin.math.sqrt

object MotorAcustico {

    const val UMBRAL_RMS_VOZ = 0.02f

    private const val UMBRAL_YIN = 0.15
    private const val F0_MIN_HZ = 70.0
    private const val F0_MAX_HZ = 400.0

    fun calcularRms(buffer: ShortArray): Float {
        if (buffer.isEmpty()) return 0f
        var suma = 0.0
        for (muestra in buffer) {
            val normalizada = muestra / 32768.0
            suma += normalizada * normalizada
        }
        return sqrt(suma / buffer.size).toFloat()
    }

    fun calcularF0(buffer: ShortArray, sampleRate: Int): Float {
        val tauMax = minOf((sampleRate / F0_MIN_HZ).toInt(), buffer.size / 2)
        val tauMin = maxOf((sampleRate / F0_MAX_HZ).toInt(), 2)
        if (buffer.size < 64 || tauMax <= tauMin) return 0f

        val diferencia = DoubleArray(tauMax + 1)
        for (tau in 1..tauMax) {
            var suma = 0.0
            for (j in 0 until buffer.size - tau) {
                val delta = (buffer[j] - buffer[j + tau]).toDouble()
                suma += delta * delta
            }
            diferencia[tau] = suma
        }

        val diferenciaNormalizada = DoubleArray(tauMax + 1)
        diferenciaNormalizada[0] = 1.0
        var acumulado = 0.0
        for (tau in 1..tauMax) {
            acumulado += diferencia[tau]
            diferenciaNormalizada[tau] = if (acumulado == 0.0) 1.0 else diferencia[tau] * tau / acumulado
        }

        for (tau in tauMin..tauMax) {
            if (diferenciaNormalizada[tau] < UMBRAL_YIN) {
                var mejorTau = tau
                while (mejorTau + 1 <= tauMax &&
                    diferenciaNormalizada[mejorTau + 1] < diferenciaNormalizada[mejorTau]
                ) {
                    mejorTau++
                }
                return sampleRate.toFloat() / mejorTau.toFloat()
            }
        }
        return 0f
    }

    fun analizarVentana(
        buffer: ShortArray,
        sampleRate: Int,
        umbralRms: Float = UMBRAL_RMS_VOZ
    ): MuestraAcustica {
        val rms = calcularRms(buffer)
        val esVoz = rms >= umbralRms
        val f0 = if (esVoz) calcularF0(buffer, sampleRate) else 0f
        return MuestraAcustica(rms = rms, f0Hz = f0, esVoz = esVoz)
    }

    fun detectarPausas(
        muestras: List<MuestraAcustica>,
        duracionVentanaMs: Long,
        umbralPausaMs: Long
    ): List<Pausa> {
        if (muestras.isEmpty()) return emptyList()
        val pausas = mutableListOf<Pausa>()
        var inicioSilencioIdx = -1
        for (i in muestras.indices) {
            if (!muestras[i].esVoz) {
                if (inicioSilencioIdx == -1) inicioSilencioIdx = i
            } else if (inicioSilencioIdx != -1) {
                val duracion = (i - inicioSilencioIdx) * duracionVentanaMs
                if (duracion >= umbralPausaMs) {
                    pausas.add(Pausa(inicioSilencioIdx * duracionVentanaMs, duracion))
                }
                inicioSilencioIdx = -1
            }
        }
        if (inicioSilencioIdx != -1) {
            val duracion = (muestras.size - inicioSilencioIdx) * duracionVentanaMs
            if (duracion >= umbralPausaMs) {
                pausas.add(Pausa(inicioSilencioIdx * duracionVentanaMs, duracion))
            }
        }
        return pausas
    }

    fun contarSilabas(muestras: List<MuestraAcustica>): Int {
        var conteo = 0
        for (i in 1 until muestras.size) {
            if (muestras[i].esVoz && !muestras[i - 1].esVoz) conteo++
        }
        if (muestras.isNotEmpty() && muestras[0].esVoz) conteo++
        return conteo
    }

    fun calcularRitmoSilabasPorMinuto(silabas: Int, duracionTotalMs: Long): Float {
        if (duracionTotalMs <= 0L) return 0f
        return silabas * 60_000f / duracionTotalMs
    }
}
