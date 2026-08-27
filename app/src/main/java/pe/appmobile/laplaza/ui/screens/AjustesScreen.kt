package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo

/**
 * Ajustes minimos: por ahora solo sonido on/off. Seccion 3.1 del maestro exceptua
 * explicitamente a ajustes de la regla "nunca Material Card/Button/Scaffold para la
 * superficie de juego" -- esta pantalla no es superficie de juego, es configuracion, asi
 * que un Switch de Material es la eleccion correcta (no hay un control reutilizable de
 * on/off entre los componentes de La Plaza, y no vale la pena inventar uno).
 *
 * [sonidoActivado] persiste en PerfilEntity.sonidoActivado (ya existia en la entidad
 * antes de esta tarea, ver data/local/entity/PerfilEntity.kt) -- se decidio persistirlo
 * ahi, en vez de en memoria, porque es una preferencia real del nino que debe sobrevivir
 * a cerrar la app, y el perfil ya es la unica fila de "quien soy" que existe.
 */
@Composable
fun AjustesScreen(
    sonidoActivado: Boolean,
    onCambiarSonido: (Boolean) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.ajustes_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = IndigoProfundo
        )

        val textoSonido = stringResource(R.string.ajustes_sonido)
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = textoSonido,
                color = IndigoProfundo,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = sonidoActivado,
                onCheckedChange = onCambiarSonido,
                modifier = Modifier.semantics { contentDescription = textoSonido }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        BotonDePlaza(label = stringResource(R.string.ajustes_volver), onClick = onVolver)
    }
}
