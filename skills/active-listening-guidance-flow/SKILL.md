---
name: active-listening-guidance-flow
description: Disena y modifica el flujo de guia con IA de ActiveListening, desde modelos de dominio y prompts de openai-kotlin hasta parsing, fallback, UiState y pruebas. Usar al cambiar preguntas pedagogicas, formato de respuesta, marcadores sugeridos, configuracion OpenAI, tratamiento de errores o presentacion de la guia.
---

# Active Listening Guidance Flow

Evoluciona la guia remota sin presentar inferencias como hechos ni sustituir la escucha y decision del estudiante.

## Flujo

1. Lee `AGENTS.md`, revisa el worktree y abre el recorrido descrito en [guidance-contract.md](references/guidance-contract.md).
2. Escribe primero el contrato semantico: que recibe el modelo, que campos devuelve, que puede afirmar y como debe actuar la app ante datos parciales.
3. Mantiene los modelos y resultados en dominio. Encapsula `openai-kotlin`, prompts y transporte en data.
4. Disena el prompt para preguntar, sugerir y explicar. Declara que la IA no escucha el audio y evita lenguaje de certeza sobre secciones o contrastes.
5. Usa un formato de salida pequeno y determinista. Si cambia, modifica prompt y parser en el mismo cambio.
6. Haz el parser tolerante a espacios, lineas sobrantes, campos invalidos y respuestas parciales, sin aceptar datos que rompan invariantes.
7. Conserva un fallback local y errores visibles para clave ausente, respuesta vacia, formato invalido y fallo de red.
8. Fusiona sugerencias por identificadores estables. No reemplaces silenciosamente ediciones del usuario ni inventes secciones no solicitadas.
9. Anade pruebas del parser con respuesta valida, parcial, malformada y texto adicional. Prueba el caso de uso para mezcla, fallback y preservacion de marcadores.
10. Si cambia el ViewModel, prueba carga, exito y error. Si cambia UI, mantiene las sugerencias editables y claramente orientativas.
11. Ejecuta `./gradlew compileDebugKotlin` y `./gradlew testDebugUnitTest`.

## Criterios pedagogicos

- Invita a escuchar cambios de ritmo, energia, instrumentacion y sensacion por separado.
- Adapta detalle y vocabulario al nivel de aprendizaje cuando el flujo disponga de ese dato.
- Prefiere preguntas observables: que cambia, donde ocurre, si se repite y que evidencia oye el usuario.
- Permite confirmar, corregir o descartar cualquier sugerencia.
- No envies audio, claves ni datos privados adicionales sin un requisito explicito.

## Terminado

El flujo esta terminado cuando prompt, parser, dominio, fallback y UI comparten el mismo contrato, las respuestas imperfectas no rompen la sesion y las pruebas relevantes pasan.
