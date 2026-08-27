package pe.appmobile.laplaza.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.seed.Avatares
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/**
 * La cuadricula manual (chunked(4) en Row, nunca LazyVerticalGrid -- seccion 7.1 punto 6
 * del maestro: 8 items es una cantidad chica y fija) para elegir uno de los 8 avatares.
 * La usan tanto la creacion de perfil como la edicion de perfil, para no duplicar la
 * logica de seleccion en dos pantallas.
 *
 * Cada fila de 4 puede necesitar mas ancho del que cabe en un telefono angosto: cuatro
 * objetivos de toque de 120dp reales, uno al lado del otro, ya suman 480dp solo de
 * contenido -- mas que el ancho de la mayoria de telefonos en dp. En vez de encoger el
 * objetivo de toque por debajo del minimo de accesibilidad, cada fila hace scroll
 * horizontal propio (como un carrusel) para que las 4 opciones sigan siendo full-size.
 */
@Composable
fun SelectorDeAvatares(
    seleccionado: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Avatares.idsDisponibles.chunked(4).forEach { fila ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fila.forEach { avatarId ->
                    ItemAvatarSeleccionable(
                        avatarId = avatarId,
                        seleccionado = avatarId == seleccionado,
                        onClick = { onSeleccionar(avatarId) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/** Un avatar tocable de al menos 120dp -- es contenido que el nino toca para jugar
 * (elegir con quien juega), no un icono de ajustes. Se distingue del resto no solo por
 * color sino por un borde y una insignia con una marca real cuando esta elegido. */
@Composable
private fun ItemAvatarSeleccionable(avatarId: Int, seleccionado: Boolean, onClick: () -> Unit) {
    val nombreBase = stringResource(R.string.avatar_descripcion, avatarId)
    val sufijoElegido = stringResource(R.string.avatar_elegido_sufijo)
    val descripcionCompleta = if (seleccionado) nombreBase + sufijoElegido else nombreBase

    // Tamano FIJO (no sizeIn + fillMaxSize del contenido): dentro de un Row con scroll
    // horizontal los hijos reciben ancho maximo no acotado (Infinity), y un
    // Modifier.fillMaxSize() interno no puede resolver eso -- de ahi que el tamano tenga
    // que quedar fijo aqui afuera y el avatar de adentro use un tamano explicito, nunca
    // "llenar lo que haya".
    Box(
        modifier = Modifier
            .size(120.dp)
            .then(
                if (seleccionado) {
                    Modifier.border(width = 3.dp, color = IndigoProfundo, shape = CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClickLabel = nombreBase, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcionCompleta }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        AvatarDePlaza(avatarId = avatarId, modifier = Modifier.size(90.dp))

        if (seleccionado) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .background(AmbarFarol, CircleShape)
                    .border(width = 1.5.dp, color = IndigoProfundo, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = IndigoProfundo,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
