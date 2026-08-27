package pe.appmobile.laplaza.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de la ficha de La Plaza (24-LA-PLAZA.md), verificada con la formula WCAG de
 * la seccion 6.1 del maestro (no a ojo). Regla fija de la propia ficha: indigo profundo
 * solo va sobre fondo o acento (los dos colores claros); blanco solo va sobre primario
 * o secundario (los dos colores saturados). Nunca al reves, en ningun tema -- indigo
 * sobre primario o secundario da ~2.1-2.6:1 y no pasa el minimo de 4.5:1.
 */

/** Primario: tarima, banners, boton principal. Texto blanco encima (5.55:1). */
val RosaBerenjena = Color(0xFFB93761)

/** Secundario: cielo, botones secundarios. Texto blanco encima (5.79:1). */
val VioletaAtardecer = Color(0xFF6C5B9E)

/** Texto principal, contornos, sombras. Va sobre fondo (11.1:1) o acento (5.99:1). */
val IndigoProfundo = Color(0xFF3D2C5F)

/** Acento: luces, insignias, destacados. Texto indigo encima, nunca blanco. */
val AmbarFarol = Color(0xFFF2A63D)

/** Fondo general de la app. */
val BlancoRosado = Color(0xFFFDF1F5)
