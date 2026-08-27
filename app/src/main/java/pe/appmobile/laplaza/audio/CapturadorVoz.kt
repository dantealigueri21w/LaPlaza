package pe.appmobile.laplaza.audio

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.appmobile.laplaza.domain.engine.MotorAcustico
import pe.appmobile.laplaza.domain.engine.MotorFluidez
import pe.appmobile.laplaza.domain.engine.MotorPuntajeAudiencia
import pe.appmobile.laplaza.domain.model.MuestraAcustica
import pe.appmobile.laplaza.domain.model.NivelAtencion
import pe.appmobile.laplaza.domain.model.PuntajeAudiencia
import pe.appmobile.laplaza.domain.model.ResultadoAcustico
import kotlin.math.log2
import kotlin.math.sqrt

/** Duración mínima de una racha de silencio para contar como "pausa" real y no como el
 * hueco natural entre dos sílabas. Es una decisión de este orquestador, no de [MotorAcustico]
 * (que solo sabe agrupar rachas de silencio si se le da un umbral): 200 ms es el límite
 * inferior de lo que [MotorFluidez] ya trata como una pausa "limpia" válida, así que una
 * racha más corta que eso ni siquiera llega a ser candidata a pausa. */
private const val UMBRAL_PAUSA_MS = 200L

/** Margen sobre el pico de ruido observado durante la calibración. Se multiplica el PICO
 * (no el promedio) porque un solo golpe de ruido ambiental en los 3-4 s de "respiración"
 * -una puerta, un carro que pasa- no debe quedar en el lado equivocado del umbral y leerse
 * luego como voz; el margen de 1.4x deja además espacio para que una voz real, que en un
 * niño suele superar al ruido de fondo con holgura, quede claramente por encima. */
private const val MARGEN_SOBRE_PICO_RUIDO = 1.4f

/** Margen absoluto mínimo sobre el promedio de ruido, para el caso borde de una calibración
 * casi perfecta (pico y promedio casi iguales, sala muy silenciosa): sin este piso, el umbral
 * calculado podría quedar tan pegado al ruido que un susto de ruido nuevo, no capturado en la
 * ventana de calibración, se leería como voz. */
private const val MARGEN_ABSOLUTO_MINIMO = 0.01f

/** Piso absoluto del umbral calibrado. Nunca se calibra por debajo de esto, ni en la sala más
 * silenciosa posible: evita que un crujido del piso o el ruido propio del micrófono a
 * ganancia mínima se interprete como el inicio de la voz. */
private const val UMBRAL_MINIMO_ABSOLUTO = 0.005f

/**
 * Duración por defecto del ritual de respiración: 3.5 s, dentro del rango "3-4 segundos" que
 * describe la ficha. Se probó desde el constructor (no como una constante interna fija) para
 * que las pruebas puedan usar una calibración mucho más corta sin cambiar la lógica.
 */
private const val DURACION_CALIBRACION_MS_POR_DEFECTO = 3500L

/**
 * Snapshot de las 4 variables en vivo más el puntaje compuesto, en el punto exacto del
 * discurso en el que se generó. Reutiliza los tipos de dominio ya probados
 * ([ResultadoAcustico], [PuntajeAudiencia]) en vez de inventar un tipo paralelo: así, una
 * futura pantalla o ViewModel que quiera llamar a `LaPlazaRepository.registrarIntento` tiene
 * en [acustico] el volumen/entonación/ritmo/pausas crudos, en [fluidez] la cuarta variable ya
 * normalizada 0..1 por [MotorFluidez], y en [puntaje] el compuesto que mueve a la plaza.
 */
data class ResultadoEnVivo(
    val acustico: ResultadoAcustico,
    val fluidez: Float,
    val puntaje: PuntajeAudiencia
) {
    companion object {
        /** Estado antes de procesar el primer bloque: cero variables, cero pausas. */
        val VACIO = ResultadoEnVivo(
            acustico = ResultadoAcustico(
                volumenPromedio = 0f,
                variacionEntonacionSemitonos = 0f,
                ritmoSilabasPorMinuto = 0f,
                pausas = emptyList(),
                duracionTotalMs = 0L
            ),
            fluidez = 1f,
            puntaje = PuntajeAudiencia(0f, NivelAtencion.DISTRAIDA)
        )
    }
}

/** Ciclo de vida observable de una captura: una futura pantalla colecciona [CapturadorVoz.estado]
 * sin saber nunca que existe `AudioRecord` detrás. */
sealed interface EstadoCapturaEnVivo {
    /** Antes de llamar a [CapturadorVoz.iniciar]. Es el valor inicial del StateFlow, así que
     * un colector siempre tiene algo que leer desde el primer momento (cold-start safe). */
    data object SinIniciar : EstadoCapturaEnVivo

