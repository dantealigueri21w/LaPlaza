package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
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

/** El orden estructural gancho -> cuerpo -> cierre nunca lo elige el nino (ver
 * MotorDiscurso.ordenEsperado): la "eleccion" es solo cual de las opciones de cada
 * franja usar, nunca el orden de las 3 franjas entre si. */
private val ordenFranjas = listOf(Franja.GANCHO, Franja.CUERPO, Franja.CIERRE)

/**
 * El tablero de armado del discurso (ficha 24-LA-PLAZA.md): el nino elige UN bloque de
 * cada una de las 3 franjas (gancho/cuerpo/cierre, siempre en ese orden) entre las
 * opciones reales de [bloques] (contenido estatico, ya escrito -- nunca generado aqui).
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
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium, color = IndigoProfundo)
        Spacer(modifier = Modifier.height(8.dp))
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
                    onClick = { onSeleccionar(bloque.id) }
                )
            }
        }
    }
}

@Composable
private fun ChipBloque(
    bloque: BloqueContenidoEntity,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estadoTexto = if (seleccionado) {
        stringResource(R.string.armar_discurso_seleccionado)
    } else {
        stringResource(R.string.armar_discurso_no_seleccionado)
    }
    val descripcion = "${bloque.texto}, $estadoTexto"

    PanelDePlaza(
        colorBorde = if (seleccionado) AmbarFarol else IndigoProfundo,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier
            .widthIn(max = 280.dp)
            .clickable(onClickLabel = bloque.texto, role = Role.Button, onClick = onClick)
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
