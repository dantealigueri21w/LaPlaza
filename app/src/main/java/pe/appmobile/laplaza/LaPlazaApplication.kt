package pe.appmobile.laplaza

import android.app.Application
import androidx.room.Room
import pe.appmobile.laplaza.data.local.AppDatabase
import pe.appmobile.laplaza.data.repository.LaPlazaRepository

/**
 * Construccion manual de la base de datos y el repositorio -- este proyecto no usa un
 * framework de inyeccion de dependencias (Hilt/Koin). AppDatabase y LaPlazaRepository ya
 * estan hechos (ver data/local y data/repository); esta clase es simplemente quien los
 * arma una sola vez, en el arranque del proceso, para que MainActivity los pueda pasar
 * al ViewModel a traves de [pe.appmobile.laplaza.ui.LaPlazaViewModelFactory].
 */
class LaPlazaApplication : Application() {

    lateinit var repositorio: LaPlazaRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val baseDeDatos = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "la_plaza.db"
        )
            // La app todavia no tiene usuarios reales con datos que preservar entre
            // versiones de esquema: mas simple que escribir una Migration real por cada
            // cambio en esta etapa. Revisar antes de la primera entrega con datos reales.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

        repositorio = LaPlazaRepository(
            perfilDao = baseDeDatos.perfilDao(),
            rinconDao = baseDeDatos.rinconDao(),
            temaDao = baseDeDatos.temaDao(),
            bloqueContenidoDao = baseDeDatos.bloqueContenidoDao(),
            intentoDao = baseDeDatos.intentoDao(),
            pregonDao = baseDeDatos.pregonDao(),
            insigniaDao = baseDeDatos.insigniaDao(),
            rachaDao = baseDeDatos.rachaDao()
        )
    }
}
