package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.SelectorDeAvatares
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/**
 * Ver/editar el perfil ya creado -- distinta de [CrearPerfilScreen], que solo existe una
 * vez, en el primer arranque. Reutiliza el mismo [SelectorDeAvatares] que la creacion.
 *
 * Sin estado propio de persistencia: [onGuardar] recibe el alias y avatarId editados, y
 * quien la use decide como guardarlos (el ViewModel real, en el NavHost).
 */
@Composable
fun PerfilScreen(
    perfil: PerfilEntity,
    onGuardar: (alias: String, avatarId: Int) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var alias by remember(perfil.id) { mutableStateOf(perfil.alias) }
    var avatarSeleccionado by remember(perfil.id) { mutableStateOf(perfil.avatarId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.perfil_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = IndigoProfundo
        )

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            singleLine = true,
            label = { Text(stringResource(R.string.perfil_editar_alias)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.perfil_elige_avatar),
            style = MaterialTheme.typography.titleLarge,
            color = IndigoProfundo
        )
        Spacer(modifier = Modifier.height(8.dp))
        SelectorDeAvatares(
            seleccionado = avatarSeleccionado,
            onSeleccionar = { avatarSeleccionado = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row {
            BotonDePlaza(
                label = stringResource(R.string.perfil_guardar),
                onClick = { onGuardar(alias.trim().ifBlank { perfil.alias }, avatarSeleccionado) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            BotonDePlaza(label = stringResource(R.string.perfil_volver), onClick = onVolver)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
