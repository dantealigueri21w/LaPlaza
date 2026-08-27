package pe.appmobile.laplaza.audio

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.appmobile.laplaza.domain.engine.MotorAcustico
import pe.appmobile.laplaza.domain.model.NivelAtencion
import kotlin.math.PI
import kotlin.math.sin

/**
 * Pruebas de orquestación de [CapturadorVoz] contra una [FuenteDeAudio] falsa: nada de
 * Robolectric ni de `AudioRecord` real, igual que las pruebas de los motores de dominio de
 * los que depende (`MotorAcusticoTest`, del mismo estilo de buffers sintéticos).
 *
 * Truco de concurrencia: se inyecta [Dispatchers.Unconfined] como dispatcher. Como
 * [FuenteDeAudio.leerSiguienteBloque] no es una función `suspend` (bloquea, igual que
 * `AudioRecord.read`), el cuerpo de la corrutina de [CapturadorVoz.iniciar] no tiene ningún
 * punto de suspensión real -así que con `Unconfined` corre de punta a punta, de forma
 * síncrona, dentro de la misma llamada a `iniciar()`. Eso evita necesitar
 * `kotlinx-coroutines-test` para pilotear la captura bloque a bloque en las pruebas.
 */
class CapturadorVozTest {

    private val sampleRate = 16000

    private fun generarSenoidal(frecuenciaHz: Double, duracionMs: Int, amplitud: Double): ShortArray {
        val totalMuestras = sampleRate * duracionMs / 1000
        return ShortArray(totalMuestras) { i ->
            val t = i.toDouble() / sampleRate
            (amplitud * Short.MAX_VALUE * sin(2 * PI * frecuenciaHz * t)).toInt().toShort()
        }
    }

    private fun generarSilencioDigital(duracionMs: Int): ShortArray = ShortArray(sampleRate * duracionMs / 1000)

    private fun nuevoCapturador(
        bloques: List<ShortArray>,
        duracionBloqueMs: Long = 100L,
        duracionCalibracionMs: Long = 200L
    ): Pair<CapturadorVoz, FuenteDeAudioFalsa> {
        val fuente = FuenteDeAudioFalsa(sampleRate, bloques.toMutableList())
        val capturador = CapturadorVoz(
            fuente = fuente,
            scope = CoroutineScope(Job()),
            dispatcher = Dispatchers.Unconfined,
            duracionBloqueMs = duracionBloqueMs,
            duracionCalibracionMs = duracionCalibracionMs
        )
        return capturador to fuente
    }

    // ---------- Estado inicial ----------

    @Test
    fun `el estado antes de iniciar es SinIniciar`() {
        val (capturador, _) = nuevoCapturador(emptyList())
        assertEquals(EstadoCapturaEnVivo.SinIniciar, capturador.estado.value)
    }

    // ---------- Calibración ----------

    @Test
    fun `la calibracion deriva un umbral mayor que cero y menor que una voz real fuerte`() {
        val ruido = List(2) { generarSenoidal(60.0, 100, amplitud = 0.02) } // ruido de fondo, no silencio digital puro
        val vozFuerte = generarSenoidal(220.0, 100, amplitud = 0.8)
        val (capturador, _) = nuevoCapturador(ruido + listOf(vozFuerte))

        capturador.iniciar()

        val estado = capturador.estado.value
        val umbral = (estado as? EstadoCapturaEnVivo.Detenido)?.umbralCalibrado
            ?: (estado as? EstadoCapturaEnVivo.Escuchando)?.umbralCalibrado
            ?: error("estado inesperado: $estado")
        assertTrue("el umbral calibrado debe ser mayor que cero, fue $umbral", umbral > 0f)
        assertTrue(
            "el umbral calibrado no debe tapar una voz real fuerte",
            umbral < MotorAcustico.calcularRms(vozFuerte)
        )
    }

    @Test
    fun `derivarUmbral con ruido vacio cae al umbral generico de MotorAcustico`() {
        val (capturador, _) = nuevoCapturador(emptyList())
        assertEquals(MotorAcustico.UMBRAL_RMS_VOZ, capturador.derivarUmbral(emptyList()), 0.0001f)
    }

    @Test
    fun `derivarUmbral queda por encima del pico de ruido observado`() {
        val (capturador, _) = nuevoCapturador(emptyList())
        val umbral = capturador.derivarUmbral(listOf(0.01f, 0.015f, 0.012f))
        assertTrue(umbral > 0.015f)
    }

    // ---------- Orquestación bloque a bloque ----------

