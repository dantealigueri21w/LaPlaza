package pe.appmobile.laplaza.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pe.appmobile.laplaza.data.repository.LaPlazaRepository

/**
 * Fabrica manual de [LaPlazaViewModel] -- este proyecto no usa Hilt ni Koin, asi que
 * quien construye la Activity/Composable raiz debe pasar el [LaPlazaRepository] ya
 * armado (ver [pe.appmobile.laplaza.LaPlazaApplication]).
 */
class LaPlazaViewModelFactory(private val repositorio: LaPlazaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(LaPlazaViewModel::class.java)) {
            "LaPlazaViewModelFactory solo sabe construir LaPlazaViewModel, no $modelClass"
        }
        return LaPlazaViewModel(repositorio) as T
    }
}
