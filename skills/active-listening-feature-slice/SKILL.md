---
name: active-listening-feature-slice
description: Implementa funcionalidades verticales completas en ActiveListening atravesando dominio, repositorios, data, Hilt, ViewModel, estado Compose y pruebas. Usar cuando una peticion anada o cambie comportamiento visible y requiera coordinar varias capas Android. No usar como flujo principal para una migracion Room aislada ni para un cambio centrado en prompts o parsing de IA; usar las Skills especializadas.
---

# Active Listening Feature Slice

Implementa una funcionalidad de extremo a extremo manteniendo la arquitectura sencilla del proyecto y su enfoque de aprendizaje musical activo.

## Flujo

1. Lee `AGENTS.md`, comprueba `git status --short` y localiza una funcionalidad existente parecida con `rg`.
2. Delimita el comportamiento observable, estados de error, persistencia necesaria y criterio de aceptacion antes de editar.
3. Traza solo las capas necesarias. Consulta [architecture-map.md](references/architecture-map.md) para los puntos de extension actuales.
4. Modela primero reglas y tipos de dominio cuando exista logica que pueda probarse sin Android.
5. Expone infraestructura mediante contratos de repositorio o gateways. No filtres Room, OpenAI, ExoPlayer, `Context` ni APIs Android a Compose.
6. Conecta implementaciones mediante Hilt siguiendo los modulos existentes. No crees singletons manuales.
7. Actualiza el ViewModel con estado explicito, eventos pequenos y errores visibles. Ejecuta operaciones largas en coroutines apropiadas.
8. Mantiene los composables pequenos: estado inmutable de entrada, callbacks de salida y Material 3. Incluye estados vacio, carga y error cuando apliquen.
9. Anade pruebas en la capa mas baja que posea el comportamiento. Amplia pruebas del ViewModel si cambia la coordinacion entre capas.
10. Compila siempre con `./gradlew compileDebugKotlin`. Ejecuta `./gradlew testDebugUnitTest` cuando cambie logica o coordinacion. Usa `./gradlew assembleDebug` para cambios amplios de recursos, Hilt o Compose.

## Decisiones

- Prefiere ampliar patrones existentes antes que introducir nuevas abstracciones.
- Mantiene entidades Room separadas de UI y detalles del cliente OpenAI detras de data.
- Las sugerencias automaticas deben poder revisarse, editarse o descartarse.
- No conviertas una ayuda pedagogica en una respuesta cerrada sobre la estructura musical.
- No modifiques Gradle salvo necesidad demostrable; realiza el cambio minimo.
- Conserva cambios ajenos presentes en el worktree.

## Terminado

La funcionalidad esta terminada cuando el recorrido completo esta conectado, los errores son visibles, las pruebas relevantes pasan y la compilacion Kotlin finaliza correctamente. Resume archivos cambiados, comportamiento y comandos de verificacion.
