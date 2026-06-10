# Mapa de arquitectura

- UI principal: `app/src/main/java/com/mrmustard/activelistening/ui/song/`
- Estado y coordinacion: `ActiveListeningUiState.kt` y `ActiveListeningViewModel.kt`
- Componentes del mapa: `ui/song/structure/`
- Casos de uso: `domain/usecase/`
- Contratos y reglas: `domain/`
- Implementaciones Android: `data/`
- Inyeccion: `di/`
- Pruebas unitarias: `app/src/test/`
- Pruebas instrumentadas: `app/src/androidTest/`

Recorridos existentes utiles:

- Importacion: `ImportSongUseCase` -> `SongImportGateway` -> `AndroidSongImportGateway` -> ViewModel -> Compose.
- Estructura: `SectionEditingUseCase` -> reglas de `domain/structure` -> ViewModel -> `ui/song/structure`.
- Exportacion: modelos y validacion de dominio -> `SongMapExportRepository` -> implementacion PDF -> ViewModel.
- Sesiones: contrato de dominio -> repositorio Room -> ViewModel -> lista Compose.