    @Test
    fun `un bloque de voz mas fuerte da un volumen promedio mayor que uno mas suave`() {
        fun volumenTrasUnBloque(amplitud: Double): Float {
            val ruido = List(2) { generarSilencioDigital(100) }
            val voz = generarSenoidal(220.0, 100, amplitud)
            val (capturador, _) = nuevoCapturador(ruido + listOf(voz))
            capturador.iniciar()
            val estado = capturador.estado.value as EstadoCapturaEnVivo.Detenido
            return estado.resultado.acustico.volumenPromedio
        }

        val volumenBajo = volumenTrasUnBloque(0.1)
        val volumenAlto = volumenTrasUnBloque(0.9)

        assertTrue(
            "un bloque mas fuerte deberia dar mas volumen promedio: bajo=$volumenBajo alto=$volumenAlto",
            volumenAlto > volumenBajo
        )
    }

    @Test
    fun `una racha de silencio entre dos bloques de voz se registra como pausa`() {
        val ruidoCalibracion = List(2) { generarSilencioDigital(100) }
        val voz1 = generarSenoidal(220.0, 100, amplitud = 0.8)
        val hueco = List(3) { generarSilencioDigital(100) } // 300 ms >= 200 ms de umbral de pausa
        val voz2 = generarSenoidal(220.0, 100, amplitud = 0.8)
        val (capturador, _) = nuevoCapturador(ruidoCalibracion + listOf(voz1) + hueco + listOf(voz2))

        capturador.iniciar()

        val estado = capturador.estado.value as EstadoCapturaEnVivo.Detenido
        val pausas = estado.resultado.acustico.pausas
        assertTrue("debia detectarse al menos una pausa, pausas=$pausas", pausas.isNotEmpty())
        assertTrue(pausas.first().duracionMs >= 300L)
    }

    @Test
    fun `sin ningun bloque de voz el volumen y el ritmo quedan en cero y la audiencia esta distraida`() {
        val soloRuido = List(2) { generarSilencioDigital(100) } + List(3) { generarSilencioDigital(100) }
        val (capturador, _) = nuevoCapturador(soloRuido)

        capturador.iniciar()

        val estado = capturador.estado.value as EstadoCapturaEnVivo.Detenido
        // Nota: no se afirma que el puntaje compuesto sea cero. Un tramo entero de silencio se
        // lee, con la lógica ya probada de MotorFluidez, como una única "pausa limpia" (queda
        // dentro de su rango 200-900 ms) y no se penaliza -aporta 0.25 al compuesto por el peso
        // de la fluidez (0.25), aunque nunca hubo voz. Esa es la matemática real de los motores
        // de dominio, no algo que este orquestador deba corregir.
        assertEquals(0f, estado.resultado.acustico.volumenPromedio, 0.0001f)
        assertEquals(0f, estado.resultado.acustico.ritmoSilabasPorMinuto, 0.0001f)
        assertEquals(NivelAtencion.DISTRAIDA, estado.resultado.puntaje.nivel)
    }

    // ---------- detener() / liberación del recurso ----------

    @Test
    fun `detener libera la fuente aunque no se haya iniciado la captura`() {
        val (capturador, fuente) = nuevoCapturador(emptyList())

        capturador.detener()

        assertTrue(fuente.liberada)
    }

    @Test
    fun `llamar detener dos veces no revienta ni libera la fuente dos veces`() {
        val (capturador, fuente) = nuevoCapturador(emptyList())

        capturador.detener()
        capturador.detener()

        assertTrue(fuente.liberada)
    }

    @Test
    fun `al agotarse la fuente sola, la captura termina en Detenido y libera el recurso`() {
        val ruido = List(2) { generarSilencioDigital(100) }
        val voz = generarSenoidal(220.0, 100, amplitud = 0.8)
        val (capturador, fuente) = nuevoCapturador(ruido + listOf(voz))

        capturador.iniciar()

        assertTrue(capturador.estado.value is EstadoCapturaEnVivo.Detenido)
        assertTrue(fuente.liberada)
    }

    @Test
    fun `llamar detener despues de que la fuente ya se agoto sola no revienta`() {
        val ruido = List(2) { generarSilencioDigital(100) }
        val voz = generarSenoidal(220.0, 100, amplitud = 0.8)
        val (capturador, fuente) = nuevoCapturador(ruido + listOf(voz))

        capturador.iniciar() // agota la fuente y ya finaliza sola (ver la prueba anterior)
        capturador.detener() // no debe reventar ni volver a llamar a fuente.liberar()

        assertTrue(capturador.estado.value is EstadoCapturaEnVivo.Detenido)
        assertTrue(fuente.liberada)
    }

