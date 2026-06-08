# Active Listening

Aplicación Android educativa para músicos, bateristas y estudiantes de música. Permite importar una canción local y guiar al usuario en una escucha activa paso a paso para identificar secciones, detectar cambios musicales y construir un mapa estructural editable.

## Qué hace

- Importa audio desde un archivo local.
- Reproduce, pausa y permite saltar en la canción.
- Crea una sesión de escucha guiada con pistas pedagógicas.
- Propone una estructura inicial de la canción para que el usuario la revise y ajuste.
- Permite etiquetar secciones como `Intro`, `Verso`, `Coro`, `Puente`, `Outro` u otras etiquetas.
- Guarda preferencias de aprendizaje y nivel de detalle.
- Puede generar una guía asistida por IA cuando la clave está configurada.

## Enfoque pedagógico

La app no busca dar una estructura cerrada como verdad definitiva. Su objetivo es ayudar a escuchar mejor:

- detectar cambios de energía, ritmo e instrumentación,
- localizar transiciones con códigos de tiempo,
- confirmar, corregir o marcar dudas sobre secciones,
- comparar hipótesis con lo que realmente se oye.

## Tecnologías

- Kotlin
- Jetpack Compose
- Material 3
- MVVM con `ViewModel`
- Hilt
- Room
- Media3 ExoPlayer
- `openai-kotlin` de Aallam

## Requisitos

- Android Studio reciente
- JDK 11
- Android SDK compatible con `compileSdk 37`
- Un dispositivo o emulador con Android 29 o superior

## Configuración de IA

La guía asistida usa un cliente compatible con OpenAI. Si no hay clave configurada, la app sigue funcionando con una guía local.

Puedes definir estos valores en `local.properties`:

```properties
devexpert.apiKey=tu_clave
devexpert.guidanceModel=mimo-v2.5
devexpert.baseUrl=https://inference.devexpert.io/v1/
```

También se aceptan las claves alternativas:

- `openai.apiKey`
- `openai.guidanceModel`

Si no configuras una clave, la pantalla mostrará una guía local en lugar de IA.

## Ejecutar la app

Desde la raíz del proyecto:

```bash
./gradlew assembleDebug
```

O abre el proyecto en Android Studio y ejecútalo sobre un dispositivo o emulador.

## Verificación

Para compilar y validar el proyecto:

```bash
./gradlew compileDebugKotlin
```

Si quieres una compilación más completa, puedes usar:

```bash
./gradlew assembleDebug
```

## Pruebas

El proyecto incluye pruebas unitarias para partes del dominio, la importación y la guía. Puedes ejecutarlas con:

```bash
./gradlew testDebugUnitTest
```

## Estructura del proyecto

- `app/src/main/java/.../ui` - pantallas Compose, estado visual y ViewModels.
- `app/src/main/java/.../domain` - modelos y reglas de negocio.
- `app/src/main/java/.../data` - repositorios, Room, reproducción y guía IA.
- `app/src/main/java/.../di` - módulos Hilt.
- `app/src/main/res` - recursos, temas e iconos.

## Flujo principal

1. Importar una canción local.
2. Reproducir y escuchar la primera pasada.
3. Iniciar la escucha guiada para recibir pistas.
4. Ajustar las secciones detectadas.
5. Confirmar, marcar como dudosas o reetiquetar las transiciones.

## Notas

- La importación soporta archivos de audio comunes como MP3, WAV, M4A y AAC.
- El límite inicial de duración está pensado para canciones de hasta 15 minutos.
- Las preferencias del usuario se guardan localmente con Room.
