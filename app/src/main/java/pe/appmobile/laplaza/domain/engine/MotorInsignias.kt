package pe.appmobile.laplaza.domain.engine

import pe.appmobile.laplaza.domain.model.IdRincon
import pe.appmobile.laplaza.domain.model.IntentoParaInsignias

/**
 * Las 12 insignias de la ficha (24-LA-PLAZA.md, seccion "Insignias"). Evalua desde cero
 * sobre TODO el historial de intentos cada vez que se llama, en vez de llevar un
 * contador incremental aparte: mas simple, y no se puede desincronizar de lo que Room
 * ya tiene guardado. El repositorio (fuera de este motor, que no toca Room) es quien
 * llama a esto tras cada declamacion y marca en `insignia` las que aparecen aqui y
 * todavia no estaban marcadas.
 *
 * Simplificacion documentada (regla de la seccion 1 del maestro: "documenta exactamente
 * que simplificaste"): la ficha describe "Gancho que Prende" como lograr que la plaza
 * se voltee EN LOS PRIMEROS SEGUNDOS -- un dato que la app no guarda por tramos de
 * tiempo dentro de un intento, solo el promedio final. Se usa el volumen promedio del
 * intento completo como proxy: sigue siendo la misma variable (volumen), solo que sin
 * el recorte temporal exacto que el texto sugiere.
 *
 * Segunda simplificacion: las 6 insignias de "completar [rincon]" leen el mismo flag
 * `rincon.completado` que ya usa [pe.appmobile.laplaza.ui.LaPlazaViewModel.finalizarDeclamacion]
 * para marcar un rincon completo (cualquier declamacion real y exitosa en cualquiera de
 * sus temas) -- la misma definicion de "completar" en toda la app, no una mas estricta
 * solo para insignias.
 *
 * Rincon Libre (ver ficha, "no hay insignia en juego por el resultado"): todo intento
 * con [IntentoParaInsignias.viaRinconLibre] se excluye de las 11 insignias basadas en
 * resultado o hito, y SOLO cuenta para [COMPANERO_DE_CHIRRI].
 */
object MotorInsignias {
    const val PRIMERA_VOZ = "PRIMERA_VOZ"
    const val GANCHO_QUE_PRENDE = "GANCHO_QUE_PRENDE"
    const val CIERRE_DE_APLAUSOS = "CIERRE_DE_APLAUSOS"
    const val SIN_CORTES = "SIN_CORTES"
    const val BALCON_DOMINADO = "BALCON_DOMINADO"
    const val COMPANERO_DE_CHIRRI = "COMPANERO_DE_CHIRRI"

    private const val TEMAS_POR_RINCON = 3
    private const val UMBRAL_GANCHO_FUERTE = 0.7f
    private const val MINIMO_GANCHOS = 5
    private const val UMBRAL_APLAUSO_ALTO = 0.75f
    private const val MINIMO_APLAUSOS = 3
    private const val UMBRAL_FLUIDEZ_SIN_CORTES = 0.9f
    private const val MINIMO_USOS_LIBRE = 10

    private val INSIGNIA_DE_RINCON_COMPLETADO = mapOf(
        IdRincon.KIOSCO to "VOZ_DEL_KIOSCO",
        IdRincon.MOSTRADOR to "BUEN_VENDEDOR",
        IdRincon.MIRADOR to "SE_OYO_EN_TODA_LA_PLAZA",
        IdRincon.JARDIN to "GRATITUD_SINCERA",
        IdRincon.FUENTE to "BUEN_CONSUELO",
        IdRincon.TARIMA_MAYOR to "LA_TARIMA_ES_TUYA"
    )

    fun insigniasGanadas(
        intentos: List<IntentoParaInsignias>,
        rinconesCompletados: Set<IdRincon>
    ): Set<String> {
        val reales = intentos.filterNot { it.viaRinconLibre }
        val ganadas = mutableSetOf<String>()

        if (reales.isNotEmpty()) ganadas += PRIMERA_VOZ

        if (reales.count { it.puntajeVolumen >= UMBRAL_GANCHO_FUERTE } >= MINIMO_GANCHOS) {
            ganadas += GANCHO_QUE_PRENDE
        }

        if (reales.count { it.puntajeCompuesto >= UMBRAL_APLAUSO_ALTO } >= MINIMO_APLAUSOS) {
            ganadas += CIERRE_DE_APLAUSOS
        }

        if (reales.any { it.puntajeFluidez >= UMBRAL_FLUIDEZ_SIN_CORTES }) {
            ganadas += SIN_CORTES
        }

        val temasDeBalconConIntento = reales.filter { it.rinconId == IdRincon.BALCON.name }
            .map { it.temaId }
            .toSet()
        if (temasDeBalconConIntento.size >= TEMAS_POR_RINCON) {
            ganadas += BALCON_DOMINADO
        }

        INSIGNIA_DE_RINCON_COMPLETADO.forEach { (rincon, insigniaId) ->
            if (rincon in rinconesCompletados) ganadas += insigniaId
        }

        if (intentos.count { it.viaRinconLibre } >= MINIMO_USOS_LIBRE) {
            ganadas += COMPANERO_DE_CHIRRI
        }

        return ganadas
    }
}
