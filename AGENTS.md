# AGENTS.md

## Proyecto

Aplicacion Android educativa para musicos, bateristas y estudiantes de musica. Permite importar una cancion desde un fichero local y guia al usuario en una escucha activa paso a paso.

El objetivo principal no es generar automaticamente una estructura musical cerrada, sino ayudar al usuario a aprender a identificar secciones como intro, verso, coro/estribillo, puente, outro u otras partes. La app debe facilitar el reconocimiento de cambios de ritmo, la localizacion de transiciones mediante codigos de tiempo y la construccion visual de un mapa estructural editable de la cancion.

La experiencia debe proponer pistas, preguntas, explicaciones y una linea de tiempo editable para acompanar al usuario desde un nivel introductorio hasta un nivel experto.

## Tecnologias

- Lenguaje: Kotlin.
- UI: Jetpack Compose.
- Sistema de diseno: Material 3.
- Arquitectura UI/datos: MVVM con ViewModels de Android Architecture Components.
- Inyeccion de dependencias: Hilt.
- Persistencia: Room.
- Procesamiento con IA: API de OpenAI.
- Cliente OpenAI: libreria comunitaria `openai-kotlin` de Aallam.

## Arquitectura

- Mantener una arquitectura sencilla y legible.
- La UI debe vivir en Compose y no debe conocer detalles de librerias de persistencia, red o IA.
- La comunicacion entre UI y datos debe hacerse mediante ViewModels.
- La capa de datos debe exponerse mediante repositorios.
- Los repositorios deben ocultar las librerias concretas utilizadas, como Room o el cliente de OpenAI.
- Evitar acoplar pantallas Compose directamente a DAOs, clientes HTTP, servicios de OpenAI o clases de infraestructura.
- Preferir modelos de dominio o UI state explicitos cuando ayuden a que las pantallas sean claras y testeables.

## Compose y Material 3

- Usar componentes de Material 3 siempre que encajen.
- Mantener las pantallas como funciones Compose pequenas y componibles.
- Separar el estado de la UI de los eventos del usuario.
- Evitar logica de negocio dentro de composables.
- Los composables deben recibir estado inmutable y callbacks.
- Respetar accesibilidad basica: textos descriptivos, labels cuando correspondan y tamanos tactiles adecuados.
- Para la linea de tiempo editable, priorizar interacciones claras, visibles y reversibles.

## ViewModels

- Los ViewModels coordinan estado, eventos y llamadas a repositorios.
- Exponer estado observable de forma estable, preferiblemente con `StateFlow` cuando el proyecto ya siga ese patron.
- No guardar referencias a `Context`, vistas ni objetos de UI en ViewModels.
- Las operaciones largas deben ejecutarse fuera del hilo principal.
- Modelar errores de forma visible para la UI, sin tragarlos silenciosamente.

## Datos, Room y KSP

- Usar Room para persistir resultados, canciones importadas, secciones, marcas temporales, progreso de aprendizaje y mapas estructurales cuando aplique.
- Usar KSP para dependencias que generan codigo, incluyendo el compiler de Room.
- No usar KAPT para Room ni para nuevas dependencias que soporten KSP.
- Mantener entidades Room separadas de modelos de UI cuando la diferencia aporte claridad.
- Las migraciones de base de datos deben ser explicitas cuando cambie el esquema.
- Los DAOs no deben filtrarse a la capa de UI.

## Hilt

- Usar Hilt para inyectar repositorios, DAOs, clientes y servicios.
- Mantener los modulos de Hilt pequenos y orientados a responsabilidades concretas.
- Evitar singletons globales manuales si Hilt puede gestionar el ciclo de vida.
- No inicializar clientes pesados directamente desde composables o ViewModels.

## OpenAI

- Usar la libreria `openai-kotlin` de Aallam para conectarse con la API de OpenAI.
- Encapsular cualquier uso de OpenAI detras de repositorios o servicios internos.
- No exponer prompts, claves, clientes ni detalles de transporte a la UI.
- No guardar claves de API en el repositorio.
- Tratar las respuestas de IA como ayudas pedagogicas, no como verdad definitiva.
- Disenar prompts y flujos para guiar al usuario con preguntas, pistas y explicaciones, no para reemplazar su escucha activa.

## Producto y pedagogia

- La app debe reforzar el aprendizaje musical progresivo.
- Evitar interfaces que den al usuario una solucion cerrada sin participacion.
- Favorecer flujos donde el usuario escuche, compare, marque, ajuste y reflexione.
- Las sugerencias automaticas deben poder editarse, confirmarse o descartarse.
- Las secciones musicales deben poder tener nombres flexibles, no solo una lista fija.
- Los codigos de tiempo y transiciones deben ser faciles de revisar y corregir.

## Gradle y dependencias

- Los ficheros `build.gradle.kts`, `settings.gradle.kts` y `libs.versions.toml` actuales deben considerarse validos.
- No modificar archivos Gradle solo porque parezcan mejorables.
- Si es imprescindible modificar Gradle o el catalogo de versiones, hacerlo con el cambio minimo necesario y explicar el motivo.
- Para compiladores o generadores de codigo, preferir KSP sobre KAPT cuando la libreria lo soporte.

## Verificacion

- Siempre que se termine de generar o modificar codigo Kotlin, compilar para detectar problemas.
- Usar:

```bash
./gradlew compileDebugKotlin
```

- Si el cambio afecta recursos, configuracion de Android, Room, Hilt o Compose de forma amplia, considerar tambien una verificacion Gradle mas amplia si el proyecto la tiene disponible.
- Si no se puede compilar por un bloqueo externo, dejar claro el comando intentado y el error principal.

## Estilo de cambios

- Hacer cambios pequenos, coherentes con la arquitectura existente.
- No introducir abstracciones nuevas salvo que reduzcan complejidad real o encajen con un patron ya presente.
- No mezclar refactors no solicitados con cambios funcionales.
- Mantener nombres claros en espanol o ingles segun el patron dominante del codigo existente.
- No modificar archivos ajenos al objetivo de la tarea.
- Antes de tocar areas con cambios existentes, revisar el contexto para no sobrescribir trabajo de otra persona.
