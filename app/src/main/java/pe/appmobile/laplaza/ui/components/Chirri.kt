package pe.appmobile.laplaza.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo
import pe.appmobile.laplaza.ui.theme.RosaBerenjena
import pe.appmobile.laplaza.ui.theme.VioletaAtardecer

/**
 * Los cinco momentos narrativos de Chirri (ficha 24-LA-PLAZA.md): reposo, el saludo al
 * entrar a un rincon, el animo justo antes de hablar, la celebracion tras un buen
 * resultado, y el "preocupado" que refleja su propio miedo (la voz que se le corto en
 * su historia). Los nombres son neutros a proposito -no "vozCortada" ni "cantoValiente"-
 * para que el dia que haya arte real (02-GUIA-IMAGENES.md, pose 1 con 8 poses) cambiar
 * a Image() sea sustituir la implementacion de [Chirri], no renombrar nada que ya use
 * el resto de la app: PREOCUPADO es donde iria la pose "voz cortada", CELEBRANDO donde
 * iria "canto valiente".
 */
enum class EstadoChirri { NEUTRAL, SALUDANDO, ANIMANDO, CELEBRANDO, PREOCUPADO }

/** A que color de la paleta de la ficha (ver Color.kt) se refiere un parametro de Chirri. */
enum class ColorChirri { ROSA_BERENJENA, VIOLETA_ATARDECER, INDIGO_PROFUNDO, AMBAR_FAROL, BLANCO_ROSADO }

/**
 * Descripcion pura -sin Compose ni Android- de como se ve y que dice Chirri en un
 * estado dado. Separada del composable [Chirri] a proposito: el mapeo estado ->
 * geometria/color se puede (y debe) probar con JUnit puro, sin Robolectric.
 *
 * Los angulos de antena son grados sobre la horizontal (positivo = hacia arriba,
 * negativo = caida). La apertura de ojos va de 0f (bien cerrados) a 1f (bien abiertos).
 * La curva de boca va de -1f (preocupado) a 1f (sonrisa grande).
 */
data class ParametrosChirri(
    val aperturaOjos: Float,
    val anguloAntenaIzquierda: Float,
    val anguloAntenaDerecha: Float,
    val curvaBoca: Float,
    val mostrarAcento: Boolean,
    val colorAcento: ColorChirri,
    val descripcion: String
)

/**
 * Mapeo puro estado -> parametros de dibujo. Ver [ParametrosChirri]. El acento de
 * CELEBRANDO usa AMBAR_FAROL a proposito: es el mismo color de "un farol mas
 * encendido" que la ficha usa para cada logro real del nino.
 */
fun parametrosDeEstado(estado: EstadoChirri): ParametrosChirri = when (estado) {
    EstadoChirri.NEUTRAL -> ParametrosChirri(
        aperturaOjos = 0.7f,
        anguloAntenaIzquierda = 35f,
        anguloAntenaDerecha = 35f,
        curvaBoca = 0.15f,
        mostrarAcento = false,
        colorAcento = ColorChirri.AMBAR_FAROL,
        descripcion = "Chirri esta tranquilo, esperando bajo su piedra."
    )

    EstadoChirri.SALUDANDO -> ParametrosChirri(
        aperturaOjos = 0.85f,
        anguloAntenaIzquierda = 75f,
        anguloAntenaDerecha = 40f,
        curvaBoca = 0.55f,
        mostrarAcento = true,
        colorAcento = ColorChirri.ROSA_BERENJENA,
        descripcion = "Chirri te saluda agitando una antena."
    )

    EstadoChirri.ANIMANDO -> ParametrosChirri(
        aperturaOjos = 0.8f,
        anguloAntenaIzquierda = 55f,
        anguloAntenaDerecha = 55f,
        curvaBoca = 0.4f,
        mostrarAcento = false,
        colorAcento = ColorChirri.AMBAR_FAROL,
        descripcion = "Chirri te anima con las antenas erguidas, listo para practicar contigo."
    )

    EstadoChirri.CELEBRANDO -> ParametrosChirri(
        aperturaOjos = 0.55f,
        anguloAntenaIzquierda = 80f,
        anguloAntenaDerecha = 80f,
        curvaBoca = 0.95f,
        mostrarAcento = true,
        colorAcento = ColorChirri.AMBAR_FAROL,
        descripcion = "Chirri celebra dando un salto, con una chispa de alegria."
    )

    EstadoChirri.PREOCUPADO -> ParametrosChirri(
        aperturaOjos = 1f,
        anguloAntenaIzquierda = -20f,
        anguloAntenaDerecha = -20f,
        curvaBoca = -0.5f,
        mostrarAcento = false,
        colorAcento = ColorChirri.AMBAR_FAROL,
        descripcion = "Chirri esta preocupado: ojos bien abiertos y antenas caidas, " +
            "como cuando se le corto la voz."
    )
}

