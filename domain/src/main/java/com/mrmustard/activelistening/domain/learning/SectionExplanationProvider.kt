package com.mrmustard.activelistening.domain.learning

import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus

object SectionExplanationProvider {

    fun contentFor(
        label: SectionLabel,
        level: LearningLevel,
        status: SectionStatus,
    ): SectionLearningContent {
        val explanation = explanations.getValue(label).getValue(level)
        return SectionLearningContent(
            summary = explanation.summary,
            details = explanation.details,
            uncertainNote = if (status == SectionStatus.Uncertain) UNCERTAIN_NOTE else null,
        )
    }

    private data class Explanation(
        val summary: String,
        val details: String,
    )

    private val explanations = mapOf(
        SectionLabel.Intro to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "La intro presenta la canción y prepara tu oído antes de que llegue la idea principal.",
                details = "Fíjate en qué instrumentos entran primero, si aparece ya el pulso y si la energía se mantiene contenida o empieza fuerte.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "La intro establece el pulso, el ambiente y algunos materiales que pueden volver después.",
                details = "Escucha si anticipa el riff, la armonía, la textura o el patrón rítmico que sostendrá el verso o el estribillo.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "La intro cumple una función de planteamiento: fija tempo, tonalidad, textura y expectativa formal.",
                details = "Puede presentar un motivo, preparar una entrada vocal, crear tensión o retrasar la sección estable para que el cambio sea perceptible.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "La intro articula el marco formal y tímbrico antes de la exposición funcional de la canción.",
                details = "Analiza densidad, registro, ritmo armónico, estabilidad métrica y grado de anticipación temática respecto a verso, coro o material recurrente.",
            ),
        ),
        SectionLabel.Verse to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "Podría ser verso si desarrolla la historia o una idea nueva con una energía más estable que el estribillo.",
                details = "Busca si la melodía cambia de frase en frase mientras el acompañamiento se repite para sostener la letra y preparar una parte más central.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "Podría ser verso si desarrolla contenido nuevo sobre una base reconocible y normalmente prepara el estribillo.",
                details = "Compara su energía con el coro: suele haber menos densidad, una melodía menos expansiva o una instrumentación más contenida.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "Podría ser verso si funciona como zona narrativa y de acumulación antes de una sección de mayor resolución.",
                details = "Observa repetición armónica, fraseo vocal, variaciones de arreglo y cómo la batería organiza continuidad o anticipa transiciones.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "Podría ser verso si sostiene información semántica con menor clausura formal que el coro.",
                details = "Evalúa estabilidad de groove, periodicidad fraseológica, tensión pre-cadencial y economía de arreglo frente a secciones de mayor pregnancia.",
            ),
        ),
        SectionLabel.Chorus to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "Podría ser coro o estribillo si es una parte repetitiva, memorable y central de la canción.",
                details = "Pregunta si sube la energía, si aparece una frase central y si esta parte vuelve casi igual más adelante.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "Podría ser coro si concentra el gancho principal, se repite y suele sentirse como llegada o recompensa.",
                details = "Busca melodías memorables, más capas de instrumentos, cambios de registro o un patrón rítmico que refuerce la sensación de resolución central.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "Podría ser coro si maximiza contraste, memorabilidad y función cadencial dentro de la forma.",
                details = "Analiza densidad, rango melódico, estabilidad armónica, repetición textual y relación de tensión/resolución con verso y puente.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "Podría ser coro si actúa como núcleo central de alta saliencia formal y semántica, a menudo con mayor clausura perceptiva.",
                details = "Contrasta contour melódico, ritmo armónico, orquestación, compresión dinámica y recurrencia motívica respecto al resto de la estructura.",
            ),
        ),
        SectionLabel.Bridge to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "Podría ser puente si ofrece contraste, transición o un cambio claro respecto al resto de la canción.",
                details = "Escucha si cambia la sensación: otra armonía, menos instrumentos, un descanso o una preparación para volver al estribillo.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "Podría ser puente si rompe la repetición y aporta aire antes de regresar a una parte conocida.",
                details = "Puede funcionar como transición: cambiar acordes, textura, ritmo o dirección melódica para que la vuelta al coro tenga más fuerza.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "Podría ser puente si introduce contraste formal y reorienta la energía acumulada por verso y coro.",
                details = "Observa modulaciones, reducciones de arreglo, cambios de groove o frases no periódicas que preparan una reexposición.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "Podría ser puente si funciona como episodio contrastante con valor de desviación formal y preparación re-transicional.",
                details = "Evalúa pivotes armónicos, ruptura de hipermetro, redistribución tímbrica y mecanismos de tensión que revalidan la sección de retorno.",
            ),
        ),
        SectionLabel.Outro to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "El outro es la parte final: muestra cómo la canción decide terminar.",
                details = "Puede repetirse, bajar la energía, cortar de golpe o dejar que algunos instrumentos desaparezcan poco a poco.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "El outro cierra la forma y puede reutilizar material anterior con otra energía.",
                details = "Busca fade out, repetición del coro, variación instrumental, cadencia final o un cambio de textura que indique cierre.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "El outro resuelve o disuelve la energía formal acumulada durante la canción.",
                details = "Analiza si hay prolongación cadencial, reducción progresiva, vamp, coda o transformación del motivo principal.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "El outro estabiliza, clausura o suspende el discurso formal mediante estrategias de coda.",
                details = "Observa procesos de liquidación motívica, cadencialidad, densidad decreciente, repetición hipermétrica o cierre abrupto como gesto estructural.",
            ),
        ),
        SectionLabel.Other to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "Esta sección puede tener una función especial que no encaja claramente con las etiquetas habituales.",
                details = "Describe con tus palabras qué ocurre: solo, pausa, cambio de ritmo, parte instrumental o transición.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "Una sección 'otra' permite nombrar partes no convencionales sin forzar la estructura.",
                details = "Escucha si funciona como solo, interludio, pre-coro, breakdown, enlace o variación de una parte anterior.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "Esta etiqueta sirve para conservar ambigüedades reales y estructuras no estándar.",
                details = "Relaciona la sección con su función: contraste, desarrollo, transición, extensión, interrupción o preparación de una reentrada.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "La categoría abierta evita imponer una taxonomía formal cuando la función perceptiva es híbrida.",
                details = "Evalúa función retórica, posición formal, recurrencia, parentesco motívico y grado de independencia frente a las secciones etiquetadas.",
            ),
        ),
    )

    private const val UNCERTAIN_NOTE =
        "Esta clasificación es una hipótesis de escucha, no una verdad absoluta: si no encaja del todo, márcala como dudosa y ajusta la etiqueta o los tiempos."
}
