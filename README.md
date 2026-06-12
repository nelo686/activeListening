# Active Listening

Aplicación Android educativa para músicos, bateristas y estudiantes de música. Permite importar una canción local y acompaña al usuario en una escucha activa para localizar transiciones, proponer etiquetas y construir un mapa estructural editable.

La aplicación no pretende presentar una estructura musical automática como verdad definitiva. Las sugerencias, incluidas las generadas con IA, son puntos de partida que el usuario debe escuchar, revisar y corregir.

## Guia de uso

Si es tu primera vez con la app, empieza por la [guia de uso](./GUIA_DE_USO.md). Explica que hace cada pantalla, como importar una cancion, como usar la escucha guiada y como editar el mapa estructural sin perder la referencia.

## Funcionalidades actuales

- Importación de audio local mediante el selector de documentos de Android.
- Validación de formato, acceso y duración antes de cargar la canción.
- Reproducción con Media3 ExoPlayer: reproducir, pausar, avanzar y retroceder por la línea temporal.
- Sesión de escucha guiada con una estructura inicial y pistas pedagógicas por sección.
- Guía opcional generada mediante una API compatible con OpenAI, con alternativa local si no hay clave o la petición falla.
- Mapa estructural editable con etiquetas `Intro`, `Verso`, `Coro`, `Puente`, `Outro` y `Otra`.
- Ajuste manual de inicios y finales mediante códigos de tiempo.
- División de una sección en la posición actual y fusión con secciones adyacentes.
- Estimación orientativa de compases, regularidad y posibles contrastes rítmicos.
- Explicaciones adaptadas a los niveles introductorio, intermedio, avanzado y experto.
- Acciones guiadas para confirmar, marcar dudas, repetir ocho segundos o avanzar a la siguiente seccion.
- Intensidad reducida con menos texto y solo las decisiones esenciales.
- Nombres libres para secciones de tipo `Otra`, como `Pre-coro`, `Solo` o `Interludio`.
- Conservación de la propuesta original para poder restaurarla después de editar.
- Guardado automático de sesiones, estructura editada y última posición de reproducción.
- Listado de canciones guardadas para continuar una sesión posteriormente.
- Eliminación de canciones guardadas mediante deslizamiento, con opción de deshacer.
- Exportación del mapa estructural a PDF mediante el selector de destino de Android.
- Resumen local de practica por cancion: sesiones, secciones revisadas y tendencia de autonomia.

## Enfoque pedagógico

El flujo está diseñado para que el usuario participe activamente:

- escuchar cambios de energía, ritmo, instrumentación o sensación,
- localizar transiciones con códigos de tiempo,
- comparar la propuesta con lo que realmente se oye,
- reetiquetar, dividir, fusionar y desplazar secciones,
- interpretar las estimaciones y sugerencias como aproximaciones editables.

La IA no recibe ni analiza directamente el audio. Actualmente trabaja con el título, la duración y los marcadores iniciales para generar preguntas, etiquetas y pistas de escucha.

## Flujo principal

1. Importar una canción compatible o abrir una sesión guardada.
2. Reproducirla y realizar una primera escucha libre.
3. Iniciar la escucha guiada para crear el mapa inicial.
4. Seleccionar secciones para cambiar etiquetas, límites o divisiones.
5. Revisar las explicaciones y estimaciones rítmicas.
6. Volver a la propuesta original si las ediciones no resultan útiles.
7. Salir y continuar más adelante desde la lista de canciones guardadas.
8. Deslizar una canción guardada para eliminar su sesión y mapa, con opción de deshacer.
9. Exportar el mapa trabajado a PDF.

## Persistencia y exportación

Room almacena cuatro tipos de información:

- preferencias de nivel educativo e intensidad de guía,
- propuesta original y versión editada de cada mapa estructural,
- metadatos de sesiones guardadas y última posición de reproducción.
- registros locales de practica usados para elaborar el resumen de progreso.

