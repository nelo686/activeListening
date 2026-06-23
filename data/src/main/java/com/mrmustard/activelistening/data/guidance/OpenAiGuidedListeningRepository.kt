package com.mrmustard.activelistening.data.guidance

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRequest
import com.mrmustard.activelistening.domain.guidance.GuidedListeningRepository
import com.mrmustard.activelistening.domain.guidance.GuidedListeningResult
import com.mrmustard.activelistening.domain.time.formatTimeCode
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

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
        }.getOrElse { error ->
            if (error is CancellationException) throw error
            GuidedListeningResult.UnableToGenerate
        }
    }

    private fun GuidedListeningRequest.toPrompt(): String {
        val sectionLines = markers.joinToString(separator = "\n") { marker ->
            "${marker.id}|${formatTimeCode(marker.positionMillis)}|${marker.title}|${marker.prompt}"
        }

        return """
            Canción: $songTitle
            Duración: ${formatTimeCode(durationMillis)}

            Secciones aproximadas propuestas por la app:
            $sectionLines

            Devuelve exactamente una línea por sección con este formato:
            id|etiqueta musical breve|pregunta o pista pedagógica|contraste de ritmo o sensación, o "sin contraste"

            En el último campo, menciona solo contrastes orientativos de ritmo o sensación musical.
            Diferéncialos de cambios de instrumentación, energía o melodía.
            Si no hay base para sugerir contraste, escribe "sin contraste".
        """.trimIndent()
    }

    private companion object {
        val SYSTEM_PROMPT = """
            Eres un profesor de escucha activa para músicos y bateristas.
            Tu tarea es guiar al estudiante mientras escucha una canción importada.
            No inventes certezas sobre la canción ni afirmes que existe una sección real si no has escuchado el audio.
            Trata cada hito como un posible cambio musical a investigar.
            Da preguntas, pistas, consejos y definiciones breves que fomenten que el usuario escuche, compare y decida.
            Usa español claro, tono pedagógico y frases cortas.
            No des la estructura cerrada de la canción; ayuda a construirla.
            Respeta exactamente los ids recibidos y devuelve solo el formato pedido, sin introducción ni Markdown.
        """.trimIndent()
    }
}
