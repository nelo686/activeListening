# Guía de uso de Active Listening

[English user guide](./USER_GUIDE.md) | **Español**

Esta guía explica, sin tecnicismos, para qué sirve cada pantalla y qué puedes hacer durante una sesión de escucha activa.

## Qué hace la app

Active Listening te ayuda a descubrir la estructura de una canción por ti mismo. En lugar de ofrecer una respuesta cerrada, propone puntos de escucha para que puedas:

- reconocer intros, versos, coros, puentes, outros u otras partes;
- localizar cambios de ritmo, energía, instrumentación o sensación;
- marcar y revisar transiciones;
- construir, guardar y exportar un mapa estructural.

Las sugerencias son hipótesis editables. La decisión final depende siempre de tu escucha.

## Antes de empezar

Necesitas una canción local en MP3, WAV, M4A o AAC, con una duración máxima de 15 minutos. La aplicación solo conserva acceso al documento elegido: no copia ni elimina el audio original.

## Pantalla inicial

La pantalla inicial contiene:

- el botón para importar una canción;
- acceso a Ajustes;
- las canciones guardadas, si ya has iniciado sesiones anteriormente.

Cada canción guardada muestra exactamente tres líneas: título; artista y duración; y estado `Analizado` o `En progreso`.

## 1. Importar una canción

Pulsa `Importar canción` y elige un archivo desde el selector de Android. La app comprueba el formato, que el documento se pueda leer y que no supere el límite de duración.

Cuando existen metadatos, se muestran título, artista y portada. Si falta el artista, aparece `Artista desconocido`.

Importar no crea todavía una sesión guardada. La sesión se guarda cuando inicias la escucha guiada.

## 2. Primera escucha libre

Usa los controles para reproducir, pausar o buscar en la canción. Antes de editar, escucha una parte representativa y presta atención a:

- cambios de energía;
- cambios de ritmo o pulso;
- entradas o salidas de instrumentos;
- repeticiones y contrastes;
- momentos en los que parece comenzar una idea nueva.

No necesitas acertar a la primera. El objetivo es formular hipótesis.

## 3. Iniciar la escucha guiada

Pulsa `Iniciar escucha guiada`. La app crea una propuesta inicial, empieza a guardar la sesión y muestra una pista para la sección actual.

Las acciones guiadas permiten:

- `Confirmar` una sección;
- marcarla como `Dudosa`;
- volver ocho segundos;
- saltar a la siguiente sección.

La guía puede ser local o remota. Si no existe una clave configurada o la petición falla, el flujo continúa con pistas locales. La IA no escucha el audio: solo recibe título, duración y marcadores iniciales.

## 4. Explorar la línea de tiempo

El mapa estructural es horizontal. Desliza para explorar canciones que no caben en pantalla. El cursor rojo indica la posición de reproducción.

Cada bloque muestra:

- nombre de la sección;
- tiempo de inicio y final;
- duración;
- insignia `Sugerida` o `Dudosa` cuando corresponda;
- indicador `Cambio de ritmo` cuando está marcado.

Las secciones confirmadas no muestran una insignia de estado para mantener el mapa más limpio.

## 5. Arrastrar fronteras

Los bordes entre bloques tienen un tirador vertical. Arrástralo para cambiar una transición:

- el final del bloque izquierdo y el inicio del derecho se mueven juntos;
- durante el gesto aparece el nuevo código de tiempo;
- el cambio se guarda al soltar;
- la app impide crear secciones de menos de cinco segundos.

El texto bajo el mapa recuerda la interacción: `Desliza para explorar. Usa los bordes para ajustar.`

## 6. Editar una sección en detalle

Toca un bloque para abrir su panel. El panel se despliega completamente, se puede desplazar y deja visible el campo activo por encima del teclado virtual.

Desde el detalle puedes:

- cambiar la etiqueta musical;
- elegir `Otra` y escribir un nombre como `Pre-coro`, `Solo` o `Interludio`;
- recorrer los estados `Sugerida`, `Confirmada` y `Dudosa`;
- activar o desactivar `Cambio de ritmo`;
- introducir tiempos de inicio y final;
- repetir la sección desde su comienzo;
- dividir en la posición de reproducción;
- fusionar con la sección anterior o siguiente.

