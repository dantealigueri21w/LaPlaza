package pe.appmobile.laplaza.data.seed

import pe.appmobile.laplaza.data.local.entity.RinconEntity

/**
 * Los 7 rincones con contenido propio. LIBRE existe como estado de desbloqueo en el
 * enum de dominio IdRincon, pero no tiene fila sembrada aqui: la app lo maneja aparte,
 * reutilizando los temas de los otros rincones para practica libre en vez de tener
 * contenido exclusivo.
 */
object SemillaRincones {
    val rincones = listOf(
        RinconEntity(id = "BALCON", nombre = "El Balcón", descripcion = "Presentarte ante la plaza", orden = 1),
        RinconEntity(id = "KIOSCO", nombre = "El Kiosco", descripcion = "Contar algo que pasó", orden = 2),
        RinconEntity(id = "MOSTRADOR", nombre = "El Mostrador", descripcion = "Pedir algo con buenas razones", orden = 3),
        RinconEntity(id = "JARDIN", nombre = "El Jardín", descripcion = "Agradecer o reconocer a alguien", orden = 4),
        RinconEntity(id = "FUENTE", nombre = "La Fuente", descripcion = "Dar ánimo o consuelo", orden = 5),
        RinconEntity(id = "MIRADOR", nombre = "El Mirador", descripcion = "Anunciar algo a todos", orden = 6),
        RinconEntity(id = "TARIMA_MAYOR", nombre = "La Tarima Mayor", descripcion = "Discurso completo de ocasión especial", orden = 7)
    )
}
