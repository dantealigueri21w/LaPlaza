package pe.appmobile.laplaza.ui.navigation

/**
 * Las rutas de las 12 pantallas de la ficha (7 rincones + Rincon Libre + home + Cuaderno
 * de Pregones + perfil + ajustes) mas la creacion de perfil, que no es una de las 12
 * pantallas del juego sino un paso previo de primer arranque (seccion 2 de la tarea).
 *
 * Los 7 rincones y el Rincon Libre comparten una sola ruta parametrizada ([RINCON]),
 * cuyo destino real es [TemasDeRinconScreen][pe.appmobile.laplaza.ui.screens.TemasDeRinconScreen]
 * (la seleccion de tema). Elegir un tema navega a [ARMAR_DISCURSO], el tablero real de
 * armado del discurso (gancho/cuerpo/cierre), que a su vez navega a [DECLAMACION] -la
 * mecanica nucleo: microfono real, indicador en vivo y pregon real.
 */
object Rutas {
    const val CREAR_PERFIL = "crear_perfil"
    const val HOME = "home"
    const val PERFIL = "perfil"
    const val AJUSTES = "ajustes"
    const val CUADERNO_DE_PREGONES = "cuaderno_pregones"

    const val ARG_RINCON_ID = "rinconId"
    const val RINCON = "rincon/{$ARG_RINCON_ID}"

    /** El id de Rincon Libre no tiene fila en la tabla `rincon` (ver SemillaRincones.kt):
     * existe solo como destino de navegacion, reutilizando temas ya vistos. */
    const val ID_RINCON_LIBRE = "LIBRE"

    fun rinconRuta(rinconId: String): String = "rincon/$rinconId"

    const val ARG_TEMA_ID = "temaId"

    /** true si se entro por el Rincon Libre -viaja pegado al temaId por las dos rutas
     * siguientes (Navigation Compose no lleva el rinconId de origen de otra forma una
     * vez elegido el tema) porque decide, en [DECLAMACION], si el intento cuenta para
     * las 11 insignias por resultado o solo para Companero de Chirri (ver la ficha,
     * seccion "Rincon Libre", y [pe.appmobile.laplaza.domain.engine.MotorInsignias]). */
    const val ARG_VIA_LIBRE = "viaLibre"

    const val ARMAR_DISCURSO = "armar/{$ARG_TEMA_ID}/{$ARG_VIA_LIBRE}"

    fun armarDiscursoRuta(temaId: Long, viaLibre: Boolean): String = "armar/$temaId/$viaLibre"

    /** La pantalla de declamacion real (microfono, indicador en vivo, pregon). El
     * discurso armado en si no viaja por la ruta -Navigation Compose solo pasa
     * primitivos- sino por [pe.appmobile.laplaza.ui.LaPlazaViewModel.prepararDeclamacion],
     * llamado justo antes de navegar aqui; esta pantalla solo necesita [ARG_TEMA_ID] para
     * saber a que tema pertenece (por ejemplo, para volver a cargar datos si el proceso
     * murio entre medio). Reutiliza [ARG_TEMA_ID], el mismo nombre de argumento que
     * [ARMAR_DISCURSO]. */
    const val DECLAMACION = "declamacion/{$ARG_TEMA_ID}/{$ARG_VIA_LIBRE}"

    fun declamacionRuta(temaId: Long, viaLibre: Boolean): String = "declamacion/$temaId/$viaLibre"
}
