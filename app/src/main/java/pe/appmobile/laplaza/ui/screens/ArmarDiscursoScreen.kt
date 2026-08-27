package pe.appmobile.laplaza.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.local.entity.BloqueContenidoEntity
import pe.appmobile.laplaza.domain.engine.MotorDiscurso
import pe.appmobile.laplaza.domain.model.BloqueDiscurso
import pe.appmobile.laplaza.domain.model.DiscursoArmado
import pe.appmobile.laplaza.domain.model.Franja
import pe.appmobile.laplaza.domain.model.ResultadoValidacion
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.PanelDePlaza
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/** Cuanto hay que arrastrar un bloque hacia arriba, en dp, para que cuente como
 * "soltado en su ranura" -- ver el comentario grande de [ChipBloque]. */
private const val UMBRAL_ARRASTRE_DP = 72

/** El orden estructural gancho -> cuerpo -> cierre nunca lo elige el nino (ver
 * MotorDiscurso.ordenEsperado): la "eleccion" es solo cual de las opciones de cada
 * franja usar, nunca el orden de las 3 franjas entre si. */
private val ordenFranjas = listOf(Franja.GANCHO, Franja.CUERPO, Franja.CIERRE)

/**
 * El tablero de armado del discurso (ficha 24-LA-PLAZA.md, "el nino arma su discurso
 * arrastrando bloques"): el nino ARRASTRA un bloque de cada una de las 3 franjas
 * (gancho/cuerpo/cierre, siempre en ese orden) hasta la ranura de esa franja, entre las
 * opciones reales de [bloques] (contenido estatico, ya escrito -- nunca generado aqui).
 * Ver [ChipBloque]: un `.clickable` a secas -sin gesto real- es opcion multiple
 * disfrazada, justo lo que prohibe la seccion 1 del maestro; tocar sigue funcionando
 * como equivalente accesible (un lector de pantalla no puede arrastrar), pero el gesto
 * principal e invitado por la ranura vacia es arrastrar.
 *
 * La accion "Declamar" solo aparece cuando hay una seleccion en las 3 franjas Y
 * [MotorDiscurso.validar] sobre esa seleccion real da [ResultadoValidacion.Valido] --
 * nunca se asume valido solo por tener 3 selecciones (ver [construirDiscursoSiValido]):
 * es la misma regla de dominio que ya prueba MotorDiscursoTest, no una copia en la UI.
 */
@Composable
fun ArmarDiscursoScreen(
    tituloTema: String,
    bloques: List<BloqueContenidoEntity>,
    onDeclamar: (DiscursoArmado) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bloquesPorFranja = remember(bloques) { bloques.groupBy { it.franja } }

    var seleccionPorFranja by remember(bloques) { mutableStateOf<Map<Franja, Long?>>(emptyMap()) }

    val discursoValidado = remember(bloques, seleccionPorFranja) {
        construirDiscursoSiValido(bloques, seleccionPorFranja)
    }

    val descripcionVolver = stringResource(R.string.accion_volver)
    val etiquetasFranja = mapOf(
        Franja.GANCHO to stringResource(R.string.armar_discurso_gancho),
        Franja.CUERPO to stringResource(R.string.armar_discurso_cuerpo),
        Franja.CIERRE to stringResource(R.string.armar_discurso_cierre)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = descripcionVolver,
                    tint = IndigoProfundo
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tituloTema,
                style = MaterialTheme.typography.titleLarge,
                color = IndigoProfundo
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ordenFranjas.forEach { franja ->
                SeccionFranja(
                    titulo = etiquetasFranja.getValue(franja),
                    bloques = bloquesPorFranja[franja.name].orEmpty(),
                    seleccionActual = seleccionPorFranja[franja],
                    onSeleccionar = { id ->
                        seleccionPorFranja = seleccionPorFranja + (franja to id)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (discursoValidado != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                BotonDePlaza(
                    label = stringResource(R.string.armar_discurso_declamar),
                    onClick = { onDeclamar(discursoValidado) }
                )
            }
        }
    }
}

@Composable
private fun SeccionFranja(
    titulo: String,
    bloques: List<BloqueContenidoEntity>,
    seleccionActual: Long?,
    onSeleccionar: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val bloqueSeleccionado = remember(bloques, seleccionActual) { bloques.find { it.id == seleccionActual } }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium, color = IndigoProfundo)
        Spacer(modifier = Modifier.height(8.dp))
        RanuraDeFranja(titulo = titulo, bloqueColocado = bloqueSeleccionado)
        Spacer(modifier = Modifier.height(10.dp))
        // FlowRow (no Row): el numero de opciones por franja es un dato real (semilla),
        // no una constante -- un Row plano se rompe apenas hay mas de las 3 que caben
        // hoy en una pantalla angosta (ver la nota de la tarea sobre este mismo bug ya
        // encontrado en una app hermana).
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            bloques.forEach { bloque ->
                ChipBloque(
                    bloque = bloque,
                    seleccionado = bloque.id == seleccionActual,
                    onColocar = { onSeleccionar(bloque.id) }
                )
            }
        }
    }
}

