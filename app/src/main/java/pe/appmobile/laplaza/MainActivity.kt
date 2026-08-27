package pe.appmobile.laplaza

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.appmobile.laplaza.ui.LaPlazaViewModel
import pe.appmobile.laplaza.ui.LaPlazaViewModelFactory
import pe.appmobile.laplaza.ui.navigation.LaPlazaNavHost
import pe.appmobile.laplaza.ui.theme.LaPlazaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repositorio = (application as LaPlazaApplication).repositorio

        setContent {
            val viewModel: LaPlazaViewModel = viewModel(factory = LaPlazaViewModelFactory(repositorio))

            LaPlazaTheme {
                LaPlazaNavHost(viewModel = viewModel)
            }
        }
    }
}
