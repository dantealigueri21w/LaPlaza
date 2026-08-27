package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.local.entity.TemaEntity
import pe.appmobile.laplaza.domain.model.SugerenciaRepaso
import pe.appmobile.laplaza.ui.components.PanelDePlaza
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/** Tamano minimo del objetivo de toque de una tarjeta de tema o de la sugerencia de
 * repaso -- misma regla de 120dp que BotonDePlaza aplica a las zonas del mapa (seccion
 * 3.1 del maestro): estas tarjetas son la accion primaria de esta pantalla. */
private val TamanoMinimoTapTarget = 120.dp

/**
 * Los temas disponibles para declamar en un rincon (sus 3 temas facil/medio/dificil) o,
 * si [tituloRincon] corresponde a Rincon Libre, los 21 temas de toda la plaza (eso lo
 * decide quien llama a esta pantalla -- ver LaPlazaNavHost.kt -- no esta pantalla).
 *
 * Regla de desbloqueo del proyecto (seccion 5.1 del maestro): TODOS los [temas] son
 * siempre tocables, sin importar dificultad ni progreso -- nunca hay una tarjeta
 * deshabilitada aqui, igual que ninguna zona del mapa de HomeScreen se deshabilita.
 *
 * Si [sugerenciaRepaso] no es null (solo pasa en Rincon Libre, ver ficha
 * 24-LA-PLAZA.md seccion "Repaso"), se muestra arriba de la lista con su [SugerenciaRepaso.motivo]
 * real -- nunca un texto inventado -- y tambien es tocable.
 */
@Composable
fun TemasDeRinconScreen(
    tituloRincon: String,
    temas: List<TemaEntity>,
    sugerenciaRepaso: SugerenciaRepaso?,
    onSeleccionarTema: (Long) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val descripcionVolver = stringResource(R.string.accion_volver)

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
                text = tituloRincon,
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
        ) {
            if (sugerenciaRepaso != null) {
                TarjetaSugerenciaRepaso(
                    sugerencia = sugerenciaRepaso,
                    onClick = { onSeleccionarTema(sugerenciaRepaso.temaId) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            temas.forEach { tema ->
                TarjetaTema(tema = tema, onClick = { onSeleccionarTema(tema.id) })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TarjetaTema(tema: TemaEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.temas_rincon_tema_descripcion, tema.titulo, tema.dificultad)

    PanelDePlaza(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = TamanoMinimoTapTarget)
            .clickable(onClickLabel = tema.titulo, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion }
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = tema.titulo, style = MaterialTheme.typography.titleMedium, color = IndigoProfundo)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = tema.dificultad, style = MaterialTheme.typography.bodyMedium, color = IndigoProfundo)
        }
    }
}

@Composable
private fun TarjetaSugerenciaRepaso(sugerencia: SugerenciaRepaso, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val titulo = stringResource(R.string.temas_rincon_sugerencia_titulo)
    val descripcion = stringResource(R.string.temas_rincon_sugerencia_descripcion, sugerencia.motivo)

    PanelDePlaza(
        color = AmbarFarol.copy(alpha = 0.25f),
        colorBorde = AmbarFarol,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = TamanoMinimoTapTarget)
            .clickable(onClickLabel = titulo, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion }
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = IndigoProfundo)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = titulo, style = MaterialTheme.typography.titleMedium, color = IndigoProfundo)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = sugerencia.motivo, style = MaterialTheme.typography.bodyMedium, color = IndigoProfundo)
        }
    }
}
