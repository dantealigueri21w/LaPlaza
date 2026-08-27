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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.ui.art.IlustracionRinconBalcon
import pe.appmobile.laplaza.ui.art.IlustracionRinconFuente
import pe.appmobile.laplaza.ui.art.IlustracionRinconJardin
import pe.appmobile.laplaza.ui.art.IlustracionRinconKiosco
import pe.appmobile.laplaza.ui.art.IlustracionRinconMirador
import pe.appmobile.laplaza.ui.art.IlustracionRinconMostrador
import pe.appmobile.laplaza.ui.art.IlustracionRinconTarimaMayor
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.components.Chirri
import pe.appmobile.laplaza.ui.components.EstadoChirri
import pe.appmobile.laplaza.ui.components.GloboDeDialogo
import pe.appmobile.laplaza.ui.theme.BlancoRosado

private const val TOTAL_PASOS = 3

/**
 * El onboarding real de la ficha (seccion 5 del maestro): 3 pantallas, una sola vez,
 * antes de [CrearPerfilScreen] -- proposito, el mundo y Chirri, como se avanza mas el
 * aviso del permiso de microfono. "Una sola vez" no necesita un flag propio en Room: como
 * [pe.appmobile.laplaza.ui.navigation.LaPlazaNavHost] solo llega aqui cuando todavia no
 * hay perfil creado, y el perfil se crea una unica vez en la vida de la app, este paso
 * tampoco se vuelve a mostrar despues sin necesidad de guardar nada aparte.
 *
 * Paginado con estado interno simple (un indice 0..2), no [androidx.compose.foundation.pager.HorizontalPager]:
 * son solo 3 pasos fijos con contenido bien distinto entre si (no cuadros homogeneos que
 * se hojean), asi que un `when` es mas simple y no menos correcto.
 */
@Composable
fun OnboardingScreen(onTerminar: () -> Unit, modifier: Modifier = Modifier) {
    var paso by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (paso) {
            0 -> PasoProposito()
            1 -> PasoElMundo()
            else -> PasoComoAvanzar()
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (paso < TOTAL_PASOS - 1) {
            BotonDePlaza(label = stringResource(R.string.onboarding_siguiente), onClick = { paso++ })
        } else {
            BotonDePlaza(label = stringResource(R.string.onboarding_empezar), onClick = onTerminar)
        }
    }
}

@Composable
private fun PasoProposito() {
    Chirri(estado = EstadoChirri.SALUDANDO, modifier = Modifier.size(150.dp))
    GloboDeDialogo(texto = stringResource(R.string.onboarding_proposito_texto))
}

@Composable
private fun PasoElMundo() {
    Chirri(estado = EstadoChirri.PREOCUPADO, modifier = Modifier.size(150.dp))
    GloboDeDialogo(texto = stringResource(R.string.onboarding_mundo_texto))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        IlustracionRinconBalcon()
        IlustracionRinconKiosco()
        IlustracionRinconMostrador()
        IlustracionRinconJardin()
        IlustracionRinconFuente()
        IlustracionRinconMirador()
        IlustracionRinconTarimaMayor()
    }
}

@Composable
private fun PasoComoAvanzar() {
    Chirri(estado = EstadoChirri.ANIMANDO, modifier = Modifier.size(150.dp))
    GloboDeDialogo(texto = stringResource(R.string.onboarding_avanzar_texto))
    GloboDeDialogo(texto = stringResource(R.string.onboarding_microfono_texto), modifier = Modifier.fillMaxWidth())
}
