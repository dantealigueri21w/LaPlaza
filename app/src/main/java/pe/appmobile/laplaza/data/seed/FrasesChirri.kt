package pe.appmobile.laplaza.data.seed

/**
 * Frases de Chirri (30 en total: 10 avisos + 10 de animo + 10 de celebracion). No se
 * siembran en Room -es una tabla de referencia en codigo- de la que una futura capa de
 * UI elegira al azar segun el momento; no necesita entidad ni tabla propia.
 */
object FrasesChirri {
    val avisos = listOf(
        "Psst... alguien en El Balcón quiere conocerte.",
        "Tengo ganas de escuchar una historia. ¿Vamos al Kiosco?",
        "En El Mostrador hay algo que pedir con buenas razones.",
        "El Jardín está esperando un gracias de verdad.",
        "Alguien en La Fuente necesita ánimo. ¿Le hablamos?",
        "Tengo un anuncio importante... bueno, tú lo tienes. Vamos al Mirador.",
        "¿Ya armaste tu discurso de hoy? Yo ya elegí el mío.",
        "La plaza está más tranquila de lo normal. ¿La despertamos?",
        "Practiquemos un rato en el Rincón Libre, sin apuro.",
        "Hoy se siente un buen día para hablar fuerte."
    )

    val animo = listOf(
        "Respira conmigo. Uno... dos... listo.",
        "No hace falta que salga perfecto, solo que sea tuyo.",
        "Yo también me pongo nervioso. Vamos juntos.",
        "Si se te traba la voz, para, respira, y sigue.",
        "Nadie te está juzgando. Solo están escuchando.",
        "Empieza cuando quieras. Yo espero aquí.",
        "Tu voz es la única que puede contar esto así.",
        "Un paso a la vez: primero el gancho, después lo demás.",
        "Está bien si tiemblas un poco. Yo también tiemblo.",
        "Cuenta hasta tres y suelta la voz."
    )

    val celebraciones = listOf(
        "¿Escuchaste eso? ¡Toda la plaza se volteó!",
        "Eso que hiciste, yo no hubiera podido.",
        "Se te notó la voz clarita de principio a fin.",
        "¡Lo lograste! Y yo que pensé que se te iba a cortar.",
        "Esa pausa antes del cierre... perfecta, de verdad.",
        "Un farol más encendido, gracias a ti.",
        "Hoy la plaza tiene una historia nueva que contar.",
        "Eso fue valiente, aunque no lo sintieras así.",
        "Guardé esto en el Cuaderno. Se merece quedar escrito.",
        "Mañana hay otro motivo para volver. Te espero."
    )
}
