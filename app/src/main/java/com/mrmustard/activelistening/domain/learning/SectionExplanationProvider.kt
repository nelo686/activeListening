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
                summary = "La intro presenta la cancion y prepara tu oido antes de que llegue la idea principal.",
                details = "Fijate en que instrumentos entran primero, si aparece ya el pulso y si la energia se mantiene contenida o empieza fuerte.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "La intro establece el pulso, el ambiente y algunos materiales que pueden volver despues.",
                details = "Escucha si anticipa el riff, la armonia, la textura o el patron ritmico que sostendra el verso o el estribillo.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "La intro cumple una funcion de planteamiento: fija tempo, tonalidad, textura y expectativa formal.",
                details = "Puede presentar un motivo, preparar una entrada vocal, crear tension o retrasar la seccion estable para que el cambio sea perceptible.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "La intro articula el marco formal y timbrico antes de la exposicion funcional de la cancion.",
                details = "Analiza densidad, registro, ritmo armonico, estabilidad metrica y grado de anticipacion tematica respecto a verso, coro o material recurrente.",
            ),
        ),
        SectionLabel.Verse to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "El verso suele contar la historia y mantener una energia mas estable que el estribillo.",
                details = "Busca si la melodia cambia de frase en frase mientras el acompanamiento se repite para sostener la letra.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "El verso desarrolla contenido nuevo sobre una base reconocible y normalmente prepara el estribillo.",
                details = "Compara su energia con el coro: suele haber menos densidad, una melodia menos expansiva o una instrumentacion mas contenida.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "El verso funciona como zona narrativa y de acumulacion antes de una seccion de mayor resolucion.",
                details = "Observa repeticion armonica, fraseo vocal, variaciones de arreglo y como la bateria organiza continuidad o anticipa transiciones.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "El verso tiende a sostener informacion semantica con menor clausura formal que el coro.",
                details = "Evalua estabilidad de groove, periodicidad fraseologica, tension pre-cadencial y economia de arreglo frente a secciones de mayor pregnancia.",
            ),
        ),
        SectionLabel.Chorus to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "El coro o estribillo suele ser la parte mas recordable y repetida de la cancion.",
                details = "Pregunta si sube la energia, si aparece una frase central y si esta parte vuelve casi igual mas adelante.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "El coro concentra el gancho principal y suele sentirse como llegada o recompensa.",
                details = "Busca melodias mas abiertas, mas capas de instrumentos, cambios de registro o un patron ritmico que refuerce la sensacion de resolucion.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "El coro suele maximizar contraste, memorabilidad y funcion cadencial dentro de la forma.",
                details = "Analiza densidad, rango melodico, estabilidad armonica, repeticion textual y relacion de tension/resolucion con verso y puente.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "El coro actua como nucleo de alta saliencia formal y semantica, a menudo con mayor clausura perceptiva.",
                details = "Contrasta contour melodico, ritmo armonico, orquestacion, compresion dinamica y recurrencia motivica respecto al resto de la estructura.",
            ),
        ),
        SectionLabel.Bridge to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "El puente ofrece contraste y ayuda a conectar partes de la cancion.",
                details = "Escucha si cambia la sensacion: otra armonia, menos instrumentos, un descanso o una preparacion para volver al estribillo.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "El puente rompe la repeticion y aporta aire antes de regresar a una parte conocida.",
                details = "Puede cambiar acordes, textura, ritmo o direccion melodica para que la vuelta al coro tenga mas fuerza.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "El puente introduce contraste formal y reorienta la energia acumulada por verso y coro.",
                details = "Observa modulaciones, reducciones de arreglo, cambios de groove o frases no periodicas que preparan una reexposicion.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "El puente funciona como episodio contrastante con valor de desviacion formal y preparacion re-transicional.",
                details = "Evalua pivotes armonicos, ruptura de hipermetro, redistribucion timbrica y mecanismos de tension que revalidan la seccion de retorno.",
            ),
        ),
        SectionLabel.Outro to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "El outro es la parte final: muestra como la cancion decide terminar.",
                details = "Puede repetirse, bajar la energia, cortar de golpe o dejar que algunos instrumentos desaparezcan poco a poco.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "El outro cierra la forma y puede reutilizar material anterior con otra energia.",
                details = "Busca fade out, repeticion del coro, variacion instrumental, cadencia final o un cambio de textura que indique cierre.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "El outro resuelve o disuelve la energia formal acumulada durante la cancion.",
                details = "Analiza si hay prolongacion cadencial, reduccion progresiva, vamp, coda o transformacion del motivo principal.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "El outro estabiliza, clausura o suspende el discurso formal mediante estrategias de coda.",
                details = "Observa procesos de liquidacion motivica, cadencialidad, densidad decreciente, repeticion hipermetrica o cierre abrupto como gesto estructural.",
            ),
        ),
        SectionLabel.Other to mapOf(
            LearningLevel.Introductory to Explanation(
                summary = "Esta seccion puede tener una funcion especial que no encaja claramente con las etiquetas habituales.",
                details = "Describe con tus palabras que ocurre: solo, pausa, cambio de ritmo, parte instrumental o transicion.",
            ),
            LearningLevel.Intermediate to Explanation(
                summary = "Una seccion 'otra' permite nombrar partes no convencionales sin forzar la estructura.",
                details = "Escucha si funciona como solo, interludio, pre-coro, breakdown, enlace o variacion de una parte anterior.",
            ),
            LearningLevel.Advanced to Explanation(
                summary = "Esta etiqueta sirve para conservar ambiguedades reales y estructuras no estandar.",
                details = "Relaciona la seccion con su funcion: contraste, desarrollo, transicion, extension, interrupcion o preparacion de una reentrada.",
            ),
            LearningLevel.Expert to Explanation(
                summary = "La categoria abierta evita imponer una taxonomia formal cuando la funcion perceptiva es hibrida.",
                details = "Evalua funcion retorica, posicion formal, recurrencia, parentesco motivico y grado de independencia frente a las secciones etiquetadas.",
            ),
        ),
    )

    private const val UNCERTAIN_NOTE =
        "Esta clasificacion es orientativa: si al escuchar no encaja del todo, marcala como dudosa y ajusta la etiqueta o los tiempos."
}
