package pe.appmobile.laplaza.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/**
 * Un tablon de puesto de mercado / placa de piedra: un rectangulo con un par de
 * esquinas cortadas de forma desigual y un leve vaiven en los bordes horizontales, para
 * que nunca se lea como un RoundedCornerShape perfecto -- seccion 3.1 del maestro.
 */
private val FormaPanelDePlaza: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val muescaChica = minOf(w, h) * 0.05f
    val muescaGrande = minOf(w, h) * 0.09f
    val vaiven = h * 0.02f

    moveTo(muescaGrande, 0f)
    lineTo(w * 0.55f, 0f)
    quadraticTo(w * 0.7f, vaiven, w - muescaChica, 0f)
    lineTo(w, muescaChica)
    lineTo(w, h - muescaGrande)
    lineTo(w - muescaGrande, h)
    lineTo(muescaChica * 1.5f, h)
    quadraticTo(w * 0.3f, h - vaiven, 0f, h - muescaChica)
    lineTo(0f, muescaGrande)
    close()
}

/**
 * Reemplaza a Card para cualquier superficie de contenido -un bloque de discurso, un
 * titular del Cuaderno de Pregones, etc- con una forma real de tabla/placa en vez de
 * esquinas redondeadas genericas. La sombra de contacto se dibuja sobre la misma forma
 * irregular, no sobre un rectangulo invisible detras.
 */
@Composable
fun PanelDePlaza(
    modifier: Modifier = Modifier,
    color: Color = BlancoRosado,
    colorBorde: Color = IndigoProfundo,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = FormaPanelDePlaza, clip = false)
            .clip(FormaPanelDePlaza)
            .background(color)
            .border(width = 2.dp, color = colorBorde, shape = FormaPanelDePlaza)
            .padding(contentPadding),
        content = content
    )
}
