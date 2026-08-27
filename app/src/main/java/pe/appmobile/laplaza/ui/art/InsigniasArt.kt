package pe.appmobile.laplaza.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo
import pe.appmobile.laplaza.ui.theme.RosaBerenjena
import pe.appmobile.laplaza.ui.theme.VioletaAtardecer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Las 12 insignias reales de la ficha (24-LA-PLAZA.md, seccion "Insignias"), dibujadas
 * en Canvas -- seccion 4.0 del maestro: mismo marco de medalla en las 12 (sombra,
 * degradado radial en el disco, anillo con degradado, listones con curvas), cada una
 * distinta solo por el glifo interior. [ganada] decide el color (dorado/vivo vs. un gris
 * apagado) Y agrega un pequeno candado -nunca solo el color, seccion 6 del maestro- para
 * que una insignia todavia no ganada se distinga sin depender de ver el color.
 */
private val TAMANO_POR_DEFECTO = 96.dp

@Composable
private fun MarcoDeInsignia(
    ganada: Boolean,
    modifier: Modifier = Modifier,
    glifo: DrawScope.(color: Color) -> Unit
) {
    Canvas(modifier = modifier.size(TAMANO_POR_DEFECTO)) {
        dibujarMarcoDeInsignia(ganada, glifo)
    }
}

private fun DrawScope.dibujarMarcoDeInsignia(ganada: Boolean, glifo: DrawScope.(color: Color) -> Unit) {
    val centro = Offset(size.width / 2f, size.height / 2f)
    val radioDisco = size.minDimension * 0.36f

    val colorPrincipal = if (ganada) AmbarFarol else IndigoProfundo.copy(alpha = 0.28f)
    val colorSecundario = if (ganada) RosaBerenjena else IndigoProfundo.copy(alpha = 0.18f)
    val colorGlifo = if (ganada) IndigoProfundo else IndigoProfundo.copy(alpha = 0.45f)

    // Listones: dos formas con cubicTo colgando bajo el disco -- capa 1.
    listOf(-1f, 1f).forEach { lado ->
        val liston = Path().apply {
            val xBase = centro.x + lado * radioDisco * 0.32f
            moveTo(xBase, centro.y + radioDisco * 0.55f)
            cubicTo(
                xBase + lado * radioDisco * 0.18f, centro.y + radioDisco * 1.35f,
                xBase - lado * radioDisco * 0.05f, centro.y + radioDisco * 1.55f,
                xBase + lado * radioDisco * 0.02f, centro.y + radioDisco * 1.85f
            )
            lineTo(xBase - lado * radioDisco * 0.22f, centro.y + radioDisco * 1.7f)
            cubicTo(
                xBase - lado * radioDisco * 0.28f, centro.y + radioDisco * 1.3f,
                xBase - lado * radioDisco * 0.30f, centro.y + radioDisco * 0.9f,
                xBase - lado * radioDisco * 0.28f, centro.y + radioDisco * 0.55f
            )
            close()
        }
        drawPath(liston, color = colorSecundario)
    }

    // Sombra de contacto -- capa 2.
    drawCircle(
        color = IndigoProfundo.copy(alpha = 0.16f),
        radius = radioDisco * 1.08f,
        center = centro + Offset(0f, radioDisco * 0.12f)
    )

    // Disco con degradado radial -- capa 3.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colorPrincipal, colorSecundario),
            center = centro - Offset(radioDisco * 0.25f, radioDisco * 0.25f),
            radius = radioDisco * 1.6f
        ),
        radius = radioDisco,
        center = centro
    )

    // Anillo con degradado -- capa 4.
    drawCircle(
        brush = Brush.sweepGradient(
            colors = if (ganada) {
                listOf(AmbarFarol, RosaBerenjena, VioletaAtardecer, AmbarFarol)
            } else {
                listOf(IndigoProfundo.copy(alpha = 0.35f), IndigoProfundo.copy(alpha = 0.15f), IndigoProfundo.copy(alpha = 0.35f))
            },
            center = centro
        ),
        radius = radioDisco,
        center = centro,
        style = Stroke(width = radioDisco * 0.09f)
    )

    glifo(colorGlifo)

    if (!ganada) {
        dibujarCandado(centro + Offset(radioDisco * 0.55f, radioDisco * 0.55f), radioDisco * 0.3f)
    }
}

