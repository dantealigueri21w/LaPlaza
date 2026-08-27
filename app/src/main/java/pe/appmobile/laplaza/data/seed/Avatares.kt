package pe.appmobile.laplaza.data.seed

/**
 * Los 8 avatares de perfil disponibles (arte real pendiente: tarea posterior, cuando se
 * genere el material grafico). Por ahora solo existen como el rango valido de
 * PerfilEntity.avatarId (1..8) para poder crear un perfil.
 */
object Avatares {
    val idsDisponibles = (1..8).toList()
}
