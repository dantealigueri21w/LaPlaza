package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.Chirri
import pe.appmobile.laplaza.ui.components.EstadoChirri
import pe.appmobile.laplaza.ui.components.PanelDePlaza
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/**
 * Destino minimo para las 9 pantallas que esta tarea no construye de verdad (los 7
 * rincones, Rincon Libre y el Cuaderno de Pregones): que navegar hasta ahi no reviente y
 * no se sienta como un enlace roto, con el titulo real de la ficha y una forma de volver.
 * Una tarea posterior reemplaza cada uno de estos destinos, uno por uno, con contenido
 * real (el tablero de armado del discurso, la captura de audio, el cuaderno de verdad).
 *
 * Importante: este mensaje de "todavia no esta listo" es sobre el ESTADO DE CONSTRUCCION
 * de la app, nunca sobre el progreso del nino -- la regla del proyecto es que ningun
 * rincon se bloquea por progreso (seccion 5.1 del maestro), asi que este marcador se
 * muestra igual sin importar cuanto haya avanzado quien juega.
 */
@Composable
fun PantallaMarcador(
    titulo: String,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Chirri(estado = EstadoChirri.NEUTRAL, modifier = Modifier.size(140.dp))
        Spacer(modifier = Modifier.height(16.dp))
        PanelDePlaza {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = IndigoProfundo,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.marcador_mensaje),
                    style = MaterialTheme.typography.bodyMedium,
                    color = IndigoProfundo,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        BotonDePlaza(label = stringResource(R.string.accion_volver), onClick = onVolver)
    }
}