/** La ranura de la franja: vacia invita con un contorno punteado (seccion 5.5 del
 * maestro, "un borde punteado... nunca con una flecha ni un texto tipo arrastra aqui" --
 * el propio hueco ya comunica que algo va ahi); llena, muestra el texto real elegido con
 * el mismo check que antes marcaba el chip. */
@Composable
private fun RanuraDeFranja(titulo: String, bloqueColocado: BloqueContenidoEntity?, modifier: Modifier = Modifier) {
    if (bloqueColocado != null) {
        PanelDePlaza(
            color = AmbarFarol.copy(alpha = 0.18f),
            colorBorde = AmbarFarol,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = AmbarFarol,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = bloqueColocado.texto, color = IndigoProfundo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        val descripcionVacia = stringResource(R.string.armar_discurso_ranura_vacia, titulo)
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics { contentDescription = descripcionVacia }
        ) {
            drawRoundRect(
                color = IndigoProfundo.copy(alpha = 0.5f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                size = Size(size.width, size.height),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                )
            )
        }
    }
}

/**
 * Un bloque de discurso que se ARRASTRA hacia arriba, hacia la ranura de su franja
 * ([RanuraDeFranja], justo encima en la misma [SeccionFranja]): al soltar habiendo
 * cruzado [UMBRAL_ARRASTRE_DP], [onColocar] hace lo mismo que antes hacia el tap -no hay
 * deteccion de solape con la ranura por posicion real (mas simple: un umbral vertical
 * fijo alcanza porque la ranura vive siempre arriba del todo, en la misma columna, y es
 * el mismo umbral para las tres franjas)-. Tocar sin arrastrar SIGUE colocando el
 * bloque, a proposito: es el equivalente accesible para quien no puede completar un
 * gesto de arrastre (lector de pantalla, control por switch) -- ver el comentario grande
 * de [ArmarDiscursoScreen].
 *
 * `offsetArrastre` seguido al dedo en tiempo real es un `mutableStateOf` sincronico
 * (nunca una coroutine por evento): [Animatable] solo entra para el resorte de vuelta a
 * cero al soltar (seccion 5 del maestro, "Resorte al soltar"), nunca para el seguimiento
 * en vivo -eso evita que varias `scope.launch` por gesto se pisen entre si.
 */
@Composable
private fun ChipBloque(
    bloque: BloqueContenidoEntity,
    seleccionado: Boolean,
    onColocar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estadoTexto = if (seleccionado) {
        stringResource(R.string.armar_discurso_seleccionado)
    } else {
        stringResource(R.string.armar_discurso_no_seleccionado)
    }
    val descripcion = "${bloque.texto}, $estadoTexto"

    val densidad = LocalDensity.current
    val umbralPx = remember(densidad) { with(densidad) { UMBRAL_ARRASTRE_DP.dp.toPx() } }
    var offsetArrastre by remember { mutableStateOf(0f) }
    val offsetResorte = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val offsetVisible = if (offsetResorte.isRunning) offsetResorte.value else offsetArrastre

    fun soltar() {
        val colocado = offsetArrastre <= -umbralPx
        val partida = offsetArrastre
        offsetArrastre = 0f
        scope.launch {
            offsetResorte.snapTo(partida)
            offsetResorte.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        if (colocado) onColocar()
    }

    PanelDePlaza(
        colorBorde = if (seleccionado) AmbarFarol else IndigoProfundo,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier
            .widthIn(max = 280.dp)
            .offset { IntOffset(0, offsetVisible.roundToInt()) }
            .pointerInput(bloque.id) {
                detectDragGestures(
                    onDragStart = { offsetArrastre = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetArrastre += dragAmount.y
                    },
                    onDragEnd = { soltar() },
                    onDragCancel = { soltar() }
                )
            }
            .clickable(onClickLabel = bloque.texto, role = Role.Button, onClick = onColocar)
            .semantics { contentDescription = descripcion }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (seleccionado) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = AmbarFarol,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = bloque.texto, color = IndigoProfundo, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * null salvo que haya UNA seleccion real por franja Y esa seleccion, convertida a
 * [DiscursoArmado], pase [MotorDiscurso.validar] de verdad -- nunca se asume valido
 * solo por tener 3 ids no nulos (podria haber, por ejemplo, dos bloques de la misma
 * franja si algun dia esta funcion cambia de forma incorrecta; llamar al motor real es
 * lo que lo hace imposible en la practica, no un comentario).
 */
private fun construirDiscursoSiValido(
    bloques: List<BloqueContenidoEntity>,
    seleccionPorFranja: Map<Franja, Long?>
): DiscursoArmado? {
    val idsSeleccionados = ordenFranjas.map { seleccionPorFranja[it] ?: return null }
    val temaId = bloques.firstOrNull()?.temaId ?: return null

    val bloquesSeleccionados = idsSeleccionados.map { id ->
        bloques.find { it.id == id } ?: return null
    }

    val bloquesDiscurso = bloquesSeleccionados.map { entidad ->
        BloqueDiscurso(
            id = entidad.id,
            temaId = entidad.temaId,
            franja = Franja.valueOf(entidad.franja),
            texto = entidad.texto
        )
    }

    val discurso = DiscursoArmado(temaId = temaId, bloques = bloquesDiscurso)
    return if (MotorDiscurso.validar(discurso) == ResultadoValidacion.Valido) discurso else null
}