Los tiempos aceptan el formato `mm:ss`. Si cierras el panel después de editar, el valor pendiente se guarda sin volver a abrir el panel.

## 7. Cambio de ritmo y estimaciones

`Cambio de ritmo` indica un posible contraste en pulso, regularidad o sensación. Puede proceder de una sugerencia o marcarse manualmente. Al activarlo en el detalle también aparece en el bloque del mapa.

No significa que la detección sea segura. Comprueba si el contraste está realmente en el ritmo o la sensación y no solo en la instrumentación, energía o melodía.

La estimación de compases se calcula a partir de la duración. Es una ayuda orientativa y puede mostrarse como aproximada, de baja confianza o irregular.

## 8. Dividir, fusionar y restaurar

- `Dividir` crea dos secciones en la posición actual si ambas pueden conservar la duración mínima.
- `Fusionar anterior` o `Fusionar siguiente` elimina la frontera compartida.
- `Restaurar propuesta original` descarta las ediciones estructurales y recupera el mapa inicial.

Estas acciones son útiles, pero conviene volver a escuchar la transición después de aplicarlas.

## 9. Ajustes pedagógicos

En Ajustes puedes elegir nivel e intensidad.

Niveles:

- `Introductorio`
- `Intermedio`
- `Avanzado`
- `Experto`

La explicación de cada sección cambia según el nivel. La intensidad `Normal` muestra más contexto; `Reducida` simplifica la guía y deja más espacio para escuchar de forma autónoma.

## 10. Canciones guardadas

Al volver al inicio encontrarás las sesiones guardadas. Una tarjeta muestra:

1. título destacado;
2. `artista · duración`;
3. `Analizado` si se han revisado todas las secciones, o `En progreso` si queda alguna.

Toca la tarjeta o su flecha para continuar. Se restauran el mapa y la última posición reproducida.

Para borrar, desliza la tarjeta hacia la izquierda y pulsa la papelera. Se eliminan la sesión, el mapa y el progreso local, pero no el audio. Puedes pulsar `Deshacer` en el mensaje inferior para restaurarlos.

## 11. Exportar a PDF

Cuando exista una estructura temporal válida, pulsa `Exportar mapa PDF` y elige un destino.

El PDF contiene:

- título y duración;
- secciones e intervalos;
- estado de cada sección;
- compases estimados y avisos;
- cambios rítmicos;
- notas educativas adaptadas al nivel.

## Etiquetas y estados

### Etiquetas musicales

- `Intro`: inicio de la canción.
- `Verso`: desarrollo o parte narrativa.
- `Coro`: idea principal o más reconocible.
- `Puente`: sección de contraste o conexión.
- `Outro`: cierre.
- `Otra`: cualquier nombre que encaje mejor con tu análisis.

### Estados

- `Sugerida`: propuesta pendiente de revisión.
- `Confirmada`: la has escuchado y te encaja.
- `Dudosa`: necesita otra escucha o una decisión posterior.

## Si algo falla

- **No se puede importar:** revisa formato, duración y acceso al archivo.
- **No aparece la guía remota:** la aplicación puede continuar con la guía local.
- **Una sesión guardada no abre:** el archivo puede haberse movido o eliminado; selecciónalo de nuevo.
- **No se puede dividir:** la posición dejaría una sección con menos de cinco segundos.
- **No se puede exportar:** revisa que las secciones formen una línea temporal válida.

## Recorrido recomendado

1. Importa una canción.
2. Escúchala una vez sin editar.
3. Inicia la guía.
4. Confirma o marca dudas.
5. Arrastra una frontera y vuelve a escucharla.
6. Edita nombres o divide solo cuando sea necesario.
7. Regresa más tarde desde canciones guardadas.
8. Exporta el mapa cuando represente tu escucha.

## Idea clave

Active Listening no está hecha para darte una solución cerrada. Su valor está en escuchar, comparar, corregir y volver a probar.
