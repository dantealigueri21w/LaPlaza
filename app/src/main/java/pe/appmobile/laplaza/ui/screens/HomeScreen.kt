package pe.appmobile.laplaza.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.appmobile.laplaza.R
import pe.appmobile.laplaza.data.local.entity.PerfilEntity
import pe.appmobile.laplaza.data.local.entity.RachaEntity
import pe.appmobile.laplaza.data.local.entity.RinconEntity
import pe.appmobile.laplaza.ui.components.AvatarDePlaza
import pe.appmobile.laplaza.ui.components.BotonDePlaza
import pe.appmobile.laplaza.ui.navigation.Rutas
import pe.appmobile.laplaza.ui.theme.AmbarFarol
import pe.appmobile.laplaza.ui.theme.BlancoRosado
import pe.appmobile.laplaza.ui.theme.IndigoProfundo
import pe.appmobile.laplaza.ui.theme.RosaBerenjena
import pe.appmobile.laplaza.ui.theme.VioletaAtardecer

/** Mismo tamano que el minimo real que ya fuerza BotonDePlaza (ver ui/components/BotonDePlaza.kt,
 * defaultMinSize de 140dp): las posiciones de la cuadricula se calculan a partir de este
 * valor para que dos zonas nunca se encimen, no para forzar un tamano distinto al que el
 * propio boton ya aplica. */
private val TAMANO_ZONA = 140.dp

/** Separacion entre zonas y margen exterior de la plaza -- fijos, no una fraccion del
 * ancho de pantalla: usar dp absolutos (en vez de fracciones de BoxWithConstraints) es lo
 * que garantiza que dos zonas nunca terminen ocupando el mismo lugar, sin importar el
 * ancho real del telefono (una fraccion del ancho disponible puede colapsar a 0 en una
 * pantalla angosta y hacer que todas las zonas se apilen en el mismo punto). */
private val ESPACIO_ENTRE_ZONAS = 16.dp
private val MARGEN_PLAZA = 16.dp
private val PASO_ZONA = TAMANO_ZONA + ESPACIO_ENTRE_ZONAS

/** El mapa es una cuadricula fija de 2 columnas x 4 filas -- exactamente 8 lugares para
 * los 7 rincones + Rincon Libre. El ancho total (2 columnas + margenes) cabe sin
 * necesidad de scroll horizontal en cualquier telefono real; el contenedor solo hace
 * scroll vertical (ver [MapaDePlaza]) para las filas que no entran en una pantalla. */
private val ANCHO_PLAZA = MARGEN_PLAZA * 2 + PASO_ZONA * 2 - ESPACIO_ENTRE_ZONAS
private val ALTO_PLAZA = MARGEN_PLAZA * 2 + PASO_ZONA * 4 - ESPACIO_ENTRE_ZONAS

private fun posicionColumna(columna: Int): Dp = MARGEN_PLAZA + PASO_ZONA * columna
private fun posicionFila(fila: Int): Dp = MARGEN_PLAZA + PASO_ZONA * fila

/**
 * El mapa de la plaza (home). La ficha lo describe como "la plaza vista de arriba en
 * angulo isometrico: se toca un rincon para entrar a hablar ahi" -- sin arte real
 * todavia, esta version dibuja un fondo de plaza simple en Canvas y posiciona los 7
 * rincones + Rincon Libre encima con offsets, nunca como una lista de Cards (seccion 3.1
 * del maestro).
 *
 * Regla de desbloqueo del proyecto (seccion 5.1 del maestro, corrige a la ficha): TODOS
 * los rincones son tocables y jugables desde el primer minuto, sin excepcion. Lo unico
 * que varia con el progreso es el estado visual "disponible" vs "completado" de cada
 * zona (ver [ZonaDeRincon]) -- nunca si se puede o no entrar.
 */
@Composable
fun HomeScreen(
    perfil: PerfilEntity,
    rincones: List<RinconEntity>,
    racha: RachaEntity?,
    onNavegarRincon: (String) -> Unit,
    onNavegarPerfil: () -> Unit,
    onNavegarAjustes: () -> Unit,
    onNavegarCuaderno: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlancoRosado)
    ) {
        EncabezadoHome(
            perfil = perfil,
            racha = racha,
            onNavegarPerfil = onNavegarPerfil,
            onNavegarAjustes = onNavegarAjustes,
            onNavegarCuaderno = onNavegarCuaderno
        )
        MapaDePlaza(
            rincones = rincones,
            onNavegarRincon = onNavegarRincon,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun EncabezadoHome(
    perfil: PerfilEntity,
    racha: RachaEntity?,
    onNavegarPerfil: () -> Unit,
    onNavegarAjustes: () -> Unit,
    onNavegarCuaderno: () -> Unit
) {
    val descripcionAvatar = stringResource(R.string.home_avatar_descripcion, perfil.alias)
    val descripcionCuaderno = stringResource(R.string.home_ir_cuaderno)
    val descripcionPerfil = stringResource(R.string.home_ir_perfil)
    val descripcionAjustes = stringResource(R.string.home_ir_ajustes)
    val textoRacha = stringResource(R.string.home_racha_texto, racha?.diasSeguidos ?: 0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarDePlaza(
            avatarId = perfil.avatarId,
            modifier = Modifier.size(56.dp),
            descripcion = descripcionAvatar
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = perfil.alias,
                style = MaterialTheme.typography.titleLarge,
                color = IndigoProfundo
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = AmbarFarol
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = textoRacha, color = IndigoProfundo, style = MaterialTheme.typography.bodyMedium)
            }
        }
        IconButton(onClick = onNavegarCuaderno) {
            Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = descripcionCuaderno, tint = IndigoProfundo)
        }
        IconButton(onClick = onNavegarPerfil) {
            Icon(imageVector = Icons.Filled.Person, contentDescription = descripcionPerfil, tint = IndigoProfundo)
        }
        IconButton(onClick = onNavegarAjustes) {
            Icon(imageVector = Icons.Filled.Settings, contentDescription = descripcionAjustes, tint = IndigoProfundo)
        }
    }
}