private fun colorReal(color: ColorChirri): Color = when (color) {
    ColorChirri.ROSA_BERENJENA -> RosaBerenjena
    ColorChirri.VIOLETA_ATARDECER -> VioletaAtardecer
    ColorChirri.INDIGO_PROFUNDO -> IndigoProfundo
    ColorChirri.AMBAR_FAROL -> AmbarFarol
    ColorChirri.BLANCO_ROSADO -> BlancoRosado
}

/**
 * Chirri, el grillo guia, dibujado a mano en Canvas -no hay arte real todavia para esta
 * app (02-GUIA-IMAGENES.md aun no se ejecuto). Aparece al entrar a un rincon y justo
 * despues de un discurso; nunca durante la declamacion, que es protagonismo de la
 * plaza, no de Chirri.
 *
 * El llamador controla el tamano con [modifier] (por ejemplo `Modifier.size(160.dp)`);
 * si no especifica ninguno, usa un minimo razonable para no colapsar a tamano cero.
 */
@Composable
fun Chirri(estado: EstadoChirri, modifier: Modifier = Modifier) {
    val parametros = parametrosDeEstado(estado)
    val escala by animateFloatAsState(
        targetValue = if (estado == EstadoChirri.CELEBRANDO) 1.08f else 1f,
        label = "escalaChirri"
    )

    Canvas(
        modifier = modifier
            .defaultMinSize(minWidth = 160.dp, minHeight = 160.dp)
            .scale(escala)
            .semantics { contentDescription = parametros.descripcion }
    ) {
        dibujarChirri(parametros)
    }
}

private fun DrawScope.dibujarChirri(p: ParametrosChirri) {
    val radioCuerpo = size.minDimension * 0.26f
    val radioCabeza = size.minDimension * 0.17f
    val centro = Offset(size.width / 2f, size.height / 2f)
    val centroCuerpo = Offset(centro.x, centro.y + radioCuerpo * 0.55f)
    val centroCabeza = Offset(centro.x, centroCuerpo.y - radioCuerpo - radioCabeza * 0.55f)
    val contorno = IndigoProfundo
    val grosor = size.minDimension * 0.012f

    // sombra de contacto: Chirri "flota" un poco sobre el suelo de la piedra, seccion
    // 4.0 del maestro exige profundidad en cualquier elemento asi.
    drawOval(
        color = IndigoProfundo.copy(alpha = 0.18f),
        topLeft = Offset(centroCuerpo.x - radioCuerpo * 1.05f, centroCuerpo.y + radioCuerpo * 0.75f),
        size = Size(radioCuerpo * 2.1f, radioCuerpo * 0.5f)
    )

    dibujarPatas(centroCuerpo, radioCuerpo, contorno, grosor)
    dibujarAntenas(centroCabeza, radioCabeza, p.anguloAntenaIzquierda, p.anguloAntenaDerecha, contorno, grosor)

    // cuerpo (abdomen): degradado radial en vez de color solido para dar volumen real,
    // mas el parche claro de vientre encima.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(VioletaAtardecer.copy(alpha = 0.75f), VioletaAtardecer),
            center = centroCuerpo - Offset(radioCuerpo * 0.3f, radioCuerpo * 0.3f),
            radius = radioCuerpo * 1.6f
        ),
        radius = radioCuerpo,
        center = centroCuerpo
    )
    drawCircle(color = contorno, radius = radioCuerpo, center = centroCuerpo, style = Stroke(width = grosor))
    drawOval(
        color = BlancoRosado,
        topLeft = Offset(centroCuerpo.x - radioCuerpo * 0.45f, centroCuerpo.y - radioCuerpo * 0.1f),
        size = Size(radioCuerpo * 0.9f, radioCuerpo * 0.85f)
    )

    // cabeza, encima del cuerpo y encima de la base de las antenas -- mismo degradado
    // que el cuerpo, mismo bloque de estilo (seccion 4.0).
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(VioletaAtardecer.copy(alpha = 0.75f), VioletaAtardecer),
            center = centroCabeza - Offset(radioCabeza * 0.3f, radioCabeza * 0.3f),
            radius = radioCabeza * 1.6f
        ),
        radius = radioCabeza,
        center = centroCabeza
    )
    drawCircle(color = contorno, radius = radioCabeza, center = centroCabeza, style = Stroke(width = grosor))

    dibujarOjos(centroCabeza, radioCabeza, p.aperturaOjos)
    dibujarBoca(centroCabeza, radioCabeza, p.curvaBoca, contorno, grosor)

    if (p.mostrarAcento) {
        dibujarChispa(centroCabeza, radioCabeza, colorReal(p.colorAcento))
    }
}

