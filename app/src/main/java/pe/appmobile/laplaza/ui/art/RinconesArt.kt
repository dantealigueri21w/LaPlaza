package pe.appmobile.laplaza.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo
import pe.appmobile.laplaza.ui.theme.RosaBerenjena
import pe.appmobile.laplaza.ui.theme.VioletaAtardecer

/**
 * Un simbolo pequeño y propio para cada uno de los 7 rincones + Rincon Libre, para que
 * las 8 zonas del mapa de la plaza se distingan de un vistazo entre si -no solo por el
 * nombre en texto- seccion 3 del maestro ("cada app debe parecer de una empresa
 * distinta... si se ponen varias juntas deben distinguirse sin leer nombres" aplica
 * tambien puertas adentro, entre las zonas de una misma app). Mismo marco circular con
 * degradado y sombra en las 8 (seccion 4.0), solo el glifo interior cambia.
 */
private val TAMANO_SIMBOLO = 34.dp

@Composable
private fun MarcoDeSimboloRincon(modifier: Modifier = Modifier, glifo: DrawScope.() -> Unit) {
    Canvas(modifier = modifier.size(TAMANO_SIMBOLO)) {
        val centro = Offset(size.width / 2f, size.height / 2f)
        val radio = size.minDimension * 0.46f
        drawCircle(color = IndigoProfundo.copy(alpha = 0.18f), radius = radio * 1.08f, center = centro + Offset(0f, radio * 0.12f))
        drawCircle(
            brush = Brush.radialGradient(colors = listOf(BlancoRosado, VioletaAtardecer.copy(alpha = 0.25f)), center = centro, radius = radio * 1.4f),
            radius = radio,
            center = centro
        )
        drawCircle(color = IndigoProfundo, radius = radio, center = centro, style = Stroke(width = radio * 0.09f))
        glifo()
    }
}

@Composable
fun IlustracionRinconBalcon(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.24f
    drawLine(color = IndigoProfundo, start = c + Offset(-r, -r * 0.4f), end = c + Offset(r, -r * 0.4f), strokeWidth = r * 0.18f)
    for (i in -2..2) {
        drawLine(color = IndigoProfundo, start = c + Offset(i * r * 0.4f, -r * 0.4f), end = c + Offset(i * r * 0.4f, r * 0.5f), strokeWidth = r * 0.1f)
    }
}

@Composable
fun IlustracionRinconKiosco(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.24f
    val techo = Path().apply {
        moveTo(c.x - r, c.y - r * 0.1f)
        lineTo(c.x, c.y - r)
        lineTo(c.x + r, c.y - r * 0.1f)
        close()
    }
    drawPath(techo, brush = Brush.linearGradient(listOf(AmbarFarol, RosaBerenjena)))
    drawRect(color = IndigoProfundo, topLeft = c + Offset(-r * 0.55f, -r * 0.1f), size = Size(r * 1.1f, r * 0.75f), style = Stroke(width = r * 0.08f))
}

@Composable
fun IlustracionRinconMostrador(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.24f
    drawRoundRect(
        brush = Brush.linearGradient(listOf(RosaBerenjena, VioletaAtardecer)),
        topLeft = c + Offset(-r, -r * 0.1f),
        size = Size(r * 2f, r * 0.7f),
        cornerRadius = CornerRadius(r * 0.15f, r * 0.15f)
    )
    drawLine(color = IndigoProfundo, start = c + Offset(-r * 0.9f, -r * 0.1f), end = c + Offset(-r * 0.9f, -r * 0.6f), strokeWidth = r * 0.12f)
    drawLine(color = IndigoProfundo, start = c + Offset(r * 0.9f, -r * 0.1f), end = c + Offset(r * 0.9f, -r * 0.6f), strokeWidth = r * 0.12f)
}

