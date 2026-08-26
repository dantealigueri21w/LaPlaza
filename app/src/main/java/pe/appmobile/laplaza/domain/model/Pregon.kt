package pe.appmobile.laplaza.domain.model

data class DatosIntento(
    val nombreTema: String,
    val nombreRincon: String,
    val puntajeVolumen: Float,
    val puntajeEntonacion: Float,
    val puntajeRitmo: Float,
    val puntajeFluidez: Float
)

data class Pregon(val titular: String, val variableDestacada: String)
