# Mapa Room actual

Archivos que deben revisarse antes de cambiar persistencia:

- `data/local/ActiveListeningDatabase.kt`: entidades, version y DAOs.
- `data/local/DatabaseMigrations.kt`: migraciones registradas.
- `data/settings/`: preferencias.
- `data/structure/`: propuesta original y estructura editada.
- `data/session/`: sesiones y posicion de reproduccion.
- `di/DatabaseModule.kt`: construccion de Room y registro de migraciones.
- `app/src/androidTest/.../ActiveListeningDatabaseMigrationTest.kt`: cobertura de bases antiguas.
- `app/schemas/`: JSON de esquemas exportados por KSP.

Configuracion relevante:

- El compiler de Room usa `ksp(libs.androidx.room.compiler)`.
- `room.schemaLocation` apunta a `app/schemas`.
- Las migraciones se suministran mediante `DatabaseMigrations.all`.