@Composable
fun IlustracionRinconJardin(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.2f
    for (i in 0 until 5) {
        val angulo = (2.0 * Math.PI * i / 5 - Math.PI / 2).toFloat()
        val petalo = Path().apply {
            val punta = c + Offset(r * 1.1f * kotlin.math.cos(angulo), r * 1.1f * kotlin.math.sin(angulo))
            moveTo(c.x, c.y)
            quadraticTo(c.x + r * 0.5f * kotlin.math.cos(angulo + 0.4f), c.y + r * 0.5f * kotlin.math.sin(angulo + 0.4f), punta.x, punta.y)
            quadraticTo(c.x + r * 0.5f * kotlin.math.cos(angulo - 0.4f), c.y + r * 0.5f * kotlin.math.sin(angulo - 0.4f), c.x, c.y)
            close()
        }
        drawPath(petalo, color = RosaBerenjena.copy(alpha = 0.85f))
    }
    drawCircle(color = AmbarFarol, radius = r * 0.35f, center = c)
}

@Composable
fun IlustracionRinconFuente(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.22f
    drawOval(brush = Brush.linearGradient(listOf(VioletaAtardecer, VioletaAtardecer.copy(alpha = 0.5f))), topLeft = c + Offset(-r, r * 0.35f), size = Size(r * 2f, r * 0.5f))
    val gota = Path().apply {
        moveTo(c.x, c.y - r)
        cubicTo(c.x + r * 0.6f, c.y - r * 0.1f, c.x + r * 0.35f, c.y + r * 0.4f, c.x, c.y + r * 0.4f)
        cubicTo(c.x - r * 0.35f, c.y + r * 0.4f, c.x - r * 0.6f, c.y - r * 0.1f, c.x, c.y - r)
        close()
    }
    drawPath(gota, color = VioletaAtardecer)
}

@Composable
fun IlustracionRinconMirador(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.22f
    drawArc(
        color = IndigoProfundo,
        startAngle = -50f,
        sweepAngle = 100f,
        useCenter = false,
        topLeft = c - Offset(r, r * 0.8f),
        size = Size(r * 2f, r * 1.6f),
        style = Stroke(width = r * 0.16f)
    )
    drawCircle(color = AmbarFarol, radius = r * 0.22f, center = c - Offset(r * 0.75f, 0f))
}

@Composable
fun IlustracionRinconTarimaMayor(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.24f
    drawRoundRect(
        brush = Brush.linearGradient(listOf(AmbarFarol, RosaBerenjena)),
        topLeft = c + Offset(-r, r * 0.15f),
        size = Size(r * 2f, r * 0.4f),
        cornerRadius = CornerRadius(r * 0.06f, r * 0.06f)
    )
    val estrella = Path().apply {
        moveTo(c.x, c.y - r)
        lineTo(c.x + r * 0.22f, c.y - r * 0.25f)
        lineTo(c.x + r * 0.9f, c.y - r * 0.2f)
        lineTo(c.x + r * 0.3f, c.y + r * 0.15f)
        lineTo(c.x + r * 0.5f, c.y + r * 0.8f)
        lineTo(c.x, c.y + r * 0.35f)
        lineTo(c.x - r * 0.5f, c.y + r * 0.8f)
        lineTo(c.x - r * 0.3f, c.y + r * 0.15f)
        lineTo(c.x - r * 0.9f, c.y - r * 0.2f)
        lineTo(c.x - r * 0.22f, c.y - r * 0.25f)
        close()
    }
    drawPath(estrella, color = IndigoProfundo)
}

@Composable
fun IlustracionRinconLibre(modifier: Modifier = Modifier) = MarcoDeSimboloRincon(modifier) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.22f
    val alas = Path().apply {
        moveTo(c.x, c.y)
        cubicTo(c.x - r * 0.3f, c.y - r * 1.1f, c.x - r * 1.2f, c.y - r * 0.9f, c.x - r * 1.1f, c.y - r * 0.2f)
        cubicTo(c.x - r * 0.6f, c.y - r * 0.35f, c.x - r * 0.25f, c.y - r * 0.15f, c.x, c.y)
        cubicTo(c.x + r * 0.3f, c.y - r * 1.1f, c.x + r * 1.2f, c.y - r * 0.9f, c.x + r * 1.1f, c.y - r * 0.2f)
        cubicTo(c.x + r * 0.6f, c.y - r * 0.35f, c.x + r * 0.25f, c.y - r * 0.15f, c.x, c.y)
        close()
    }
    drawPath(alas, brush = Brush.radialGradient(listOf(AmbarFarol, RosaBerenjena), center = c, radius = r * 1.6f))
}
