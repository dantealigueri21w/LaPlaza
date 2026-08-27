package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.local.entity.PregonEntity
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.Chirri
import pe.appmobile.laplaza.ui.components.EstadoChirri
import pe.appmobile.laplaza.ui.components.PanelDePlaza
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo
import pe.appmobile.laplaza.ui.theme.VioletaAtardecer

private val FormateadorFechaPregon = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-PE"))

/**
 * El Cuaderno de Pregones real: cada [PregonEntity] es un titular generado por
 * [pe.appmobile.laplaza.domain.engine.MotorPregon] a partir de una declamacion real y
 * guardado en Room -- nunca una galeria de relleno. [pregones] ya llega ordenado del mas
 * reciente al mas antiguo (ver PregonDao.obtenerTodos). No mas de 21 filas posibles (una
 * por tema, seccion "Datos semilla" de la ficha): un `Column` con scroll manual alcanza
 * sin necesitar `LazyColumn` (seccion 7.1 del maestro).
 */
@Composable
fun CuadernoDePregonesScreen(
    pregones: List<PregonEntity>,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.cuaderno_titulo),
            style = MaterialTheme.typography.headlineSmall,
            color = IndigoProfundo,
            textAlign = TextAlign.Center
        )

        if (pregones.isEmpty()) {
            CuadernoVacio(modifier = Modifier.weight(1f, fill = false))
        } else {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                pregones.forEach { pregon -> PaginaDePregon(pregon) }
            }
        }

        BotonDePlaza(label = stringResource(R.string.accion_volver), onClick = onVolver)
    }
}

@Composable
private fun CuadernoVacio(modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.cuaderno_vacio_descripcion)
    Column(
        modifier = modifier.semantics { contentDescription = descripcion },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Chirri(estado = EstadoChirri.ANIMANDO, modifier = Modifier.size(140.dp))
        PanelDePlaza {
            Text(
                text = stringResource(R.string.cuaderno_vacio_mensaje),
                style = MaterialTheme.typography.bodyMedium,
                color = IndigoProfundo,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PaginaDePregon(pregon: PregonEntity) {
    val fecha = remember(pregon.fechaEpochMs) {
        Instant.ofEpochMilli(pregon.fechaEpochMs)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(FormateadorFechaPregon)
    }
    val descripcion = stringResource(R.string.cuaderno_pagina_descripcion, pregon.titular, fecha)

    PanelDePlaza(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = descripcion }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            InsigniaEstrellaPregon(variableDestacada = pregon.variableDestacada, modifier = Modifier.size(28.dp))
            Text(
                text = pregon.titular,
                style = MaterialTheme.typography.bodyLarge,
                color = IndigoProfundo
            )
            Text(
                text = fecha,
                style = MaterialTheme.typography.labelMedium,
                color = VioletaAtardecer
            )
        }
    }
}

/**
 * Una pequena estrella de pagina -no una insignia formal del Cuaderno de Insignias del
 * perfil, solo un adorno de la propia pagina- para que cada entrada del cuaderno no sea
 * solo texto plano: nucleo con degradado radial, contorno curvo con `cubicTo` y una
 * sombra de contacto (seccion 4.0 del maestro).
 */
@Composable
private fun InsigniaEstrellaPregon(variableDestacada: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) { dibujarEstrellaPregon() }
}

private fun DrawScope.dibujarEstrellaPregon() {
    val centro = Offset(size.width / 2f, size.height / 2f)
    val radioExterior = size.minDimension * 0.48f
    val radioInterior = radioExterior * 0.46f

    drawCircle(
        color = IndigoProfundo.copy(alpha = 0.16f),
        radius = radioExterior * 1.05f,
        center = centro + Offset(0f, radioExterior * 0.12f)
    )

    val puntas = 5
    val camino = androidx.compose.ui.graphics.Path().apply {
        for (i in 0 until puntas * 2) {
            val radio = if (i % 2 == 0) radioExterior else radioInterior
            val angulo = (Math.PI / puntas * i - Math.PI / 2).toFloat()
            val punto = Offset(centro.x + radio * kotlin.math.cos(angulo), centro.y + radio * kotlin.math.sin(angulo))
            if (i == 0) moveTo(punto.x, punto.y) else {
                val anguloAnterior = (Math.PI / puntas * (i - 1) - Math.PI / 2).toFloat()
                val radioAnterior = if ((i - 1) % 2 == 0) radioExterior else radioInterior
                val anterior = Offset(
                    centro.x + radioAnterior * kotlin.math.cos(anguloAnterior),
                    centro.y + radioAnterior * kotlin.math.sin(anguloAnterior)
                )
                val control = Offset((anterior.x + punto.x) / 2f, (anterior.y + punto.y) / 2f)
                quadraticTo(anterior.x, anterior.y, control.x, control.y)
            }
        }
        close()
    }
    drawPath(
        camino,
        brush = Brush.radialGradient(
            colors = listOf(AmbarFarol, AmbarFarol.copy(alpha = 0.75f)),
            center = centro,
            radius = radioExterior * 1.3f
        )
    )
    drawPath(camino, color = IndigoProfundo, style = Stroke(width = size.minDimension * 0.02f))
}
