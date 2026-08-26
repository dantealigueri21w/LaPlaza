# Bitácora de compilación — La Plaza

## Fase: scaffolding + dominio (2026-08-25)

Compilado en local, máquina de desarrollo (no GitHub Actions — ese hash llega al final, sección 15 del maestro, cuando se suba a GitHub por primera vez).

### `./gradlew clean testDebugUnitTest`
```
BUILD SUCCESSFUL in 28s
30 actionable tasks: 29 executed, 1 up-to-date
```
53 pruebas, 0 fallos, 0 errores, repartidas en 6 motores de dominio:

| Motor | Tests |
|---|---|
| MotorAcustico | 17 |
| MotorPuntajeAudiencia | 9 |
| MotorFluidez | 7 |
| MotorDiscurso | 6 |
| MotorPregon | 5 |
| MotorProgreso | 9 |
| **Total** | **53** |

### `./gradlew lintDebug`
```
BUILD SUCCESSFUL in 1m 2s
28 actionable tasks: 9 executed, 19 up-to-date
```

### `./gradlew assembleDebug`
```
BUILD SUCCESSFUL in 31s
38 actionable tasks: 18 executed, 20 up-to-date
```
APK de depuración generado en `app/build/outputs/apk/debug/app-debug.apk`.

### Alcance de esta fase

Motores de dominio completos y probados: MotorAcustico, MotorPuntajeAudiencia, MotorFluidez,
MotorDiscurso, MotorPregon, MotorProgreso — 53 pruebas, todas pasando. Sin Room, sin UI, sin
permiso de micrófono todavía: eso es el siguiente plan (`la-plaza-room-datos.md`, capa de datos)
y el plan de pantallas que viene después de eso, cuando exista el arte generado.
