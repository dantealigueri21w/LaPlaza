package pe.appmobile.laplaza.data.seed

/**
 * Un bloque de contenido real de un tema. [franja] coincide exactamente con los valores
 * que guarda BloqueContenidoEntity.franja: "GANCHO", "CUERPO" o "CIERRE". [orden] es la
 * posicion 1..3 dentro de esa franja (no la posicion global entre los 9 bloques).
 */
data class BloqueSemilla(val franja: String, val texto: String, val orden: Int)

/**
 * Los 9 bloques de contenido (3 GANCHO + 3 CUERPO + 3 CIERRE) de cada uno de los 21 temas,
 * indexados por TemaSemilla.clave. El sembrado del repositorio primero inserta el tema
 * (obteniendo su id real autogenerado) y recien despues busca aqui sus bloques para
 * insertarlos con ese temaId.
 */
object SemillaBloques {

    val porTema: Map<String, List<BloqueSemilla>> = mapOf(

        // ---------- El Balcón ----------

        "BALCON_1" to bloques(
            gancho = listOf(
                "¡Hola, plaza! Soy nuevo por aquí y quiero que me conozcan.",
                "¿Puedo contarles algo sobre mí antes de que seamos amigos?",
                "Antes de jugar juntos, déjenme presentarme como se debe."
            ),
            cuerpo = listOf(
                "Me llamo como me llamo y lo que más me gusta hacer es...",
                "Soy de por aquí cerca, y algo que se me da bien es...",
                "Tengo una cosa que casi nadie sabe de mí, y es que..."
            ),
            cierre = listOf(
                "Eso soy yo. ¡Espero que nos llevemos bien!",
                "Y así me conocerán a partir de hoy.",
                "Ahora ya saben quién soy. ¡A jugar se ha dicho!"
            )
        ),

        "BALCON_2" to bloques(
            gancho = listOf(
                "¿Adivinen cuál es mi favorito de todos?",
                "Hay algo que elijo siempre, sin pensarlo dos veces.",
                "Si me preguntan una sola cosa sobre mí, sería esta."
            ),
            cuerpo = listOf(
                "Mi favorito es este, y lo elijo porque me hace sentir...",
                "Lo probé (o lo vi) por primera vez cuando... y desde ahí no cambié de opinión.",
                "No es solo que me guste: es que me recuerda a..."
            ),
            cierre = listOf(
                "Por eso, para mí, no hay otro como este.",
                "Así que ya lo saben: ese es mi favorito, y no pienso cambiarlo.",
                "¿Y ustedes? Seguro tienen uno parecido."
            )
        ),

        "BALCON_3" to bloques(
            gancho = listOf(
                "Hay algo que hago mejor que la mayoría, y no siempre fue así.",
                "¿Quieren saber en qué soy bueno de verdad?",
                "Esto no me salió fácil la primera vez, pero ahora sí."
            ),
            cuerpo = listOf(
                "Se me da bien esto, y lo aprendí practicando una y otra vez hasta que salió.",
                "Al principio se me caía, se me olvidaba o me salía mal, pero seguí intentando.",
                "Alguien me enseñó, y después seguí solo hasta hacerlo mío."
            ),
            cierre = listOf(
                "Por eso ahora puedo decir que sí se me da bien.",
                "Y todavía sigo mejorando cada vez que lo hago.",
                "Si algo no les sale a la primera, a mí tampoco me salió."
            )
        ),

        // ---------- El Kiosco ----------

        "KIOSCO_1" to bloques(
            gancho = listOf(
                "¿A que no adivinan lo que pasó hoy en el recreo?",
                "Esto que les voy a contar todavía me hace reír.",
                "En el recreo de hoy pasó algo que nadie se esperaba."
            ),
            cuerpo = listOf(
                "Estábamos jugando cuando de repente pasó esto, y todos nos quedamos con la boca abierta.",
                "Alguien intentó algo, salió mal de la forma más chistosa, y terminamos riéndonos todos juntos.",
                "Fue solo un momento, pero fue tan raro y tan gracioso que no se me olvida."
            ),
            cierre = listOf(
                "Por eso hoy fue un recreo que no voy a olvidar.",
                "Y ahora cada vez que lo recuerdo, me vuelvo a reír.",
                "Así que ya saben: en ese recreo, pasó de todo."
            )
        ),

        "KIOSCO_2" to bloques(
            gancho = listOf(
                "Tengo una noticia fresca del barrio, recién enterada.",
                "¿Ya se enteraron de lo que va a pasar por aquí?",
                "Esto le va a interesar a todo el que vive cerca."
            ),
            cuerpo = listOf(
                "Resulta que en el barrio va a pasar esto, y lo supe porque...",
                "Es algo que va a cambiar cómo se ve o cómo funciona esta zona.",
                "Todavía no está confirmado del todo, pero las señales apuntan a que sí va a pasar."
            ),
            cierre = listOf(
                "Así que ya lo saben, corran la voz.",
                "Por eso vale la pena estar atentos los próximos días.",
                "Esa es la noticia. Ahora ustedes decidan qué piensan de ella."
            )
        ),

        "KIOSCO_3" to bloques(
            gancho = listOf(
                "Hace poco hubo un problema entre dos amigos míos, y así se resolvió.",
                "No siempre las peleas terminan mal, esta vez terminó bien, y les cuento cómo.",
                "Esto empezó como un malentendido y terminó siendo una lección."
            ),
            cuerpo = listOf(
                "Todo empezó porque uno pensó una cosa y el otro entendió otra completamente distinta.",
                "Al principio ninguno quería ceder, hasta que alguien propuso simplemente hablarlo con calma.",
                "Cuando por fin se sentaron a escucharse, se dieron cuenta de que el problema era más pequeño de lo que parecía."
            ),
            cierre = listOf(
                "Así terminaron siendo amigos otra vez, y hasta más unidos que antes.",
                "La lección quedó clara: hablar a tiempo evita que un problema chico se haga grande.",
                "Y desde entonces, cuando algo no les queda claro, prefieren preguntar antes de enojarse."
            )
        ),

        // ---------- El Mostrador ----------

        "MOSTRADOR_1" to bloques(
            gancho = listOf(
                "Tengo un pedido, y creo que tiene una buena razón.",
                "Antes de que digan que no, escuchen por qué lo pido.",
                "Solo les voy a pedir una cosa, nada más."
            ),
            cuerpo = listOf(
                "Quiero quedarme un rato más jugando, porque justo estábamos por terminar algo importante.",
                "No es que quiera desobedecer, es que unos minutos más harían la diferencia.",
                "Prometo que en cuanto termine, voy a hacer todo lo que me toca sin quejarme."
            ),
            cierre = listOf(
                "Por eso les pido, con toda razón, un poco más de tiempo.",
                "Solo unos minutos, y se los voy a agradecer de verdad.",
                "Creo que es un pedido justo. ¿Qué dicen?"
            )
        ),

        "MOSTRADOR_2" to bloques(
            gancho = listOf(
                "Necesito una mano, y creo que ustedes pueden dármela.",
                "Esto solo no lo puedo mover, así que les pido ayuda.",
                "Antes de lastimarme intentándolo solo, mejor pido ayuda."
            ),
            cuerpo = listOf(
                "Tengo que llevar esto de un lado a otro, pero pesa más de lo que puedo cargar yo solo.",
                "Si alguien me ayuda un momento, terminamos rápido y entre los dos no pesa nada.",
                "No es algo difícil, solo necesita más de un par de manos."
            ),
            cierre = listOf(
                "Por eso pido ayuda, no porque no pueda intentarlo, sino porque es más seguro así.",
                "Con una mano más, esto se resuelve en un momento.",
                "¿Alguien se anima a ayudarme? Se los voy a agradecer."
            )
        ),

        "MOSTRADOR_3" to bloques(
            gancho = listOf(
                "Sé que mi compañero se equivocó, pero quiero pedir algo por él.",
                "Antes de decidir sobre él, déjenme decir una cosa.",
                "Esto no es fácil de pedir, pero creo que vale la pena."
            ),
            cuerpo = listOf(
                "Sé que lo que hizo no estuvo bien, y él también lo sabe, por eso quiere corregirlo.",
                "Todos nos equivocamos alguna vez, y lo que importa es qué hacemos después del error.",
                "Le pido una segunda oportunidad, no porque no haya consecuencias, sino porque puede demostrar que aprendió."
            ),
            cierre = listOf(
                "Por eso pido que le den la oportunidad de mostrar que puede hacerlo mejor.",
                "Una segunda oportunidad no es no tener consecuencias, es creer que alguien puede cambiar.",
                "Confío en que, si se la dan, no los va a decepcionar."
            )
        ),

        // ---------- El Jardín ----------

        "JARDIN_1" to bloques(
            gancho = listOf(
                "Hoy alguien me ayudó, y quiero decírselo delante de todos.",
                "Antes de que se me olvide, tengo que agradecer algo.",
                "Esto que voy a decir es un simple gracias, pero de corazón."
            ),
            cuerpo = listOf(
                "Hoy me costaba hacer algo, y esta persona se acercó a ayudarme sin que se lo pidiera.",
                "No tenía por qué hacerlo, pero lo hizo igual, y eso significó mucho para mí.",
                "Gracias a esa ayuda, pude terminar algo que solo no hubiera logrado."
            ),
            cierre = listOf(
                "Por eso, de verdad, muchas gracias.",
                "Ese tipo de ayuda no se olvida fácil.",
                "Espero poder devolverle el favor pronto."
            )
        ),

        "JARDIN_2" to bloques(
            gancho = listOf(
                "Quiero contarles algo que hizo un amigo mío, porque se lo merece.",
                "No siempre se reconoce a tiempo lo bueno que hace la gente, así que hoy sí.",
                "Esto se lo tengo que decir en voz alta, no solo para él."
            ),
            cuerpo = listOf(
                "Mi amigo hizo algo que no era fácil, y lo hizo bien, sin buscar que nadie lo aplaudiera.",
                "Se esforzó, no se rindió a la primera, y al final le salió tal como quería.",
                "Lo que más admiro es que lo hizo pensando en ayudar a otros, no solo en quedar bien."
            ),
            cierre = listOf(
                "Por eso quería que todos supieran lo que hizo.",
                "Se lo merece, y me alegra tenerlo como amigo.",
                "Ojalá más gente hiciera las cosas como él las hizo."
            )
        ),

        "JARDIN_3" to bloques(
            gancho = listOf(
                "Hay alguien en mi familia que hace algo todos los días sin que se lo agradezcan.",
                "Esto lo pienso seguido, pero nunca lo había dicho en voz alta.",
                "Quiero hablar de alguien que siempre está, aunque nadie se lo note."
            ),
            cuerpo = listOf(
                "Todos los días hace algo por mí que yo casi nunca noto, hasta que me puse a pensarlo bien.",
                "No pide nada a cambio, y lo sigue haciendo aunque esté cansado o tenga su propio día difícil.",
                "Es de esas cosas pequeñas que, sumadas, hacen una gran diferencia en mi vida."
            ),
            cierre = listOf(
                "Por eso hoy quiero decirle, en público, que se lo agradezco de verdad.",
                "No siempre lo digo, pero siempre lo pienso.",
                "Ojalá esta persona sepa lo importante que es para mí."
            )
        ),

        // ---------- La Fuente ----------

        "FUENTE_1" to bloques(
            gancho = listOf(
                "Sé que hoy no ganamos, pero tengo algo que decirte.",
                "Perder duele, y quiero que sepas que no estás solo en esto.",
                "Antes de que te sientas mal todo el día, escúchame un momento."
            ),
            cuerpo = listOf(
                "Perder este partido no borra todo lo bueno que hiciste mientras jugabas.",
                "Todos los equipos pierden alguna vez, hasta los mejores, y lo que importa es lo que hacemos después.",
                "La próxima vez vamos a estar mejor preparados, y yo voy a estar ahí contigo."
            ),
            cierre = listOf(
                "Así que levanta la cabeza, que esto no define quién eres.",
                "Un partido perdido no es el final de nada.",
                "Vamos por el siguiente, juntos."
            )
        ),

        "FUENTE_2" to bloques(
            gancho = listOf(
                "Sé que extrañas a tu amigo, y quiero acompañarte un momento.",
                "No voy a decirte que no estés triste, porque tienes derecho a estarlo.",
                "Esto que sientes es normal, y quiero que lo sepas."
            ),
            cuerpo = listOf(
                "Que alguien se mude no significa que la amistad se acabe, solo que cambia de forma.",
                "Todavía pueden escribirse, llamarse, contarse cómo les va, la distancia no borra lo que vivieron.",
                "Mientras tanto, aquí sigo yo, y podemos extrañarlo juntos en vez de que lo hagas solo."
            ),
            cierre = listOf(
                "Así que no estás solo en esto, cuenta conmigo.",
                "La tristeza va a pasar, y los recuerdos se quedan.",
                "Y quién sabe, capaz se vuelven a encontrar más pronto de lo que piensan."
            )
        ),

        "FUENTE_3" to bloques(
            gancho = listOf(
                "Sé que tienes miedo de intentarlo, y eso no tiene nada de malo.",
                "Antes de que decidas no hacerlo, déjame decirte algo.",
                "Todos hemos tenido miedo de empezar algo alguna vez, yo incluido."
            ),
            cuerpo = listOf(
                "Tener miedo no significa que no puedas hacerlo, solo significa que te importa el resultado.",
                "Nadie empieza siendo bueno en algo, se empieza intentando, aunque salga mal las primeras veces.",
                "Y si te sale mal, no pasa nada: lo importante es que lo intentaste, y eso ya es valiente."
            ),
            cierre = listOf(
                "Así que anímate, que yo voy a estar aquí para verte intentarlo.",
                "El miedo se queda más pequeño cada vez que uno se atreve.",
                "Y si no te sale hoy, mañana lo vuelves a intentar."
            )
        ),

        // ---------- El Mirador ----------

        "MIRADOR_1" to bloques(
            gancho = listOf(
                "¡Atención, plaza! Tengo un anuncio importante.",
                "Escuchen todos, que esto les va a interesar.",
                "Este sábado va a pasar algo que no se pueden perder."
            ),
            cuerpo = listOf(
                "Este sábado hay una feria en la plaza, con puestos, juegos y comida para todos.",
                "Va a empezar en la mañana y va a durar todo el día, así que hay tiempo de sobra para ir.",
                "Van a poder participar, no solo mirar, hay actividades para todas las edades."
            ),
            cierre = listOf(
                "Así que ya lo saben: este sábado, todos a la plaza.",
                "No se lo pierdan, porque va a estar muy divertido.",
                "¡Los espero ahí a todos!"
            )
        ),

        "MIRADOR_2" to bloques(
            gancho = listOf(
                "Tengo un anuncio, y esta vez es sobre cómo cuidamos nuestro parque.",
                "Antes de que sigan jugando, escuchen esto un momento.",
                "Esto nos afecta a todos los que usamos el parque."
            ),
            cuerpo = listOf(
                "A partir de ahora, vamos a tener una regla nueva para mantenerlo limpio y en buen estado.",
                "No es para complicarnos la vida, es para que el parque siga siendo un lugar donde todos quieran venir.",
                "Es algo sencillo de cumplir, y si todos colaboramos, se nota la diferencia enseguida."
            ),
            cierre = listOf(
                "Así que cuento con que todos la respeten, porque el parque es de todos.",
                "Entre todos lo mantenemos bonito, entre todos lo disfrutamos.",
                "Gracias por escuchar, y nos vemos cuidando el parque juntos."
            )
        ),

        "MIRADOR_3" to bloques(
            gancho = listOf(
                "Ya tenemos el resultado de la votación, y se los quiero contar bien.",
                "Todos votaron, y ahora toca anunciar lo que decidieron entre todos.",
                "Esto es lo que el salón entero decidió, no una sola persona."
            ),
            cuerpo = listOf(
                "Contamos los votos con cuidado, y el resultado salió así de claro.",
                "Algunos van a estar contentos con el resultado, y otros no tanto, y eso también está bien.",
                "Lo importante es que todos tuvieron la oportunidad de opinar antes de que se decidiera."
            ),
            cierre = listOf(
                "Así que este es el resultado final, y ahora seguimos adelante juntos con esta decisión.",
                "Gracias a todos por participar, eso es lo que más vale.",
                "La próxima vez que votemos, espero verlos a todos participar otra vez."
            )
        ),

        // ---------- La Tarima Mayor ----------

        "TARIMA_MAYOR_1" to bloques(
            gancho = listOf(
                "Este año que termina nos dejó más de lo que pensábamos al empezar.",
                "Hoy no es un día cualquiera: es el cierre de todo un año juntos.",
                "Antes de irnos de vacaciones, quiero decir unas palabras sobre este año."
            ),
            cuerpo = listOf(
                "Aprendimos cosas nuevas, nos equivocamos, lo volvimos a intentar, y al final lo logramos.",
                "No fue un año perfecto, pero fue un año real, con esfuerzo de todos los que estamos aquí.",
                "Cada uno de nosotros se va distinto de como llegó, y eso ya vale la pena celebrarlo."
            ),
            cierre = listOf(
                "Por eso, más que una despedida, esto es un hasta pronto.",
                "Nos vemos el próximo año, listos para un nuevo comienzo.",
                "¡Feliz fin de año para todos, nos lo ganamos!"
            )
        ),

        "TARIMA_MAYOR_2" to bloques(
            gancho = listOf(
                "Hoy es un día especial: inauguramos algo nuevo para todos.",
                "Llevábamos tiempo esperando este momento, y por fin llegó.",
                "Lo que hoy abrimos aquí no es solo un lugar, es un esfuerzo de muchos."
            ),
            cuerpo = listOf(
                "Esto se construyó pensando en todos los que vivimos y jugamos en esta plaza.",
                "No fue fácil llegar hasta aquí, pero cada esfuerzo valió la pena para este resultado.",
                "A partir de hoy, este lugar es de todos, y depende de todos cuidarlo."
            ),
            cierre = listOf(
                "Por eso, con mucho gusto, declaro esto abierto para toda la plaza.",
                "Que lo disfruten, que lo cuiden, y que lo hagan suyo.",
                "¡Bienvenidos a este nuevo espacio de todos!"
            )
        ),

        "TARIMA_MAYOR_3" to bloques(
            gancho = listOf(
                "Hoy quiero mostrarles algo en lo que estuve trabajando.",
                "Esto no lo hice de un día para otro, así que déjenme contarles cómo llegó hasta aquí.",
                "Toda la plaza está reunida, y por fin puedo presentarles mi proyecto."
            ),
            cuerpo = listOf(
                "La idea nació de un problema que noté y que quise resolver a mi manera.",
                "Tuve que intentarlo varias veces, corregir lo que no funcionaba, y seguir adelante igual.",
                "Lo que tengo ahora es el resultado de todo ese esfuerzo, y estoy listo para compartirlo."
            ),
            cierre = listOf(
                "Por eso, hoy, se los presento con mucho orgullo.",
                "Espero que les sirva tanto como me sirvió a mí hacerlo.",
                "Gracias por escucharme, y gracias por estar aquí para verlo."
            )
        )
    )

    /** Arma las 9 filas (3 GANCHO + 3 CUERPO + 3 CIERRE) de un tema a partir de sus 3
     * listas de 3 frases, asignando franja y orden (1..3 dentro de cada franja)
     * automaticamente para no repetir esas etiquetas a mano 189 veces. */
    private fun bloques(gancho: List<String>, cuerpo: List<String>, cierre: List<String>): List<BloqueSemilla> {
        require(gancho.size == 3 && cuerpo.size == 3 && cierre.size == 3) {
            "Cada franja debe tener exactamente 3 bloques"
        }
        return gancho.mapIndexed { i, texto -> BloqueSemilla("GANCHO", texto, i + 1) } +
            cuerpo.mapIndexed { i, texto -> BloqueSemilla("CUERPO", texto, i + 1) } +
            cierre.mapIndexed { i, texto -> BloqueSemilla("CIERRE", texto, i + 1) }
    }
}