    /**
     * Reproduce, con hilos reales (no [Dispatchers.Unconfined]), el bug real encontrado
     * jugando la app en un emulador (seccion 10.3 del maestro): tocar "Terminar" no
     * hacia nada, una y otra vez. Causa real: [CapturadorVoz.detener] pone el estado en
     * `Detenido` desde el hilo llamador, pero si la corrutina de fondo ya estaba a mitad
     * de leer un bloque, termina esa lectura DESPUES y sobreescribe `Detenido` de vuelta
     * a `Escuchando` -- una carrera de dos escritores sobre el mismo StateFlow, invisible
     * en las pruebas de arriba porque todas usan `Dispatchers.Unconfined` (todo corre en
     * un solo hilo, sin carrera posible).
     */
    @Test
    fun `detener mientras un bloque esta a mitad de lectura no deja que ese bloque revierta el estado a Escuchando`() {
        val leyendoElBloqueEnVuelo = CountDownLatch(1)
        val puedeTerminarDeLeerlo = CountDownLatch(1)
        val fuente = FuenteDeAudioQueBloqueaEnLaSegundaLlamada(
            sampleRateHz = sampleRate,
            bloques = mutableListOf(generarSilencioDigital(100), generarSenoidal(220.0, 100, amplitud = 0.8)),
            avisaQueEstaLeyendo = leyendoElBloqueEnVuelo,
            esperaHastaQuePuedaContinuar = puedeTerminarDeLeerlo
        )
        // duracionCalibracionMs = duracionBloqueMs: la calibracion consume exactamente 1
        // bloque (la primera llamada a leerSiguienteBloque, que no bloquea) y pasa a
        // Escuchando; la SEGUNDA llamada -ya dentro de procesarSiguienteBloque- es la que
        // el fake deja a medio camino.
        val capturador = CapturadorVoz(
            fuente = fuente,
            scope = CoroutineScope(Job() + Dispatchers.IO),
            dispatcher = Dispatchers.IO,
            duracionBloqueMs = 100L,
            duracionCalibracionMs = 100L
        )

        capturador.iniciar()
        assertTrue(
            "el bloque en vuelo no empezo a leerse a tiempo",
            leyendoElBloqueEnVuelo.await(2, TimeUnit.SECONDS)
        )

        // En este punto, calibrar() ya publico Escuchando y la corrutina de fondo esta
        // bloqueada a mitad de su SEGUNDA lectura. detener() corre en este hilo (el de la
        // prueba), como si fuera el hilo de UI tocando "Terminar".
        capturador.detener()
        assertTrue(
            "detener() debe dejar el estado en Detenido de inmediato",
            capturador.estado.value is EstadoCapturaEnVivo.Detenido
        )

        // Se libera el bloque que estaba en vuelo: sin el guard contra la carrera, su
        // callback tardio pisaria el Detenido de arriba con un nuevo Escuchando.
        puedeTerminarDeLeerlo.countDown()

        val limite = System.currentTimeMillis() + 300
        while (System.currentTimeMillis() < limite) {
            Thread.sleep(20)
        }
        assertTrue(
            "un bloque que ya estaba en vuelo antes de detener() no debe revertir el estado a Escuchando",
            capturador.estado.value is EstadoCapturaEnVivo.Detenido
        )
    }
}

/** Como [FuenteDeAudioFalsa], pero su segunda llamada a [leerSiguienteBloque] se queda
 * bloqueada hasta que la prueba la libere -para poder orquestar con precision una
 * llamada a [CapturadorVoz.detener] justo mientras esa lectura esta en vuelo. */
private class FuenteDeAudioQueBloqueaEnLaSegundaLlamada(
    override val sampleRateHz: Int,
    private val bloques: MutableList<ShortArray>,
    private val avisaQueEstaLeyendo: CountDownLatch,
    private val esperaHastaQuePuedaContinuar: CountDownLatch
) : FuenteDeAudio {
    private var llamada = 0

    override fun leerSiguienteBloque(): ShortArray {
        llamada++
        if (llamada == 2) {
            avisaQueEstaLeyendo.countDown()
            esperaHastaQuePuedaContinuar.await()
        }
        return if (bloques.isNotEmpty()) bloques.removeAt(0) else ShortArray(0)
    }

    override fun liberar() = Unit
}

/** Fuente de audio falsa para pruebas: entrega bloques predefinidos en orden y señala el fin
 * de la captura devolviendo un [ShortArray] vacío cuando se agota, igual que contempla el
 * contrato de [FuenteDeAudio]. */
class FuenteDeAudioFalsa(
    override val sampleRateHz: Int,
    private val bloques: MutableList<ShortArray>
) : FuenteDeAudio {

    var liberada = false
        private set

    override fun leerSiguienteBloque(): ShortArray =
        if (bloques.isNotEmpty()) bloques.removeAt(0) else ShortArray(0)

    override fun liberar() {
        if (liberada) throw IllegalStateException("liberar() no deberia llamarse dos veces sobre la misma fuente")
        liberada = true
    }
}
