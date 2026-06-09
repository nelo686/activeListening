package com.mrmustard.activelistening.data.guidance

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import javax.inject.Inject

class OpenAiGuidedListeningRepository @Inject constructor(
    private val openAI: OpenAI,
    private val config: OpenAiGuidanceConfig,
) : GuidedListeningRepository {

    override suspend fun createGuidedListeningPlan(
        request: GuidedListeningRequest,
    ): GuidedListeningResult {
        if (!config.isConfigured) return GuidedListeningResult.MissingApiKey

        return runCatching {
            val completion = openAI.chatCompletion(
                ChatCompletionRequest(
                    model = ModelId(config.model),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = SYSTEM_PROMPT,
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = request.toPrompt(),
                        ),
                    ),
                ),
            )
            val content = completion.choices.firstOrNull()?.message?.content.orEmpty()
            val markers = GuidedListeningPlanParser.parse(content)
            if (markers.isEmpty()) {
                GuidedListeningResult.UnableToGenerate
            } else {
                GuidedListeningResult.Success(markers)
            }
        }.getOrElse {
            GuidedListeningResult.UnableToGenerate
        }
    }

    private fun GuidedListeningRequest.toPrompt(): String {
        val sectionLines = markers.joinToString(separator = "\n") { marker ->
            "${marker.id}|${formatTime(marker.positionMillis)}|${marker.title}|${marker.prompt}"
        }

        return """
            Cancion: $songTitle
            Duracion: ${formatTime(durationMillis)}

            Secciones aproximadas propuestas por la app:
            $sectionLines

            Devuelve exactamente una linea por seccion con este formato:
            id|etiqueta musical breve|pregunta o pista pedagogica|contraste de ritmo o sensacion, o "sin contraste"

            En el ultimo campo, menciona solo contrastes orientativos de ritmo o sensacion musical.
            Diferencialos de cambios de instrumentacion, energia o melodia.
            Si no hay base para sugerir contraste, escribe "sin contraste".
        """.trimIndent()
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0L) / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "%d:%02d".format(minutes, seconds)
    }

    private companion object {
        val SYSTEM_PROMPT = """
            Eres un profesor de escucha activa para musicos y bateristas.
            Tu tarea es guiar al estudiante mientras escucha una cancion importada.
            No inventes certezas sobre la cancion ni afirmes que existe una seccion real si no has escuchado el audio.
            Trata cada hito como un posible cambio musical a investigar.
            Da preguntas, pistas, consejos y definiciones breves que fomenten que el usuario escuche, compare y decida.
            Usa espanol claro, tono pedagogico y frases cortas.
            No des la estructura cerrada de la cancion; ayuda a construirla.
            Respeta exactamente los ids recibidos y devuelve solo el formato pedido, sin introduccion ni Markdown.
        """.trimIndent()
    }
}
