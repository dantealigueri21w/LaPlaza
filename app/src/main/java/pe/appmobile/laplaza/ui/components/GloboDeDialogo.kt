package pe.appmobile.laplaza.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/**
 * Un globo de dialogo redondeado con una cola triangular apuntando hacia donde esta
 * Chirri. Reemplaza a AlertDialog para las lineas de Chirri -- seccion 3.1 del maestro:
 * este es un dialogo del personaje, no una alerta del sistema.
 */
private fun formaGloboDeDialogo(colaALaIzquierda: Boolean): Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val radio = minOf(w, h) * 0.18f
    val altoCola = h * 0.14f
    val anchoCola = w * 0.09f
    val altoCuerpo = h - altoCola
    val xCola = if (colaALaIzquierda) w * 0.20f else w * 0.80f

    moveTo(radio, 0f)
    lineTo(w - radio, 0f)
    quadraticTo(w, 0f, w, radio)
    lineTo(w, altoCuerpo - radio)
    quadraticTo(w, altoCuerpo, w - radio, altoCuerpo)
    lineTo(xCola + anchoCola, altoCuerpo)
    lineTo(xCola, h)
    lineTo(xCola - anchoCola, altoCuerpo)
    lineTo(radio, altoCuerpo)
    quadraticTo(0f, altoCuerpo, 0f, altoCuerpo - radio)
    lineTo(0f, radio)
    quadraticTo(0f, 0f, radio, 0f)
    close()
}

/**
 * Una linea de Chirri. [colaHaciaIzquierda] apunta la cola del globo hacia donde este
 * dibujado Chirri en la pantalla (por ejemplo, hacia la izquierda si Chirri esta a la
 * izquierda del globo).
 */
@Composable
fun GloboDeDialogo(
    texto: String,
    modifier: Modifier = Modifier,
    colaHaciaIzquierda: Boolean = true
) {
    val forma = remember(colaHaciaIzquierda) { formaGloboDeDialogo(colaHaciaIzquierda) }

    Box(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = forma, clip = false)
            .clip(forma)
            .background(BlancoRosado)
            .border(width = 2.dp, color = IndigoProfundo, shape = forma)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 12.dp)
            .semantics { contentDescription = "Chirri dice: $texto" }
    ) {
        Text(
            text = texto,
            color = IndigoProfundo,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
