package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.seed.Avatares
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.Chirri
import pe.appmobile.laplaza.ui.components.EstadoChirri
import pe.appmobile.laplaza.ui.components.GloboDeDialogo
import pe.appmobile.laplaza.ui.components.SelectorDeAvatares
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/** Longitud maxima razonable de un apodo -- no viene de la ficha, es una salvaguarda
 * practica para que el alias siempre quepa donde se muestre (home, perfil). */
private const val LARGO_MAXIMO_ALIAS = 16

/** Tag solo para pruebas: OutlinedTextField no tiene un texto propio unico y estable
 * para ubicarlo por contenido antes de escribir (su label deja de mostrarse igual apenas
 * el campo tiene foco/texto), asi que un testTag es mas confiable que buscarlo por texto. */
const val TAG_CAMPO_ALIAS = "campo_alias_crear_perfil"

/**
 * El paso de primer arranque: elegir un apodo (nunca el nombre real -- la ficha es
 * explicita en esto) y una de las 8 caras. No es el onboarding narrativo de "los
 * primeros 30 segundos" de la ficha (eso va de la mano del primer rincon real, en una
 * tarea posterior); es solo el paso practico para poder crear un [pe.appmobile.laplaza.data.local.entity.PerfilEntity].
 *
 * Pantalla sin estado propio de persistencia: [onCrear] recibe el alias y el avatarId
 * elegidos, y quien la use decide que hacer con ellos (llamar al ViewModel y navegar).
 */
@Composable
fun CrearPerfilScreen(
    onCrear: (alias: String, avatarId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var alias by remember { mutableStateOf("") }
    var avatarSeleccionado by remember { mutableStateOf(Avatares.idsDisponibles.first()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Chirri(estado = EstadoChirri.SALUDANDO, modifier = Modifier.size(150.dp))
        Spacer(modifier = Modifier.height(8.dp))
        GloboDeDialogo(texto = stringResource(R.string.crear_perfil_saludo))

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.crear_perfil_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = IndigoProfundo
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = { nuevoValor ->
                if (nuevoValor.length <= LARGO_MAXIMO_ALIAS) alias = nuevoValor
            },
            singleLine = true,
            label = { Text(stringResource(R.string.crear_perfil_hint_alias)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TAG_CAMPO_ALIAS)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.crear_perfil_elige_avatar),
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
        BotonDePlaza(
            label = stringResource(R.string.crear_perfil_confirmar),
            onClick = { onCrear(alias.trim(), avatarSeleccionado) },
            enabled = alias.isNotBlank()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
