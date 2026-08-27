package pe.appmobile.laplaza.data.seed

/**
 * Definicion de los 21 temas de discurso (3 por rincon: FACIL, MEDIO, DIFICIL -salvo
 * TARIMA_MAYOR, que es MEDIO/DIFICIL/DIFICIL a proposito: es el rincon final, ninguno de
 * sus temas deberia sentirse "facil").
 *
 * [clave] es un identificador local estable, NO el id autogenerado de Room (TemaEntity.id
 * se conoce recien al insertar, porque es autoGenerate = true). El sembrado del
 * repositorio inserta cada tema uno por uno, y usa [clave] para encontrar en
 * SemillaBloques.porTema los 9 bloques de contenido que le corresponden, antes de
 * insertarlos ya con el temaId real.
 */
data class TemaSemilla(
    val clave: String,
    val rinconId: String,
    val titulo: String,
    val dificultad: String,
    val orden: Int
)

object SemillaTemas {
    val temas = listOf(
        // El Balcón
        TemaSemilla("BALCON_1", "BALCON", "Preséntate a la plaza", "FACIL", 1),
        TemaSemilla("BALCON_2", "BALCON", "Tu color o comida favorita y por qué", "MEDIO", 2),
        TemaSemilla("BALCON_3", "BALCON", "Algo que se te da bien y cómo lo aprendiste", "DIFICIL", 3),

        // El Kiosco
        TemaSemilla("KIOSCO_1", "KIOSCO", "Algo divertido que te pasó en el recreo", "FACIL", 1),
        TemaSemilla("KIOSCO_2", "KIOSCO", "Una noticia del barrio", "MEDIO", 2),
        TemaSemilla("KIOSCO_3", "KIOSCO", "Cómo se resolvió un problema entre amigos", "DIFICIL", 3),

        // El Mostrador
        TemaSemilla("MOSTRADOR_1", "MOSTRADOR", "Pide que te dejen quedarte un rato más jugando", "FACIL", 1),
        TemaSemilla("MOSTRADOR_2", "MOSTRADOR", "Pide ayuda para cargar algo pesado", "MEDIO", 2),
        TemaSemilla("MOSTRADOR_3", "MOSTRADOR", "Pide que le den una segunda oportunidad a un compañero", "DIFICIL", 3),

        // El Jardín
        TemaSemilla("JARDIN_1", "JARDIN", "Agradece a alguien que te ayudó hoy", "FACIL", 1),
        TemaSemilla("JARDIN_2", "JARDIN", "Reconoce a un amigo por algo que hizo bien", "MEDIO", 2),
        TemaSemilla("JARDIN_3", "JARDIN", "Agradece a alguien de tu familia por algo que hace siempre", "DIFICIL", 3),

        // La Fuente
        TemaSemilla("FUENTE_1", "FUENTE", "Anima a un amigo que perdió un partido", "FACIL", 1),
        TemaSemilla("FUENTE_2", "FUENTE", "Consuela a alguien que está triste porque se mudó un amigo", "MEDIO", 2),
        TemaSemilla("FUENTE_3", "FUENTE", "Anima a alguien que tiene miedo de intentar algo nuevo", "DIFICIL", 3),

        // El Mirador
        TemaSemilla("MIRADOR_1", "MIRADOR", "Anuncia que hay una feria en la plaza este sábado", "FACIL", 1),
        TemaSemilla("MIRADOR_2", "MIRADOR", "Anuncia una regla nueva para cuidar el parque", "MEDIO", 2),
        TemaSemilla("MIRADOR_3", "MIRADOR", "Anuncia el resultado de una votación del salón", "DIFICIL", 3),

        // La Tarima Mayor (capstone: MEDIO/DIFICIL/DIFICIL a propósito, ver KDoc arriba)
        TemaSemilla("TARIMA_MAYOR_1", "TARIMA_MAYOR", "Discurso de despedida de año escolar", "MEDIO", 1),
        TemaSemilla("TARIMA_MAYOR_2", "TARIMA_MAYOR", "Discurso para inaugurar algo nuevo en la plaza", "DIFICIL", 2),
        TemaSemilla("TARIMA_MAYOR_3", "TARIMA_MAYOR", "Discurso para presentar un proyecto tuyo a toda la plaza", "DIFICIL", 3)
    )
}
