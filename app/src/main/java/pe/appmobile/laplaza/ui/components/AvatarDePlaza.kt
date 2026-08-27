package pe.appmobile.laplaza.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
 * Las 8 siluetas de avatar (arte real pendiente, ver data/seed/Avatares.kt: solo existe
 * el rango 1..8). Cada [DefinicionAvatar] combina una silueta distinta con colores de la
 * paleta -- nunca solo un cambio de color entre avatares, para que la diferencia se note
 * incluso sin distinguir tonos parecidos.
 */
private enum class FormaAvatar { CIRCULO, CUADRADO, TRIANGULO, ESTRELLA, HEXAGONO, DIAMANTE, OVALO, CRUZ }

private data class DefinicionAvatar(val forma: FormaAvatar, val colorFondo: Color, val colorDetalle: Color)

private fun definicionDe(avatarId: Int): DefinicionAvatar = when (avatarId) {
    1 -> DefinicionAvatar(FormaAvatar.CIRCULO, RosaBerenjena, BlancoRosado)
    2 -> DefinicionAvatar(FormaAvatar.CUADRADO, RosaBerenjena, AmbarFarol)
    3 -> DefinicionAvatar(FormaAvatar.TRIANGULO, VioletaAtardecer, BlancoRosado)
    4 -> DefinicionAvatar(FormaAvatar.ESTRELLA, VioletaAtardecer, AmbarFarol)
    5 -> DefinicionAvatar(FormaAvatar.HEXAGONO, AmbarFarol, IndigoProfundo)
    6 -> DefinicionAvatar(FormaAvatar.DIAMANTE, AmbarFarol, RosaBerenjena)
    7 -> DefinicionAvatar(FormaAvatar.OVALO, BlancoRosado, RosaBerenjena)
    else -> DefinicionAvatar(FormaAvatar.CRUZ, BlancoRosado, VioletaAtardecer)
}

/**
 * Dibuja el avatar [avatarId] (1..8, ver [pe.appmobile.laplaza.data.seed.Avatares]) como
 * una silueta simple en Canvas -- no hay arte real todavia para esta app. El llamador
 * controla el tamano con [modifier]; si no especifica ninguno, usa un minimo razonable.
 * [descripcion] es opcional porque a veces el avatar se dibuja dentro de un contenedor
 * que ya trae su propia descripcion de accesibilidad (por ejemplo un item seleccionable
 * del selector de avatares).
 */
@Composable
fun AvatarDePlaza(avatarId: Int, modifier: Modifier = Modifier, descripcion: String? = null) {
    val definicion = remember(avatarId) { definicionDe(avatarId) }
    val modificadorConDescripcion = if (descripcion != null) {
        modifier.semantics { contentDescription = descripcion }
    } else {
        modifier
    }

    Canvas(
        modifier = modificadorConDescripcion.defaultMinSize(minWidth = 96.dp, minHeight = 96.dp)
    ) {
        dibujarAvatar(definicion)
    }
}

private fun DrawScope.dibujarAvatar(definicion: DefinicionAvatar) {
    val centro = Offset(size.width / 2f, size.height / 2f)
    val radio = size.minDimension * 0.4f
    val grosor = size.minDimension * 0.035f

    when (definicion.forma) {
        FormaAvatar.CIRCULO -> {
            drawCircle(color = definicion.colorFondo, radius = radio, center = centro)
            drawCircle(color = IndigoProfundo, radius = radio, center = centro, style = Stroke(width = grosor))
        }

        FormaAvatar.CUADRADO -> {
            val lado = radio * 1.6f
            val esquina = Offset(centro.x - lado / 2f, centro.y - lado / 2f)
            val tamano = Size(lado, lado)
            val redondeo = CornerRadius(lado * 0.18f, lado * 0.18f)
            drawRoundRect(color = definicion.colorFondo, topLeft = esquina, size = tamano, cornerRadius = redondeo)
            drawRoundRect(
                color = IndigoProfundo,
                topLeft = esquina,
                size = tamano,
                cornerRadius = redondeo,
                style = Stroke(width = grosor)
            )
        }

        FormaAvatar.OVALO -> {
            val esquina = Offset(centro.x - radio, centro.y - radio * 0.72f)
            val tamano = Size(radio * 2f, radio * 1.44f)
            drawOval(color = definicion.colorFondo, topLeft = esquina, size = tamano)
            drawOval(color = IndigoProfundo, topLeft = esquina, size = tamano, style = Stroke(width = grosor))
        }

        else -> {
            val trazo = crearPathDeForma(definicion.forma, centro, radio)
            drawPath(trazo, color = definicion.colorFondo)
            drawPath(trazo, color = IndigoProfundo, style = Stroke(width = grosor))
        }
    }

    dibujarOjos(centro, radio, definicion.colorDetalle, grosor)
}