La canción sigue siendo un documento local externo. La app conserva el permiso de lectura concedido por el selector de Android; si el archivo se mueve, se elimina o deja de estar disponible, será necesario importarlo de nuevo.

Al borrar una canción desde la lista de guardadas se eliminan de Room su sesión y su mapa estructural, pero no el archivo de audio original. El Snackbar permite deshacer inmediatamente la operación y restaurar ambos datos.

El PDF incluye el título, la duración, las secciones, sus códigos de tiempo, duración, estado, compases estimados, avisos de aproximación, contrastes rítmicos y notas educativas. La exportación solo se habilita cuando existe una estructura temporal válida.

## Tecnologías

- Kotlin 2.4
- Jetpack Compose y Material 3
- MVVM con `ViewModel` y `StateFlow`
- Hilt para inyección de dependencias
- Room con KSP para persistencia
- Media3 ExoPlayer para reproducción
- `openai-kotlin` de Aallam y Ktor/OkHttp para la guía remota
- Gradle 9.5.1 y Android Gradle Plugin 9.2.1

## Arquitectura

El código se divide en capas sencillas:

- `ui`: pantallas Compose, estado inmutable y coordinación mediante `ActiveListeningViewModel`.
- `domain`: modelos, contratos de repositorio, reglas de estructura, casos de uso y validación de exportación.
- `data`: implementaciones Android, Room, ExoPlayer, cliente de guía y generación PDF.
- `di`: módulos Hilt para base de datos, reproducción, repositorios y cliente remoto.

La UI no accede directamente a DAOs, ExoPlayer, `PdfDocument` ni al cliente OpenAI. Estas dependencias quedan encapsuladas detrás de contratos de dominio y repositorios.

## Requisitos

- Android Studio compatible con el stack actual del proyecto.
- JDK 21 para ejecutar Gradle; el repositorio incluye configuración de toolchain para el daemon.
- Android SDK con `compileSdk 37`.
- Dispositivo o emulador con Android 10 / API 29 o superior.

El código Kotlin/Java se compila con compatibilidad Java 11, aunque el proceso Gradle usa JDK 21.

## Configuración de la guía remota

La guía usa `openai-kotlin` contra un endpoint compatible con la API de OpenAI. La configuración predeterminada apunta al servicio de inferencia de DevExpert.

Define los valores privados en `local.properties`, que no debe versionarse:

```properties
devexpert.apiKey=tu_clave
devexpert.guidanceModel=mimo-v2.5
devexpert.baseUrl=https://inference.devexpert.io/v1/
```

También se aceptan estos alias para clave y modelo:

```properties
openai.apiKey=tu_clave
openai.guidanceModel=mimo-v2.5
```

La clave puede proporcionarse mediante la variable de entorno `DEVEXPERT_API_KEY`. Si no hay una clave configurada, la app sigue funcionando con la guía local.

## Ejecutar la app

Desde la raíz del proyecto:

```bash
./gradlew assembleDebug
```

El APK debug se genera dentro de `app/build/outputs/apk/debug/`. También se puede abrir el proyecto en Android Studio y ejecutarlo sobre un dispositivo o emulador.

## Verificación y pruebas

Compilación Kotlin recomendada durante el desarrollo:

```bash
./gradlew compileDebugKotlin
```

Pruebas unitarias de dominio, importación, persistencia, parser de guía y validación de exportación:

```bash
./gradlew testDebugUnitTest
```

Compilación debug completa:

```bash
./gradlew assembleDebug
```

## Formatos y límites

- Formatos admitidos: MP3, WAV, M4A y AAC.
- Duración máxima del MVP: 15 minutos.
- Formato de exportación actual: PDF.
- Los cálculos de compases y contrastes son orientativos, no un análisis musical definitivo.

## Limitaciones actuales

- No se puede renombrar una sesión guardada desde la interfaz.
- El indicador de autonomia resume comportamientos observables; no certifica aprendizaje musical.
- La exportación no ofrece todavía PNG, JSON, CSV, MusicXML ni MIDI.
