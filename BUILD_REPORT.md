# Bitácora de compilación — La Plaza

Compilado en local, máquina de desarrollo (sección 15 del maestro, v8): esta build local es el
entregable final, no una que descargue GitHub Actions después del push.

## Verificación final, `./gradlew clean`

### `./gradlew testDebugUnitTest`
```
BUILD SUCCESSFUL in 1m 56s
56 actionable tasks: 56 executed
```
**200 pruebas, 0 fallos, 0 errores.**

### `./gradlew lintDebug`
```
BUILD SUCCESSFUL
```
Sin advertencias de accesibilidad, sin permisos fuera de lo esperado, sin código muerto marcado.

### `./gradlew assembleDebug`
```
BUILD SUCCESSFUL
```
APK en `app/build/outputs/apk/debug/app-debug.apk`, **20 MB** (dentro del rango esperado de
15–25 MB para arte 100% Canvas, sección 14.1 del maestro).

## Verificaciones de la sección 4.0 (arte Canvas)

```
grep -rhoE "fun (Ilustracion|Insignia)[A-Za-z]+" app/src/main/java --include="*.kt" | sort -u | wc -l
→ 22   (mínimo exigido: 17)

grep -rc "Brush\.\(linear\|radial\|sweep\)Gradient\|\.shadow(\|cubicTo\|quadraticTo" app/src/main/java --include="*.kt" | awk -F: '{s+=$2} END{print s+0}'
→ 62   (se acerca al primero: no son formas planas)
```

12 insignias reales de la ficha + 8 símbolos propios de rincón (uno por cada uno de los 7
rincones + Rincón Libre, visibles en el mapa de la plaza) + Chirri (con degradado radial y
sombra de contacto, agregados en esta pasada) + la estrella del Cuaderno de Pregones.

## Verificación de la sección 1.1 (mecanismo real, no opción múltiple)

```
grep -rEc "isCorrect|correctAnswer|respuestaCorrecta|esCorrecta|correctIndex|opcionCorrecta" app/src/main/java → 0
grep -rEc "detectDragGestures|\.draggable|detectTransformGestures|rememberDraggable" app/src/main/java → 2
```
El armado del discurso (gancho/cuerpo/cierre) se resuelve arrastrando cada bloque a su ranura;
tocarlo es el equivalente accesible, no el mecanismo principal.

## Verificación de la sección 9 (offline y privacidad)

```
grep -n "uses-permission" app/src/main/AndroidManifest.xml
→ android.permission.RECORD_AUDIO   (el único; sin INTERNET)
```

## Verificación de la sección 11 (higiene del repositorio)

```
git ls-files | xargs grep -il "claude|anthropic|chatgpt|openai|copilot|gemini"  → sin resultados
git ls-files | xargs grep -l "RODRIGO"                                          → sin resultados
git log --format='%an <%ae>' | sort -u → dantealigueri21w <...@users.noreply.github.com>
```

## Se jugó un ciclo real completo en un emulador (sección 10.3 del maestro)

AVD Android 14 (API 34), instalación del APK real, recorrido completo: onboarding → crear
perfil → mapa de la plaza → El Balcón → armar el discurso arrastrando los 3 bloques de verdad
→ declamar con el micrófono real (permiso concedido, calibración, indicador en vivo, público
reaccionando) → Terminar → pregón real generado → Continuar → racha y rincón completado
visibles en el mapa → entrada real en el Cuaderno de Pregones → insignias reales otorgadas
(Primera Voz, Sin Cortes) visibles en el perfil.

Este recorrido encontró y corrigió **tres bugs reales** que ningún test en verde había
detectado (la misma lección de la sección 10.3: los tests no prueban por sí solos que la
mecánica se pueda completar):

1. **El armado de discurso no era un arrastre real**, era tocar para elegir (opción múltiple
   disfrazada, sección 1 del maestro). Se reescribió con un gesto de arrastre real
   (`detectDragGestures`) hacia la ranura de cada franja, con el toque como equivalente
   accesible.
2. **Un botón sin tamaño propio se estiraba a todo el ancho disponible** dentro de cualquier
   pantalla con scroll (altura entrante infinita) — visto primero en el onboarding, con el
   texto del botón encimado sobre el dibujo del farol. Corregido con un tamaño fijo de 140dp.
3. **Terminar la declamación no hacía nada.** Una carrera real entre el hilo de UI (al tocar
   "Terminar") y el hilo de captura de audio en segundo plano podía revertir el estado de
   `Detenido` de vuelta a `Escuchando` justo después de fijarlo, dejando la pantalla
   congelada. Corregido con un guard (`@Volatile`) que impide que una lectura de bloque ya en
   vuelo sobreescriba un cierre ya iniciado.