    /** Ritual de respiración en curso: [progreso] va de 0 (recién empezado) a 1 (a punto de
     * terminar y arrancar la escucha real). */
    data class Calibrando(val progreso: Float) : EstadoCapturaEnVivo

    /** Captura en vivo, tras calibrar. [umbralCalibrado] queda expuesto para depuración/UI de
     * diagnóstico; [resultado] es el snapshot más reciente. */
    data class Escuchando(val umbralCalibrado: Float, val resultado: ResultadoEnVivo) : EstadoCapturaEnVivo

    /** La captura terminó -por [CapturadorVoz.detener] o porque la fuente se agotó sola- con
     * [resultado] congelado en su último valor real. */
    data class Detenido(val umbralCalibrado: Float, val resultado: ResultadoEnVivo) : EstadoCapturaEnVivo
}

/**
 * Bisagra entre un micrófono real (o uno falso, en pruebas) y los motores de dominio de La
 * Plaza. No sabe nada de pantallas ni de Compose: solo lee bloques de [fuente], los hace pasar
 * por [MotorAcustico] → [MotorFluidez] → [MotorPuntajeAudiencia], y publica el resultado en
 * [estado] para que una futura pantalla/ViewModel lo colecte.
 *
 * Nunca crea su propio [CoroutineScope]: el [scope] se inyecta (en producción, el
 * `viewModelScope` de una futura pantalla) para que este capturador jamás pueda fugar una
 * corrutina -cancelar ese scope basta para detener todo, incluso si nadie llamó a [detener].
 *
 * Privacidad: el único estado que este objeto conserva entre bloques es una lista de
 * [MuestraAcustica] (RMS, F0 y un booleano por bloque -unos pocos bytes cada uno, nunca
 * audio). El `ShortArray` que devuelve cada `leerSiguienteBloque()` se usa una sola vez, para
 * calcular esa muestra, y se descarta de inmediato: nunca se guarda en una lista, nunca se
 * escribe a disco.
 */