/** Una zona tocable del mapa: un id de rincon real (o [Rutas.ID_RINCON_LIBRE]), su
 * nombre, si esta completado, y su posicion fija (fila, columna) en la cuadricula de 2x4
 * de la plaza (ver [ANCHO_PLAZA]/[ALTO_PLAZA]). */
private data class ZonaPlaza(
    val id: String,
    val nombre: String,
    val completado: Boolean,
    val fila: Int,
    val columna: Int
)

/** Lugar de cada rincon en la cuadricula de 2 columnas x 4 filas -- puramente
 * espacial/visual (un balcon arriba, la tarima abajo como cierre), nunca de desbloqueo:
 * el orden de la ficha es sugerido, no una compuerta (seccion 5.1 del maestro). */
private val POSICIONES: Map<String, Pair<Int, Int>> = mapOf(
    "BALCON" to (0 to 0),
    "KIOSCO" to (0 to 1),
    "MOSTRADOR" to (1 to 0),
    "JARDIN" to (1 to 1),
    "FUENTE" to (2 to 0),
    "MIRADOR" to (2 to 1),
    "TARIMA_MAYOR" to (3 to 0),
    "LIBRE" to (3 to 1)
)

private fun construirZonas(rincones: List<RinconEntity>, nombreLibre: String): List<ZonaPlaza> {
    val zonasDeRincones = rincones.map { rincon ->
        val (fila, columna) = POSICIONES[rincon.id] ?: (0 to 0)
        ZonaPlaza(id = rincon.id, nombre = rincon.nombre, completado = rincon.completado, fila = fila, columna = columna)
    }
    val (filaLibre, columnaLibre) = POSICIONES.getValue("LIBRE")
    val zonaLibre = ZonaPlaza(
        id = Rutas.ID_RINCON_LIBRE,
        nombre = nombreLibre,
        completado = false,
        fila = filaLibre,
        columna = columnaLibre
    )
    return zonasDeRincones + zonaLibre
}

@Composable
private fun MapaDePlaza(
    rincones: List<RinconEntity>,
    onNavegarRincon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val nombreLibre = stringResource(R.string.home_rincon_libre)
    val zonas = remember(rincones, nombreLibre) { construirZonas(rincones, nombreLibre) }
    val descripcionPlaza = stringResource(R.string.home_plaza_descripcion)

    Box(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.size(width = ANCHO_PLAZA, height = ALTO_PLAZA)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = descripcionPlaza }
            ) {
                dibujarFondoDePlaza()
            }

            zonas.forEach { zona ->
                ZonaDeRincon(
                    zona = zona,
                    onClick = { onNavegarRincon(zona.id) },
                    modifier = Modifier
                        .offset(x = posicionColumna(zona.columna), y = posicionFila(zona.fila))
                        .size(TAMANO_ZONA)
                )
            }
        }
    }
}

/**
 * Una zona del mapa: siempre tocable ([BotonDePlaza] siempre con `enabled = true`, sin
 * importar el progreso -- ver la nota de desbloqueo arriba), con una insignia visual
 * (una estrella, no solo un cambio de color) cuando el rincon esta completado.
 */
@Composable
private fun ZonaDeRincon(zona: ZonaPlaza, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val estadoTexto = if (zona.completado) {
        stringResource(R.string.home_estado_completado)
    } else {
        stringResource(R.string.home_estado_disponible)
    }

    Box(modifier = modifier) {
        BotonDePlaza(
            label = zona.nombre,
            onClick = onClick,
            enabled = true,
            descripcion = "${zona.nombre}, $estadoTexto"
        )
        if (zona.completado) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .background(AmbarFarol, CircleShape)
                    .border(1.5.dp, IndigoProfundo, CircleShape)
            ) {
                Text(
                    text = "★",
                    color = IndigoProfundo,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/** Fondo de la plaza: una cuadricula suave a modo de adoquin y un par de toldos
 * sugeridos arriba, con colores de la paleta a baja opacidad -- no hay arte real
 * todavia (02-GUIA-IMAGENES.md aun no se ejecuto para esta app). */
private fun DrawScope.dibujarFondoDePlaza() {
    drawRect(color = BlancoRosado, size = size)

    val filas = 14
    val columnas = 8
    val anchoCelda = size.width / columnas
    val altoCelda = size.height / filas
    for (fila in 0 until filas) {
        for (columna in 0 until columnas) {
            val colorCelda = if ((fila + columna) % 2 == 0) {
                AmbarFarol.copy(alpha = 0.08f)
            } else {
                VioletaAtardecer.copy(alpha = 0.06f)
            }
            drawRect(
                color = colorCelda,
                topLeft = Offset(columna * anchoCelda, fila * altoCelda),
                size = Size(anchoCelda - 2f, altoCelda - 2f)
            )
        }
    }

    val colorToldo = RosaBerenjena.copy(alpha = 0.45f)
    drawArc(
        color = colorToldo,
        startAngle = 0f,
        sweepAngle = -180f,
        useCenter = true,
        topLeft = Offset(size.width * 0.10f, -50f),
        size = Size(150f, 120f)
    )
    drawArc(
        color = colorToldo,
        startAngle = 0f,
        sweepAngle = -180f,
        useCenter = true,
        topLeft = Offset(size.width * 0.58f, -50f),
        size = Size(150f, 120f)
    )
}
