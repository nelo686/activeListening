# Active Listening

[English](./README_EN.md) | **Español**

Aplicación Android educativa para músicos, bateristas y estudiantes de música. Permite importar una canción local y acompaña al usuario durante una escucha activa para localizar transiciones, contrastar hipótesis y construir un mapa estructural editable.

La aplicación no presenta una estructura musical automática como verdad definitiva. Las propuestas locales y las sugerencias generadas con IA son puntos de partida que el usuario debe escuchar, revisar, confirmar o corregir.

## Guía de uso

Si es tu primera vez con la app, empieza por la [guía de uso](./GUIA_DE_USO.md). Explica el flujo completo sin tecnicismos: importación, reproducción, escucha guiada, edición de secciones, canciones guardadas y exportación.

## Funcionalidades actuales

- Importación de audio local mediante el selector de documentos de Android.
- Validación de formato, acceso y duración antes de cargar la canción.
- Lectura de título, artista y portada desde los metadatos del archivo cuando están disponibles.
- Reproducción con Media3 ExoPlayer: reproducir, pausar y buscar en la canción.
- Escucha guiada con una propuesta estructural inicial y pistas pedagógicas por sección.
- Guía remota opcional mediante una API compatible con OpenAI, con alternativa local si falta la clave o falla la petición.
- Protección frente a respuestas tardías: una respuesta de IA nunca se aplica a otra canción o sesión posterior.
- Mapa editable con etiquetas `Intro`, `Verso`, `Coro`, `Puente`, `Outro` y `Otra`.
- Nombres libres para secciones `Otra`, como `Pre-coro`, `Solo` o `Interludio`.
- Estados `Sugerida`, `Confirmada` y `Dudosa`.
- Línea de tiempo horizontal con cursor de reproducción y códigos de tiempo.
- Ajuste directo de fronteras: arrastrar un borde modifica conjuntamente el final del bloque izquierdo y el inicio del derecho.
- Ajuste preciso de inicio y final desde el detalle de sección.
- División en la posición actual y fusión con secciones adyacentes.
- Duración mínima protegida al mover, dividir o fusionar secciones.
- Indicador editable `Cambio de ritmo` tanto en el detalle como en el bloque de la línea de tiempo.
- Estimación orientativa de compases y regularidad según la duración de cada sección.
- Explicaciones adaptadas a los niveles introductorio, intermedio, avanzado y experto.
- Acciones guiadas para confirmar, marcar una duda, volver ocho segundos o avanzar de sección.
- Intensidad de guía normal o reducida.
- Restauración de la propuesta original después de editar.
- Guardado de sesiones, estructura original/editada, progreso y última posición de reproducción.
- Lista de canciones guardadas con título, artista, duración y estado `Analizado` o `En progreso`.
- Eliminación mediante deslizamiento, con opción inmediata de deshacer.
- Exportación del mapa estructural a PDF mediante el selector de destino de Android.

## Enfoque pedagógico

El flujo está diseñado para que el usuario participe activamente:

- escucha cambios de energía, ritmo, instrumentación o sensación;
- localiza transiciones con códigos de tiempo;
- compara la propuesta con lo que realmente oye;
- etiqueta, divide, fusiona y desplaza fronteras;
- marca decisiones como confirmadas o dudosas;
- interpreta las estimaciones y sugerencias como aproximaciones editables.

La IA no recibe ni analiza directamente el audio. Trabaja con el título, la duración y los marcadores iniciales para proponer etiquetas, preguntas y pistas de escucha. Por eso sus respuestas se presentan como ayuda pedagógica y no como análisis definitivo.

## Flujo principal

1. Importar una canción compatible o abrir una sesión guardada.
2. Reproducirla y realizar una primera escucha libre.
3. Iniciar la escucha guiada para crear y guardar el mapa inicial.
4. Explorar horizontalmente la línea de tiempo.
5. Arrastrar fronteras o abrir una sección para ajustar tiempos, etiqueta, estado y contraste rítmico.
6. Dividir, fusionar o restaurar la propuesta original cuando sea necesario.
7. Continuar más adelante desde `Canciones guardadas`.
8. Exportar el mapa trabajado a PDF.

## Línea de tiempo y edición

Cada bloque muestra su etiqueta, intervalo y duración. Los estados `Sugerida` y `Dudosa` aparecen como insignias; una sección confirmada no añade esa insignia. Cuando se marca un contraste, el bloque muestra `Cambio de ritmo`.