private fun DrawScope.dibujarCandado(centro: Offset, radio: Float) {
    drawCircle(color = BlancoRosado, radius = radio * 1.15f, center = centro)
    val arco = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                center = centro - Offset(0f, radio * 0.35f),
                radius = radio * 0.5f
            )
        )
    }
    drawPath(arco, color = IndigoProfundo, style = Stroke(width = radio * 0.16f))
    drawRoundRect(
        color = IndigoProfundo,
        topLeft = centro + Offset(-radio * 0.55f, -radio * 0.1f),
        size = androidx.compose.ui.geometry.Size(radio * 1.1f, radio * 0.85f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radio * 0.15f, radio * 0.15f)
    )
}

/** Estrella de N puntas via `quadraticTo` -- reutilizada por varios glifos. */
private fun trazarEstrella(centro: Offset, radioExterior: Float, radioInterior: Float, puntas: Int = 5): Path {
    fun punto(indice: Int): Offset {
        val radio = if (indice % 2 == 0) radioExterior else radioInterior
        val angulo = (Math.PI / puntas * indice - Math.PI / 2).toFloat()
        return Offset(centro.x + radio * cos(angulo), centro.y + radio * sin(angulo))
    }
    return Path().apply {
        moveTo(punto(0).x, punto(0).y)
        for (i in 1..puntas * 2) {
            val actual = punto(i % (puntas * 2))
            val anterior = punto(i - 1)
            val control = Offset((anterior.x + actual.x) / 2f, (anterior.y + actual.y) / 2f)
            quadraticTo(anterior.x, anterior.y, control.x, control.y)
        }
        close()
    }
}

@Composable
fun InsigniaPrimeraVoz(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val radioBase = size.minDimension * 0.15f
    for (i in 1..3) {
        drawArc(
            color = color.copy(alpha = 0.9f - i * 0.2f),
            startAngle = -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = centro - Offset(radioBase * i, radioBase * i * 0.8f),
            size = androidx.compose.ui.geometry.Size(radioBase * i * 2, radioBase * i * 1.6f),
            style = Stroke(width = radioBase * 0.22f)
        )
    }
    drawCircle(color = color, radius = radioBase * 0.5f, center = centro - Offset(radioBase * 0.9f, 0f))
}

@Composable
fun InsigniaGanchoQuePrende(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.22f
    val chispa = Path().apply {
        moveTo(centro.x + r * 0.15f, centro.y - r)
        cubicTo(centro.x + r * 0.9f, centro.y - r * 0.2f, centro.x + r * 0.1f, centro.y - r * 0.1f, centro.x + r * 0.6f, centro.y + r)
        cubicTo(centro.x - r * 0.1f, centro.y + r * 0.5f, centro.x - r * 0.5f, centro.y + r * 0.3f, centro.x - r * 0.15f, centro.y - r * 0.3f)
        cubicTo(centro.x - r * 0.4f, centro.y - r * 0.15f, centro.x - r * 0.2f, centro.y - r * 0.85f, centro.x + r * 0.15f, centro.y - r)
        close()
    }
    drawPath(chispa, color = color)
}

