package pe.appmobile.laplaza.domain.model

enum class IdRincon { BALCON, KIOSCO, MOSTRADOR, JARDIN, FUENTE, MIRADOR, TARIMA_MAYOR, LIBRE }

data class SugerenciaRepaso(val temaId: Long, val nombreTema: String, val motivo: String)

/** Una fila real de `intento`, ya traducida a lo minimo que [pe.appmobile.laplaza.domain.engine.MotorInsignias]
 * necesita para evaluar las 12 insignias -- sin nada de Room ni de Android, para que el
 * motor se pueda probar puro (seccion 8 del maestro). [rinconId] es el del TEMA
 * declamado (el mismo string que [IdRincon.name]), no el rincon desde el que se navego:
 * un intento hecho vía Rincon Libre sobre un tema de El Kiosco sigue teniendo
 * `rinconId = "KIOSCO"`. */
data class IntentoParaInsignias(
    val temaId: Long,
    val rinconId: String,
    val viaRinconLibre: Boolean,
    val puntajeVolumen: Float,
    val puntajeCompuesto: Float,
    val puntajeFluidez: Float
)