Las fronteras internas tienen un tirador visible y un área táctil más ancha. Durante el arrastre aparece el tiempo provisional. El cambio se persiste al soltar y mantiene contiguos ambos bloques, respetando la duración mínima.

Al tocar un bloque se abre el detalle de sección completamente desplegado. El contenido es desplazable y respeta el teclado virtual, de modo que los campos de tiempo y nombre permanecen visibles durante la edición.

## Canciones guardadas

Una canción se incorpora a la lista al iniciar la escucha guiada. Cada tarjeta limita la información a tres líneas:

1. título de la canción;
2. artista y duración;
3. estado `Analizado` o `En progreso`.

`Analizado` significa que todas las secciones registradas han sido revisadas. Si faltan secciones por revisar, se muestra `En progreso`.

Al reabrir una sesión se restaura el mapa y la última posición. Deslizar una tarjeta hacia la izquierda descubre la acción de borrado. La eliminación borra sesión, estructura y progreso de Room, pero nunca el archivo de audio original; el mensaje inferior permite deshacerla inmediatamente.

## Persistencia y exportación

Room, actualmente en la versión de esquema 5, almacena:

- nivel educativo e intensidad de guía;
- propuesta original y versión editada del mapa;
- sesión, título, artista, tipo, duración y última posición;
- registros locales de práctica y secciones revisadas.

La canción continúa siendo un documento externo. La app conserva el permiso de lectura concedido por Android. Si el archivo se mueve, se elimina o el permiso deja de ser válido, será necesario seleccionarlo de nuevo.

El PDF incluye título, duración, secciones, intervalos, estados, estimaciones de compases, avisos de aproximación, cambios rítmicos y notas educativas. La exportación solo se habilita para una estructura temporal válida.

## Tecnologías

- Kotlin 2.4.0
- Jetpack Compose y Material 3 (Compose BOM 2026.06.00)
- MVVM con Android `ViewModel` y `StateFlow`
- Hilt 2.59.2
- Room 2.8.4 con KSP
- Media3 ExoPlayer 1.10.1
- `openai-kotlin` 4.1.0 de Aallam y Ktor/OkHttp
- Gradle 9.5.1 y Android Gradle Plugin 9.2.1

## Arquitectura

- `ui`: pantallas Compose, estado inmutable y coordinación mediante `ActiveListeningViewModel`.
- `domain`: modelos, contratos, reglas de estructura, casos de uso y validadores.
- `data`: Room, selector de documentos, ExoPlayer, guía remota y generación PDF.
- `di`: módulos Hilt para base de datos, reproducción, repositorios y cliente remoto.

La UI no conoce DAOs, ExoPlayer, `PdfDocument` ni el cliente OpenAI. Estas dependencias están encapsuladas tras contratos de dominio y repositorios.

## Requisitos

- Android Studio compatible con el stack del proyecto.
- JDK 21 para Gradle.
- Android SDK con `compileSdk 37`.
- Dispositivo o emulador con Android 10 / API 29 o superior.

El bytecode Kotlin/Java mantiene compatibilidad Java 11, aunque Gradle usa JDK 21.

## Configuración de la guía remota

La configuración predeterminada apunta al servicio compatible de DevExpert. Añade los valores privados a `local.properties`, que no debe versionarse:

```properties
devexpert.apiKey=tu_clave
devexpert.guidanceModel=mimo-v2.5
devexpert.baseUrl=https://inference.devexpert.io/v1/
```

También se aceptan `openai.apiKey`, `openai.guidanceModel` y la variable de entorno `DEVEXPERT_API_KEY`. Sin clave, la aplicación continúa funcionando con la guía local.

## Compilación y pruebas

```bash
./gradlew compileDebugKotlin
./gradlew testDebugUnitTest
./gradlew compileDebugAndroidTestKotlin
./gradlew assembleDebug
```

El APK debug se genera en `app/build/outputs/apk/debug/`. Las pruebas instrumentadas necesitan un dispositivo o emulador.

## Formatos y límites

- Formatos: MP3, WAV, M4A y AAC.
- Duración máxima: 15 minutos.
- Exportación: PDF.
- Duración mínima de una sección: 5 segundos.
- Las estimaciones rítmicas son orientativas.

## Limitaciones actuales

- La IA no escucha ni analiza el audio.
- No se puede renombrar manualmente una sesión guardada desde la lista.
- No hay forma de onda ni detección acústica automática de transiciones.
- No se exporta todavía a PNG, JSON, CSV, MusicXML o MIDI.
