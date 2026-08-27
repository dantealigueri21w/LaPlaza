-- La Plaza — esquema de la base de datos local (SQLite vía Room), versión 2.
--
-- Transcripción del esquema que Room exporta en
-- app/schemas/pe.appmobile.laplaza.data.local.AppDatabase/2.json,
-- con los nombres de tabla ya resueltos. Toda la información vive en el
-- dispositivo: no hay servidor, ni cuentas, ni sincronización. El audio del
-- micrófono nunca se guarda: solo las cuatro métricas derivadas de cada intento.

-- Perfil del jugador. Fila única (id autogenerado, en la práctica siempre 1).
-- Nunca guarda nombre real ni ningún dato que identifique al niño: solo un
-- apodo elegido por él y el número de uno de los 8 avatares locales.
CREATE TABLE IF NOT EXISTS `perfil` (
    `id`              INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `alias`           TEXT    NOT NULL,
    `avatarId`        INTEGER NOT NULL,
    `sonidoActivado`  INTEGER NOT NULL,
    `hapticaActivada` INTEGER NOT NULL
);

-- Los 7 rincones de la plaza. Rincón Libre no tiene fila aquí: existe solo
-- como destino de navegación, reutilizando los temas de los 7 rincones reales.
CREATE TABLE IF NOT EXISTS `rincon` (
    `id`          TEXT    NOT NULL,
    `nombre`      TEXT    NOT NULL,
    `descripcion` TEXT    NOT NULL,
    `orden`       INTEGER NOT NULL,
    `completado`  INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);

-- Los 21 temas de discurso (3 por rincón: fácil, medio, difícil).
CREATE TABLE IF NOT EXISTS `tema` (
    `id`         INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `rinconId`   TEXT    NOT NULL,
    `titulo`     TEXT    NOT NULL,
    `dificultad` TEXT    NOT NULL,
    `orden`      INTEGER NOT NULL,
    FOREIGN KEY(`rinconId`) REFERENCES `rincon`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE INDEX IF NOT EXISTS `index_tema_rinconId` ON `tema` (`rinconId`);

-- Los 189 bloques de contenido (9 por tema: 3 de gancho, 3 de cuerpo, 3 de
-- cierre) que el niño arrastra para armar su discurso.
CREATE TABLE IF NOT EXISTS `bloque_contenido` (
    `id`     INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `temaId` INTEGER NOT NULL,
    `franja` TEXT    NOT NULL,
    `texto`  TEXT    NOT NULL,
    `orden`  INTEGER NOT NULL,
    FOREIGN KEY(`temaId`) REFERENCES `tema`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE INDEX IF NOT EXISTS `index_bloque_contenido_temaId` ON `bloque_contenido` (`temaId`);

-- Cada declamación real: las 4 variables medidas en vivo (volumen, entonación,
-- ritmo, fluidez) y el puntaje compuesto que resulta de combinarlas.
-- `viaRinconLibre` = 1 cuando el intento se hizo practicando en el Rincón
-- Libre: esos intentos no generan una página nueva en el Cuaderno de Pregones
-- ni cuentan para las insignias por resultado (ver la ficha, "Rincón Libre").
CREATE TABLE IF NOT EXISTS `intento` (
    `id`                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `temaId`            INTEGER NOT NULL,
    `fechaEpochMs`      INTEGER NOT NULL,
    `esRepaso`          INTEGER NOT NULL,
    `puntajeVolumen`    REAL    NOT NULL,
    `puntajeEntonacion` REAL    NOT NULL,
    `puntajeRitmo`      REAL    NOT NULL,
    `puntajeFluidez`    REAL    NOT NULL,
    `puntajeCompuesto`  REAL    NOT NULL,
    `viaRinconLibre`    INTEGER NOT NULL,
    FOREIGN KEY(`temaId`) REFERENCES `tema`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE INDEX IF NOT EXISTS `index_intento_temaId` ON `intento` (`temaId`);

-- El Cuaderno de Pregones: un titular real generado a partir de cada intento
-- (fuera de Rincón Libre), con la variable que más brilló ese día.
CREATE TABLE IF NOT EXISTS `pregon` (
    `id`                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `intentoId`         INTEGER NOT NULL,
    `titular`           TEXT    NOT NULL,
    `variableDestacada` TEXT    NOT NULL,
    `fechaEpochMs`      INTEGER NOT NULL,
    FOREIGN KEY(`intentoId`) REFERENCES `intento`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
);
CREATE INDEX IF NOT EXISTS `index_pregon_intentoId` ON `pregon` (`intentoId`);

-- Las 12 insignias. `fechaObtenidaEpochMs` NULL significa que todavía no se
-- ganó; se llena la primera vez que el motor de insignias la detecta ganada.
CREATE TABLE IF NOT EXISTS `insignia` (
    `id`                    TEXT    NOT NULL,
    `nombre`                TEXT    NOT NULL,
    `descripcion`           TEXT    NOT NULL,
    `fechaObtenidaEpochMs`  INTEGER,
    PRIMARY KEY(`id`)
);

-- Racha de días seguidos con al menos una actividad completada. Fila única
-- (id fijo = 1), recalculada tras cada declamación desde las fechas reales
-- de `intento`.
CREATE TABLE IF NOT EXISTS `racha` (
    `id`             INTEGER NOT NULL,
    `diasSeguidos`   INTEGER NOT NULL,
    `ultimoDiaEpoch` INTEGER NOT NULL,
    PRIMARY KEY(`id`)
);
