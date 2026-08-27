package pe.appmobile.laplaza.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import pe.appmobile.laplaza.data.local.dao.BloqueContenidoDao
import pe.appmobile.laplaza.data.local.dao.InsigniaDao
import pe.appmobile.laplaza.data.local.dao.IntentoDao
import pe.appmobile.laplaza.data.local.dao.PerfilDao
import pe.appmobile.laplaza.data.local.dao.PregonDao
import pe.appmobile.laplaza.data.local.dao.RachaDao
import pe.appmobile.laplaza.data.local.dao.RinconDao
import pe.appmobile.laplaza.data.local.dao.TemaDao
import pe.appmobile.laplaza.data.local.entity.BloqueContenidoEntity
import pe.appmobile.laplaza.data.local.entity.InsigniaEntity
import pe.appmobile.laplaza.data.local.entity.IntentoEntity
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.PregonEntity
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.data.local.entity.TemaEntity

@Database(
    entities = [
        PerfilEntity::class, RinconEntity::class, TemaEntity::class,
        BloqueContenidoEntity::class, IntentoEntity::class, PregonEntity::class,
        InsigniaEntity::class, RachaEntity::class
    ],
    // v2: agrega IntentoEntity.viaRinconLibre (ver LaPlazaApplication -- destructiva a
    // proposito, la app todavia no tiene usuarios reales con datos que preservar).
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
    abstract fun rinconDao(): RinconDao
    abstract fun temaDao(): TemaDao
    abstract fun bloqueContenidoDao(): BloqueContenidoDao
    abstract fun intentoDao(): IntentoDao
    abstract fun pregonDao(): PregonDao
    abstract fun insigniaDao(): InsigniaDao
    abstract fun rachaDao(): RachaDao
}