/** Solo para las siluetas armadas a mano con Path (las que no tienen una funcion nativa
 * de DrawScope como drawCircle/drawRoundRect/drawOval). */
private fun crearPathDeForma(forma: FormaAvatar, centro: Offset, radio: Float): Path = when (forma) {
    FormaAvatar.TRIANGULO -> Path().apply {
        moveTo(centro.x, centro.y - radio)
        lineTo(centro.x + radio * 0.92f, centro.y + radio * 0.75f)
        lineTo(centro.x - radio * 0.92f, centro.y + radio * 0.75f)
        close()
    }

    FormaAvatar.DIAMANTE -> Path().apply {
        moveTo(centro.x, centro.y - radio)
        lineTo(centro.x + radio, centro.y)
        lineTo(centro.x, centro.y + radio)
        lineTo(centro.x - radio, centro.y)
        close()
    }

    FormaAvatar.HEXAGONO -> Path().apply {
        for (i in 0 until 6) {
            val angulo = (PI / 3.0 * i - PI / 2.0).toFloat()
            val punto = Offset(centro.x + radio * cos(angulo), centro.y + radio * sin(angulo))
            if (i == 0) moveTo(punto.x, punto.y) else lineTo(punto.x, punto.y)
        }
        close()
    }

    FormaAvatar.ESTRELLA -> Path().apply {
        val puntas = 5
        for (i in 0 until puntas * 2) {
            val radioPunto = if (i % 2 == 0) radio else radio * 0.44f
            val angulo = (PI / puntas * i - PI / 2.0).toFloat()
            val punto = Offset(centro.x + radioPunto * cos(angulo), centro.y + radioPunto * sin(angulo))
            if (i == 0) moveTo(punto.x, punto.y) else lineTo(punto.x, punto.y)
        }
        close()
    }

    FormaAvatar.CRUZ -> Path().apply {
        val brazo = radio * 0.42f
        moveTo(centro.x - brazo, centro.y - radio)
        lineTo(centro.x + brazo, centro.y - radio)
        lineTo(centro.x + brazo, centro.y - brazo)
        lineTo(centro.x + radio, centro.y - brazo)
        lineTo(centro.x + radio, centro.y + brazo)
        lineTo(centro.x + brazo, centro.y + brazo)
        lineTo(centro.x + brazo, centro.y + radio)
        lineTo(centro.x - brazo, centro.y + radio)
        lineTo(centro.x - brazo, centro.y + brazo)
        lineTo(centro.x - radio, centro.y + brazo)
        lineTo(centro.x - radio, centro.y - brazo)
        lineTo(centro.x - brazo, centro.y - brazo)
        close()
    }

    // CIRCULO, CUADRADO y OVALO se dibujan con funciones nativas de DrawScope, no con Path.
    FormaAvatar.CIRCULO, FormaAvatar.CUADRADO, FormaAvatar.OVALO -> Path()
}

/** Un par de "ojos" simples le da caracter de personaje a una silueta geometrica -- para
 * que el selector de avatares no se sienta como una simple paleta de colores. */
private fun DrawScope.dibujarOjos(centro: Offset, radio: Float, colorDetalle: Color, grosor: Float) {
    val separacion = radio * 0.38f
    val y = centro.y - radio * 0.05f
    val radioOjo = radio * 0.13f
    listOf(centro.x - separacion, centro.x + separacion).forEach { x ->
        drawCircle(color = colorDetalle, radius = radioOjo, center = Offset(x, y))
        drawCircle(color = IndigoProfundo, radius = radioOjo, center = Offset(x, y), style = Stroke(width = grosor * 0.5f))
    }
}
