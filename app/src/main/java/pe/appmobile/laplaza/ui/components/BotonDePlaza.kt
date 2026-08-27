package pe.appmobile.laplaza.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/** Tamano minimo de cualquier objeto que el nino toca para jugar (no para ajustes). */
private val TamanoMinimoTapTarget = 120.dp

/**
 * Reemplaza a Button para las acciones primarias que el nino toca para jugar -nunca
 * para acciones secundarias como ajustes, que si pueden seguir usando Material Button-
 * seccion 3.1 del maestro. El objeto tocable es un farol que se ve "encendido" cuando
 * [enabled] es true, en eco directo del progreso de la propia plaza: cada logro
 * enciende un farol mas.
 *
 * El minimo de 120dp se aplica por FUERA del [modifier] del llamador a proposito: si
 * alguien pasa un `Modifier.size()` mas chico por error, este componente lo corrige
 * hacia arriba en vez de dejar pasar un objetivo de toque demasiado pequeno para un
 * nino (regla de accesibilidad verificada como ausente en 15 de 17 apps hermanas).
 */
@Composable
fun BotonDePlaza(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    descripcion: String = label
) {
    val escala by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.96f,
        label = "escalaBotonDePlaza"
    )

    Box(
        modifier = Modifier
            .sizeIn(minWidth = TamanoMinimoTapTarget, minHeight = TamanoMinimoTapTarget)
            .then(modifier)
            .defaultMinSize(minWidth = 140.dp, minHeight = 140.dp)
            .scale(escala)
            .clickable(
                enabled = enabled,
                onClickLabel = label,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            dibujarFarol(encendido = enabled)
        }
        Text(
            text = label,
            color = IndigoProfundo,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

/** Un farol de plaza: anilla, techo, jaula de vidrio con dos barras, y base. */
private fun DrawScope.dibujarFarol(encendido: Boolean) {
    val ancho = size.width
    val alto = size.height
    val centroX = ancho / 2f
    val colorVidrio = if (encendido) AmbarFarol else AmbarFarol.copy(alpha = 0.3f)
    val colorMetal = IndigoProfundo

    // resplandor cuando esta encendido: circulos concentricos de alpha decreciente en
    // vez de un blur real, para no exigir RenderEffect (API 31+) con minSdk 24.
    if (encendido) {
        val centroResplandor = Offset(centroX, alto * 0.42f)
        drawCircle(color = AmbarFarol.copy(alpha = 0.18f), radius = ancho * 0.42f, center = centroResplandor)
        drawCircle(color = AmbarFarol.copy(alpha = 0.28f), radius = ancho * 0.32f, center = centroResplandor)
    }

    drawCircle(
        color = colorMetal,
        radius = ancho * 0.05f,
        center = Offset(centroX, alto * 0.08f),
        style = Stroke(width = ancho * 0.02f)
    )

    val techo = Path().apply {
        moveTo(centroX - ancho * 0.30f, alto * 0.22f)
        lineTo(centroX + ancho * 0.30f, alto * 0.22f)
        lineTo(centroX + ancho * 0.18f, alto * 0.14f)
        lineTo(centroX - ancho * 0.18f, alto * 0.14f)
        close()
    }
    drawPath(techo, color = colorMetal)

    val vidrioTopLeft = Offset(centroX - ancho * 0.26f, alto * 0.22f)
    val vidrioSize = Size(ancho * 0.52f, alto * 0.46f)
    val esquinaVidrio = CornerRadius(ancho * 0.06f, ancho * 0.06f)
    drawRoundRect(color = colorVidrio, topLeft = vidrioTopLeft, size = vidrioSize, cornerRadius = esquinaVidrio)
    drawRoundRect(
        color = colorMetal,
        topLeft = vidrioTopLeft,
        size = vidrioSize,
        cornerRadius = esquinaVidrio,
        style = Stroke(width = ancho * 0.018f)
    )

    val xIzq = vidrioTopLeft.x + vidrioSize.width * 0.33f
    val xDer = vidrioTopLeft.x + vidrioSize.width * 0.66f
    drawLine(
        color = colorMetal,
        start = Offset(xIzq, vidrioTopLeft.y),
        end = Offset(xIzq, vidrioTopLeft.y + vidrioSize.height),
        strokeWidth = ancho * 0.012f
    )
    drawLine(
        color = colorMetal,
        start = Offset(xDer, vidrioTopLeft.y),
        end = Offset(xDer, vidrioTopLeft.y + vidrioSize.height),
        strokeWidth = ancho * 0.012f
    )

    drawRoundRect(
        color = colorMetal,
        topLeft = Offset(centroX - ancho * 0.16f, vidrioTopLeft.y + vidrioSize.height),
        size = Size(ancho * 0.32f, alto * 0.07f),
        cornerRadius = CornerRadius(ancho * 0.02f, ancho * 0.02f)
    )
}
