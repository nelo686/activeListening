# Contrato de guia actual

Recorrido principal:

1. `GuidedSessionUseCase` crea marcadores locales y una `GuidedListeningRequest`.
2. `GuidedListeningRepository` define resultados de dominio.
3. `OpenAiGuidedListeningRepository` usa `openai-kotlin` y construye los prompts.
4. `GuidedListeningPlanParser` transforma texto en sugerencias.
5. El caso de uso mezcla sugerencias con secciones locales.
6. `ActiveListeningViewModel` publica carga, resultado o error.
7. `GuidedPromptPanel` y la UI estructural muestran ayudas editables.

Pruebas de referencia:

- `GuidedListeningPlanParserTest.kt`
- `GuidedSessionUseCaseTest.kt`
- `ActiveListeningViewModelTest.kt`

Invariantes:

- La IA no analiza el audio.
- Los ids recibidos deben conservarse.
- Una respuesta vacia o inutil debe producir fallback o error controlado.
- La aplicacion sigue funcionando sin clave API.
- Las sugerencias son orientativas y nunca una estructura musical definitiva.