private fun DrawScope.dibujarPatas(centroCuerpo: Offset, radioCuerpo: Float, color: Color, grosor: Float) {
    dibujarUnaPata(centroCuerpo, radioCuerpo, haciaLaIzquierda = true, color, grosor)
    dibujarUnaPata(centroCuerpo, radioCuerpo, haciaLaIzquierda = false, color, grosor)
}

/** Una pata trasera doblada en angulo, la silueta tipica de un grillo saltador. */
private fun DrawScope.dibujarUnaPata(
    centroCuerpo: Offset,
    radioCuerpo: Float,
    haciaLaIzquierda: Boolean,
    color: Color,
    grosor: Float
) {
    val signo = if (haciaLaIzquierda) -1f else 1f
    val cadera = Offset(centroCuerpo.x + signo * radioCuerpo * 0.75f, centroCuerpo.y + radioCuerpo * 0.35f)
    val rodilla = Offset(cadera.x + signo * radioCuerpo * 0.55f, cadera.y + radioCuerpo * 0.35f)
    val pie = Offset(rodilla.x + signo * radioCuerpo * 0.15f, rodilla.y + radioCuerpo * 0.75f)
    val trazoPata = Path().apply {
        moveTo(cadera.x, cadera.y)
        lineTo(rodilla.x, rodilla.y)
        lineTo(pie.x, pie.y)
    }
    drawPath(
        trazoPata,
        color = color,
        style = Stroke(width = grosor * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.dibujarAntenas(
    centroCabeza: Offset,
    radioCabeza: Float,
    anguloIzquierda: Float,
    anguloDerecha: Float,
    color: Color,
    grosor: Float
) {
    dibujarUnaAntena(centroCabeza, radioCabeza, anguloIzquierda, haciaLaIzquierda = true, color, grosor)
    dibujarUnaAntena(centroCabeza, radioCabeza, anguloDerecha, haciaLaIzquierda = false, color, grosor)
}

private fun DrawScope.dibujarUnaAntena(
    centroCabeza: Offset,
    radioCabeza: Float,
    anguloGrados: Float,
    haciaLaIzquierda: Boolean,
    color: Color,
    grosor: Float
) {
    val signo = if (haciaLaIzquierda) -1f else 1f
    val origen = Offset(centroCabeza.x + signo * radioCabeza * 0.5f, centroCabeza.y - radioCabeza * 0.7f)
    val largo = radioCabeza * 2.3f
    val radianes = (anguloGrados * PI / 180f).toFloat()
    val punta = Offset(
        x = origen.x + signo * largo * cos(radianes),
        y = origen.y - largo * sin(radianes)
    )
    val puntoControl = Offset(
        x = origen.x + signo * largo * 0.55f,
        y = origen.y - largo * 0.15f
    )
    val trazoAntena = Path().apply {
        moveTo(origen.x, origen.y)
        quadraticTo(puntoControl.x, puntoControl.y, punta.x, punta.y)
    }
    drawPath(trazoAntena, color = color, style = Stroke(width = grosor, cap = StrokeCap.Round))
    drawCircle(color = color, radius = grosor * 1.5f, center = punta)
}

/**
 * Ojos grandes y redondos -mas legibles para un nino, no "bebe"- salvo cuando la
 * apertura baja de 0.65f: ahi se dibuja un arco feliz hacia arriba en vez de un
 * circulo, el gesto tipico de un ojo entrecerrado de alegria (CELEBRANDO).
 */
private fun DrawScope.dibujarOjos(centroCabeza: Offset, radioCabeza: Float, apertura: Float) {
    val separacion = radioCabeza * 0.55f
    val y = centroCabeza.y - radioCabeza * 0.1f
    dibujarUnOjo(Offset(centroCabeza.x - separacion, y), radioCabeza * 0.5f, apertura)
    dibujarUnOjo(Offset(centroCabeza.x + separacion, y), radioCabeza * 0.5f, apertura)
}

private fun DrawScope.dibujarUnOjo(centro: Offset, radio: Float, apertura: Float) {
    if (apertura < 0.65f) {
        val trazo = Path().apply {
            moveTo(centro.x - radio, centro.y)
            quadraticTo(centro.x, centro.y - radio * 1.3f, centro.x + radio, centro.y)
        }
        drawPath(trazo, color = IndigoProfundo, style = Stroke(width = radio * 0.35f, cap = StrokeCap.Round))
    } else {
        val radioEscalado = radio * (0.75f + 0.25f * apertura)
        drawCircle(color = BlancoRosado, radius = radioEscalado, center = centro)
        drawCircle(
            color = IndigoProfundo,
            radius = radioEscalado,
            center = centro,
            style = Stroke(width = radioEscalado * 0.12f)
        )
        drawCircle(color = IndigoProfundo, radius = radioEscalado * 0.5f, center = centro)
    }
}

/** curva > 0 sonrie (forma de "u"), curva < 0 preocupa (forma de arco hacia arriba). */
private fun DrawScope.dibujarBoca(centroCabeza: Offset, radioCabeza: Float, curva: Float, color: Color, grosor: Float) {
    val anchoBoca = radioCabeza * 0.7f
    val y = centroCabeza.y + radioCabeza * 0.55f
    val inicio = Offset(centroCabeza.x - anchoBoca / 2f, y)
    val fin = Offset(centroCabeza.x + anchoBoca / 2f, y)
    val control = Offset(centroCabeza.x, y + curva * radioCabeza * 0.6f)
    val trazo = Path().apply {
        moveTo(inicio.x, inicio.y)
        quadraticTo(control.x, control.y, fin.x, fin.y)
    }
    drawPath(trazo, color = color, style = Stroke(width = grosor * 1.2f, cap = StrokeCap.Round))
}

/** Una chispa de cuatro puntas: el acento de saludo o de celebracion. */
private fun DrawScope.dibujarChispa(centroCabeza: Offset, radioCabeza: Float, color: Color) {
    val centro = Offset(centroCabeza.x + radioCabeza * 1.6f, centroCabeza.y - radioCabeza * 1.3f)
    val radio = radioCabeza * 0.45f
    val trazo = Path().apply {
        moveTo(centro.x, centro.y - radio)
        quadraticTo(centro.x, centro.y, centro.x + radio, centro.y)
        quadraticTo(centro.x, centro.y, centro.x, centro.y + radio)
        quadraticTo(centro.x, centro.y, centro.x - radio, centro.y)
        quadraticTo(centro.x, centro.y, centro.x, centro.y - radio)
        close()
    }
    drawPath(trazo, color = color)
}
