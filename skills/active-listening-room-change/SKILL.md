---
name: active-listening-room-change
description: Evoluciona la persistencia Room de ActiveListening de forma compatible, incluyendo entidades, DAOs, mapeos, version de base de datos, migraciones KSP y pruebas de migracion. Usar al anadir o modificar tablas, columnas, indices, relaciones, datos persistidos o contratos de repositorio respaldados por Room.
---

# Active Listening Room Change

Realiza cambios de esquema sin destruir sesiones, preferencias ni mapas estructurales guardados por usuarios existentes.

## Flujo

1. Lee `AGENTS.md`, revisa el worktree y abre los archivos indicados en [room-map.md](references/room-map.md).
2. Identifica la version actual, los esquemas exportados y todas las versiones antiguas que deben poder actualizarse.
3. Define el nuevo modelo persistido y su mapeo de dominio. No expongas entidades ni DAOs a ViewModels o Compose.
4. Actualiza entidad y DAO con el cambio minimo. Conserva nombres de tabla y columna salvo que el requisito exija cambiarlos.
5. Incrementa exactamente una vez la version de `ActiveListeningDatabase`.
6. Crea una `Migration(old, new)` explicita. No uses migracion destructiva como solucion.
7. Registra la migracion en `DatabaseMigrations.all` y conserva las migraciones anteriores.
8. Actualiza repositorios y mapeos. Decide valores para filas antiguas de forma explicita y coherente con el dominio.
9. Amplia `ActiveListeningDatabaseMigrationTest` para crear la base antigua, insertar datos representativos, migrar y comprobar esquema y contenido.
10. Actualiza pruebas unitarias de DAO o repositorio afectadas.
11. Ejecuta `./gradlew compileDebugKotlin` y `./gradlew testDebugUnitTest`. Ejecuta la prueba instrumentada de migracion cuando haya dispositivo o infraestructura disponible; si no, informa del comando pendiente.

## Reglas de seguridad

- Usa KSP, nunca KAPT, para Room.
- No borres ni recrees tablas si se pueden preservar los datos con SQL de migracion.
- Para cambios incompatibles, crea tabla temporal, copia columnas de forma explicita, elimina la antigua y renombra.
- Comprueba claves primarias, nulabilidad, defaults, indices y nombres reales generados por Room.
- Mantiene `room.schemaLocation` y los esquemas versionados.
- No mezcles el cambio de esquema con refactors no relacionados.

## Terminado

El cambio esta terminado cuando una instalacion nueva usa el esquema nuevo, una base antigua conserva sus datos al migrar, los repositorios siguen ocultando Room y las verificaciones aplicables pasan.