@Composable
fun InsigniaCierreDeAplausos(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val radio = size.minDimension * 0.28f
    for (i in 0 until 6) {
        val angulo = (2.0 * Math.PI * i / 6).toFloat()
        val inicio = centro + Offset(radio * 0.4f * cos(angulo), radio * 0.4f * sin(angulo))
        val fin = centro + Offset(radio * cos(angulo), radio * sin(angulo))
        drawLine(color = color, start = inicio, end = fin, strokeWidth = radio * 0.14f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

@Composable
fun InsigniaSinCortes(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.24f
    val onda = Path().apply {
        moveTo(centro.x - r, centro.y)
        cubicTo(centro.x - r * 0.5f, centro.y - r, centro.x - r * 0.2f, centro.y + r, centro.x, centro.y)
        cubicTo(centro.x + r * 0.2f, centro.y - r, centro.x + r * 0.5f, centro.y + r, centro.x + r, centro.y)
    }
    drawPath(onda, color = color, style = Stroke(width = r * 0.22f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}

@Composable
fun InsigniaBalconDominado(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.26f
    drawLine(color = color, start = centro + Offset(-r, -r * 0.3f), end = centro + Offset(r, -r * 0.3f), strokeWidth = r * 0.16f)
    for (i in -2..2) {
        drawLine(
            color = color,
            start = centro + Offset(i * r * 0.4f, -r * 0.3f),
            end = centro + Offset(i * r * 0.4f, r * 0.6f),
            strokeWidth = r * 0.1f
        )
    }
}

@Composable
fun InsigniaVozDelKiosco(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.26f
    val techo = Path().apply {
        moveTo(centro.x - r, centro.y)
        lineTo(centro.x, centro.y - r)
        lineTo(centro.x + r, centro.y)
        close()
    }
    drawPath(techo, color = color)
    drawRoundRect(
        color = color,
        topLeft = centro + Offset(-r * 0.6f, 0f),
        size = androidx.compose.ui.geometry.Size(r * 1.2f, r * 0.7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.08f, r * 0.08f)
    )
}

@Composable
fun InsigniaBuenVendedor(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.26f
    val canasta = Path().apply {
        moveTo(centro.x - r, centro.y - r * 0.2f)
        lineTo(centro.x + r, centro.y - r * 0.2f)
        lineTo(centro.x + r * 0.7f, centro.y + r * 0.7f)
        lineTo(centro.x - r * 0.7f, centro.y + r * 0.7f)
        close()
    }
    drawPath(canasta, color = color)
    drawArc(
        color = color,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = centro - Offset(r * 0.55f, r * 1.1f),
        size = androidx.compose.ui.geometry.Size(r * 1.1f, r * 1.1f),
        style = Stroke(width = r * 0.12f)
    )
}

@Composable
fun InsigniaSeOyoEnTodaLaPlaza(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val rBase = size.minDimension * 0.1f
    for (i in 1..3) {
        drawCircle(color = color.copy(alpha = 1f - i * 0.22f), radius = rBase * i, center = centro, style = Stroke(width = rBase * 0.22f))
    }
    drawCircle(color = color, radius = rBase * 0.5f, center = centro)
}

@Composable
fun InsigniaGratitudSincera(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.24f
    val corazon = Path().apply {
        moveTo(centro.x, centro.y + r * 0.8f)
        cubicTo(centro.x - r * 1.3f, centro.y - r * 0.3f, centro.x - r * 0.5f, centro.y - r * 1.2f, centro.x, centro.y - r * 0.4f)
        cubicTo(centro.x + r * 0.5f, centro.y - r * 1.2f, centro.x + r * 1.3f, centro.y - r * 0.3f, centro.x, centro.y + r * 0.8f)
        close()
    }
    drawPath(corazon, color = color)
}

@Composable
fun InsigniaBuenConsuelo(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.26f
    val gota = Path().apply {
        moveTo(centro.x, centro.y - r)
        cubicTo(centro.x + r * 0.9f, centro.y + r * 0.1f, centro.x + r * 0.55f, centro.y + r, centro.x, centro.y + r)
        cubicTo(centro.x - r * 0.55f, centro.y + r, centro.x - r * 0.9f, centro.y + r * 0.1f, centro.x, centro.y - r)
        close()
    }
    drawPath(gota, color = color)
}

@Composable
fun InsigniaLaTarimaEsTuya(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.26f
    drawRoundRect(
        color = color,
        topLeft = centro + Offset(-r, r * 0.15f),
        size = androidx.compose.ui.geometry.Size(r * 2f, r * 0.5f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.08f, r * 0.08f)
    )
    val estrella = trazarEstrella(centro - Offset(0f, r * 0.35f), r * 0.55f, r * 0.25f)
    drawPath(estrella, color = color)
}

@Composable
fun InsigniaCompaneroDeChirri(ganada: Boolean, modifier: Modifier = Modifier) = MarcoDeInsignia(ganada, modifier) { color ->
    val centro = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension * 0.2f
    drawOval(
        color = color,
        topLeft = centro - Offset(r * 0.75f, r * 0.55f),
        size = androidx.compose.ui.geometry.Size(r * 1.5f, r * 1.1f)
    )
    val antena = Path().apply {
        moveTo(centro.x - r * 0.3f, centro.y - r * 0.5f)
        quadraticTo(centro.x - r * 0.9f, centro.y - r * 1.3f, centro.x - r * 0.6f, centro.y - r * 1.7f)
    }
    val antena2 = Path().apply {
        moveTo(centro.x + r * 0.3f, centro.y - r * 0.5f)
        quadraticTo(centro.x + r * 0.9f, centro.y - r * 1.3f, centro.x + r * 0.6f, centro.y - r * 1.7f)
    }
    drawPath(antena, color = color, style = Stroke(width = r * 0.12f))
    drawPath(antena2, color = color, style = Stroke(width = r * 0.12f))
}