class CapturadorVoz(
    private val fuente: FuenteDeAudio,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val duracionBloqueMs: Long = FuenteDeAudioReal.DURACION_BLOQUE_MS,
    private val duracionCalibracionMs: Long = DURACION_CALIBRACION_MS_POR_DEFECTO
) {

    private val _estado = MutableStateFlow<EstadoCapturaEnVivo>(EstadoCapturaEnVivo.SinIniciar)
    val estado: StateFlow<EstadoCapturaEnVivo> = _estado.asStateFlow()

    // Derivados acústicos por bloque, nunca el audio crudo. Ver el comentario de privacidad
    // de la clase.
    private val muestras = mutableListOf<MuestraAcustica>()

    private var umbral: Float = MotorAcustico.UMBRAL_RMS_VOZ
    private var job: Job? = null
    private var finalizadoYa = false

    /**
     * Arranca el ritual de respiración y, al terminar, la escucha en vivo, en una corrutina
     * del [scope] inyectado. No-op si ya hay una captura en curso (evita arrancar dos veces
     * en paralelo sobre la misma fuente).
     */
    fun iniciar() {
        if (job?.isActive == true) return
        muestras.clear()
        umbral = MotorAcustico.UMBRAL_RMS_VOZ
        finalizadoYa = false
        job = scope.launch(dispatcher) {
            calibrar()
            while (isActive) {
                if (!procesarSiguienteBloque()) break
            }
            finalizar()
        }
    }

    /**
     * Detiene la captura y libera el micrófono de inmediato, sin esperar a que la corrutina
     * de [iniciar] note la cancelación. Segura de llamar más de una vez, o antes de haber
     * llamado a [iniciar] siquiera -en cualquier caso, garantiza que [fuente] queda liberada.
     */
    fun detener() {
        job?.cancel()
        finalizar()
    }

    /** Ritual de respiración: toma [duracionCalibracionMs] de silencio real y deriva el
     * umbral de esa sesión (ver [derivarUmbral]), en vez de usar el umbral genérico fijo de
     * [MotorAcustico]. */
    private fun calibrar() {
        val bloquesNecesarios = if (duracionBloqueMs <= 0L) {
            1
        } else {
            maxOf(1, (duracionCalibracionMs / duracionBloqueMs).toInt())
        }
        val rmsDeRuido = mutableListOf<Float>()
        for (i in 1..bloquesNecesarios) {
            _estado.value = EstadoCapturaEnVivo.Calibrando(progreso = i.toFloat() / bloquesNecesarios)
            val bloque = fuente.leerSiguienteBloque()
            if (bloque.isNotEmpty()) rmsDeRuido.add(MotorAcustico.calcularRms(bloque))
        }
        umbral = derivarUmbral(rmsDeRuido)
        _estado.value = EstadoCapturaEnVivo.Escuchando(umbral, ResultadoEnVivo.VACIO)
    }

    /**
     * Deriva el umbral real de silencio/voz de esta sesión a partir de las muestras de RMS
     * tomadas durante el ritual de respiración. Ver los comentarios de [MARGEN_SOBRE_PICO_RUIDO],
     * [MARGEN_ABSOLUTO_MINIMO] y [UMBRAL_MINIMO_ABSOLUTO] para el porqué de cada término.
     */
    internal fun derivarUmbral(rmsDeRuido: List<Float>): Float {
        if (rmsDeRuido.isEmpty()) return MotorAcustico.UMBRAL_RMS_VOZ
        val pico = rmsDeRuido.max()
        val promedio = rmsDeRuido.average().toFloat()
        val propuesto = maxOf(pico * MARGEN_SOBRE_PICO_RUIDO, promedio + MARGEN_ABSOLUTO_MINIMO)
        return propuesto.coerceAtLeast(UMBRAL_MINIMO_ABSOLUTO)
    }

    /**
     * Lee y procesa exactamente un bloque. Devuelve `false` cuando la fuente ya no tiene más
     * datos (fin natural de la captura), `true` en caso contrario. Público a nivel de módulo
     * (`internal`) solo para que las pruebas puedan pilotear la orquestación bloque a bloque
     * sin depender de temporizadores reales.
     */
    internal fun procesarSiguienteBloque(): Boolean {
        val bloque = fuente.leerSiguienteBloque()
        if (bloque.isEmpty()) return false
        muestras.add(MotorAcustico.analizarVentana(bloque, fuente.sampleRateHz, umbral))
        _estado.value = EstadoCapturaEnVivo.Escuchando(umbral, calcularResultadoEnVivo())
        return true
    }

    private fun calcularResultadoEnVivo(): ResultadoEnVivo {
        val duracionTotalMs = muestras.size * duracionBloqueMs
        val acustico = ResultadoAcustico(
            volumenPromedio = calcularVolumenPromedio(muestras),
            variacionEntonacionSemitonos = calcularVariacionEntonacion(muestras),
            ritmoSilabasPorMinuto = MotorAcustico.calcularRitmoSilabasPorMinuto(
                MotorAcustico.contarSilabas(muestras),
                duracionTotalMs
            ),
            pausas = MotorAcustico.detectarPausas(muestras, duracionBloqueMs, UMBRAL_PAUSA_MS),
            duracionTotalMs = duracionTotalMs
        )
        val fluidez = MotorFluidez.calcularFluidez(acustico.pausas, acustico.duracionTotalMs)
        val puntaje = MotorPuntajeAudiencia.calcularPuntajeCompuesto(acustico, fluidez)
        return ResultadoEnVivo(acustico, fluidez, puntaje)
    }

    /** Promedio de RMS solo de las ventanas marcadas como voz. Las ventanas de silencio
     * quedan fuera a propósito: su RMS es, por definición, cercano a cero (es lo que las hizo
     * "silencio" contra el umbral calibrado), así que promediarlas junto a las de voz
     * penalizaría dos veces el mismo silencio -una vez aquí y otra vez, correctamente, en
     * [MotorFluidez] vía las pausas. */
    private fun calcularVolumenPromedio(muestras: List<MuestraAcustica>): Float {
        val deVoz = muestras.filter { it.esVoz }
        if (deVoz.isEmpty()) return 0f
        return (deVoz.sumOf { it.rms.toDouble() } / deVoz.size).toFloat()
    }

    /**
     * Variación de entonación en semitonos: desviación estándar de las frecuencias
     * fundamentales de voz (convertidas a semitonos respecto a su propia media) en lo que va
     * del discurso. [MotorAcustico] no expone una función para esto -solo calcula F0 ventana a
     * ventana-, así que agregarlas a lo largo del intento es trabajo propio de este
     * orquestador. Con menos de dos muestras de voz con F0 detectado no hay variación que
     * medir: se devuelve 0.
     */
    private fun calcularVariacionEntonacion(muestras: List<MuestraAcustica>): Float {
        val f0sHz = muestras.filter { it.esVoz && it.f0Hz > 0f }.map { it.f0Hz.toDouble() }
        if (f0sHz.size < 2) return 0f
        val mediaHz = f0sHz.average()
        val semitonos = f0sHz.map { 12.0 * log2(it / mediaHz) }
        val mediaSemitonos = semitonos.average()
        val varianza = semitonos.sumOf { (it - mediaSemitonos) * (it - mediaSemitonos) } / semitonos.size
        return sqrt(varianza).toFloat()
    }

    private fun finalizar() {
        if (finalizadoYa) return
        finalizadoYa = true
        val actual = _estado.value
        if (actual is EstadoCapturaEnVivo.Escuchando) {
            _estado.value = EstadoCapturaEnVivo.Detenido(actual.umbralCalibrado, actual.resultado)
        }
        fuente.liberar()
    }
}