Los tres quedaron cubiertos con pruebas nuevas que reproducen la causa real (no solo el
síntoma) antes de corregirlos.

Emulador cerrado limpio al terminar (`adb emu kill` + verificación de que no quedan procesos
`qemu-system-x86_64` corriendo).

## Alcance de esta build

Motores de dominio, capa Room, seed real (7 rincones · 21 temas · 189 bloques · 12 insignias),
las 12 pantallas de la ficha con contenido real, micrófono real, Cuaderno de Pregones real,
racha diaria real, motor de insignias real, onboarding de 3 pasos, y arte 100% Canvas con
degradados/sombra/curvas en Chirri, los símbolos de rincón y las insignias.

Pendiente antes de la entrega final (fuera del alcance de esta build): memoria descriptiva y
manual de usuario (fase 2, con capturas del APK real), y la carpeta de entrega numerada
(sección 14.3 del maestro).

## Corrección posterior a la Fase 2, 27/08/2026: bug real de fluidez en silencio total

Rodrigo probó el APK real en su celular sin decir ninguna palabra durante una declamación
(solo armó el discurso y tocó Hablar/Terminar), y la app respondió "Dijiste... sin cortes,
de principio a fin", como si el discurso se hubiera dicho completo. El mismo patrón se
reprodujo, sin notarlo en el momento, en el emulador de la Fase 2 (grabación con `-no-audio`,
sin voz real de principio a fin).

**Causa real:** `MotorAcustico.detectarPausas` agrupa un silencio continuo en una sola
`Pausa`, y esa pausa puede llegar a durar la grabación entera. Antes de esta corrección,
`MotorFluidez.calcularFluidez` penalizaba cualquier pausa "sucia" aislada (fuera de 200-900
ms) con un monto fijo de 0.05, sin importar si duraba 901 ms o los 8 segundos completos de la
grabación. Silencio total terminaba en fluidez ≈0.95, arriba del umbral de 0.9 que
`MotorInsignias` usa para otorgar la insignia "Sin Cortes" — se podía ganar esa insignia sin
haber hablado.

**Corrección:** la penalización de una pausa aislada fuera del rango limpio ahora escala con
la proporción de la grabación entera que esa pausa ocupa (`pausa.duracionMs /
duracionTotalMs`), no un monto fijo. Silencio total o casi total cae a fluidez cercana a 0;
una pausa corta y aislada dentro de un discurso mayormente hablado sigue penalizando poco,
igual que antes. `MotorFluidezTest.kt` tiene 3 pruebas nuevas que reproducen el caso real
(silencio total, discurso a medias, y el caso normal ya cubierto sin cambios).

```
./gradlew clean testDebugUnitTest
→ BUILD SUCCESSFUL, 203 pruebas, 0 fallos, 0 errores (200 anteriores + 3 nuevas)

./gradlew lintDebug
→ BUILD SUCCESSFUL

./gradlew assembleDebug
→ BUILD SUCCESSFUL
```

## Corrección posterior a la Fase 2, 27/08/2026: la app no tenía ícono de lanzador

El manifiesto no declaraba `android:icon`, y no existía ningún recurso `mipmap-*`: al
instalar la app, Android mostraba el ícono genérico del sistema en vez de uno propio.

Se agregó un ícono adaptable (API 26+) más una versión plana de respaldo para API 24-25 (el
`minSdk` real del proyecto), ambos como `VectorDrawable` en XML, sin ningún archivo de imagen
externo (misma vía de arte 100% código que ya usa el resto de la app, sección 4.0 del
maestro): el mismo farol que ya dibuja `BotonDePlaza` en toda la app, en la paleta real de
`ui/theme/Color.kt` (fondo `RosaBerenjena`, farol en `AmbarFarol`/`IndigoProfundo`).

```
app/src/main/res/
  drawable/ic_launcher_background.xml
  drawable/ic_launcher_foreground.xml
  mipmap-anydpi-v26/ic_launcher.xml        (adaptive-icon, API 26+)
  mipmap-anydpi-v26/ic_launcher_round.xml
  mipmap/ic_launcher.xml                    (vector plano, respaldo API 24-25)
  mipmap/ic_launcher_round.xml
```

Verificado con `aapt2 dump badging` sobre el APK recompilado:
`application: label='La Plaza' icon='res/mipmap-anydpi-v26/ic_launcher.xml'` (antes,
`icon=''`). Permisos sin cambios: solo `RECORD_AUDIO`, sin `INTERNET`.
