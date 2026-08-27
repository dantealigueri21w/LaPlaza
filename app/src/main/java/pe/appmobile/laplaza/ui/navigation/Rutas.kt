package pe.appmobile.laplaza.ui.navigation

/**
 * Las rutas de las 12 pantallas de la ficha (7 rincones + Rincon Libre + home + Cuaderno
 * de Pregones + perfil + ajustes) mas la creacion de perfil, que no es una de las 12
 * pantallas del juego sino un paso previo de primer arranque (seccion 2 de la tarea).
 *
 * Los 7 rincones y el Rincon Libre comparten una sola ruta parametrizada ([RINCON]),
 * cuyo destino real es [TemasDeRinconScreen][pe.appmobile.laplaza.ui.screens.TemasDeRinconScreen]
 * (la seleccion de tema). Elegir un tema navega a [ARMAR_DISCURSO], el tablero real de
 * armado del discurso (gancho/cuerpo/cierre). El Cuaderno de Pregones y la declamacion
 * en si ([DECLAMAR_PROXIMAMENTE]) siguen siendo el destino marcador (ver
 * ui/screens/PantallaMarcador.kt); una tarea posterior reemplaza cada uno por su
 * contenido real.
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
    const val ARMAR_DISCURSO = "armar/{$ARG_TEMA_ID}"

    fun armarDiscursoRuta(temaId: Long): String = "armar/$temaId"

    /** Destino marcador para la declamacion real (mic/Chirri/plaza), que es una tarea
     * posterior: [ArmarDiscursoScreen][pe.appmobile.laplaza.ui.screens.ArmarDiscursoScreen]
     * navega aqui al terminar de armar un discurso valido. */
    const val DECLAMAR_PROXIMAMENTE = "declamar_proximamente"
}
