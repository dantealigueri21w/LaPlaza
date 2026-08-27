package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.local.entity.InsigniaEntity
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.ui.art.InsigniaBalconDominado
import pe.appmobile.laplaza.ui.art.InsigniaBuenConsuelo
import pe.appmobile.laplaza.ui.art.InsigniaBuenVendedor
import pe.appmobile.laplaza.ui.art.InsigniaCierreDeAplausos
import pe.appmobile.laplaza.ui.art.InsigniaCompaneroDeChirri
import pe.appmobile.laplaza.ui.art.InsigniaGanchoQuePrende
import pe.appmobile.laplaza.ui.art.InsigniaGratitudSincera
import pe.appmobile.laplaza.ui.art.InsigniaLaTarimaEsTuya
import pe.appmobile.laplaza.ui.art.InsigniaPrimeraVoz
import pe.appmobile.laplaza.ui.art.InsigniaSeOyoEnTodaLaPlaza
import pe.appmobile.laplaza.ui.art.InsigniaSinCortes
import pe.appmobile.laplaza.ui.art.InsigniaVozDelKiosco
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.SelectorDeAvatares
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/**
 * Ver/editar el perfil ya creado -- distinta de [CrearPerfilScreen], que solo existe una
 * vez, en el primer arranque. Reutiliza el mismo [SelectorDeAvatares] que la creacion.
 *
 * Sin estado propio de persistencia: [onGuardar] recibe el alias y avatarId editados, y
 * quien la use decide como guardarlos (el ViewModel real, en el NavHost).
 */
@Composable
fun PerfilScreen(
    perfil: PerfilEntity,
    onGuardar: (alias: String, avatarId: Int) -> Unit,
    onVolver: () -> Unit,
    insignias: List<InsigniaEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var alias by remember(perfil.id) { mutableStateOf(perfil.alias) }
    var avatarSeleccionado by remember(perfil.id) { mutableStateOf(perfil.avatarId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.perfil_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = IndigoProfundo
        )

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            singleLine = true,
            label = { Text(stringResource(R.string.perfil_editar_alias)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.perfil_elige_avatar),
            style = MaterialTheme.typography.titleLarge,
            color = IndigoProfundo
        )
        Spacer(modifier = Modifier.height(8.dp))
        SelectorDeAvatares(
            seleccionado = avatarSeleccionado,
            onSeleccionar = { avatarSeleccionado = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row {
            BotonDePlaza(
                label = stringResource(R.string.perfil_guardar),
                onClick = { onGuardar(alias.trim().ifBlank { perfil.alias }, avatarSeleccionado) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            BotonDePlaza(label = stringResource(R.string.perfil_volver), onClick = onVolver)
        }

        if (insignias.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.perfil_insignias_titulo),
                style = MaterialTheme.typography.titleLarge,
                color = IndigoProfundo
            )
            Spacer(modifier = Modifier.height(8.dp))
            GaleriaDeInsignias(insignias)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** 12 insignias, 3 por fila -- `Column` con filas armadas a mano (seccion 7.1 punto 6
 * del maestro: esta pantalla ya hace scroll con [verticalScroll], asi que ninguna grilla
 * dentro puede ser perezosa). */
@Composable
private fun GaleriaDeInsignias(insignias: List<InsigniaEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        insignias.chunked(3).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { insignia ->
                    ItemDeInsignia(insignia, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ItemDeInsignia(insignia: InsigniaEntity, modifier: Modifier = Modifier) {
    val ganada = insignia.fechaObtenidaEpochMs != null
    val estado = if (ganada) {
        stringResource(R.string.perfil_insignia_ganada)
    } else {
        stringResource(R.string.perfil_insignia_por_ganar)
    }
    Column(
        modifier = modifier.semantics { contentDescription = "${insignia.nombre}, $estado. ${insignia.descripcion}" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IlustracionDeInsignia(insignia.id, ganada)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = insignia.nombre,
            style = MaterialTheme.typography.labelMedium,
            color = IndigoProfundo,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IlustracionDeInsignia(id: String, ganada: Boolean) {
    when (id) {
        "PRIMERA_VOZ" -> InsigniaPrimeraVoz(ganada)
        "GANCHO_QUE_PRENDE" -> InsigniaGanchoQuePrende(ganada)
        "CIERRE_DE_APLAUSOS" -> InsigniaCierreDeAplausos(ganada)
        "SIN_CORTES" -> InsigniaSinCortes(ganada)
        "BALCON_DOMINADO" -> InsigniaBalconDominado(ganada)
        "VOZ_DEL_KIOSCO" -> InsigniaVozDelKiosco(ganada)
        "BUEN_VENDEDOR" -> InsigniaBuenVendedor(ganada)
        "SE_OYO_EN_TODA_LA_PLAZA" -> InsigniaSeOyoEnTodaLaPlaza(ganada)
        "GRATITUD_SINCERA" -> InsigniaGratitudSincera(ganada)
        "BUEN_CONSUELO" -> InsigniaBuenConsuelo(ganada)
        "LA_TARIMA_ES_TUYA" -> InsigniaLaTarimaEsTuya(ganada)
        "COMPANERO_DE_CHIRRI" -> InsigniaCompaneroDeChirri(ganada)
        else -> InsigniaPrimeraVoz(ganada)
    }
}
