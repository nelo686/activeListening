package com.mrmustard.activelistening.domain.learning

import com.mrmustard.activelistening.domain.structure.SectionLabel
import com.mrmustard.activelistening.domain.structure.SectionStatus
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionExplanationProviderTest {

    @Test
    fun `returns different explanations for each learning level`() {
        val summaries = LearningLevel.entries.map { level ->
            SectionExplanationProvider.contentFor(
                label = SectionLabel.Chorus,
                level = level,
                status = SectionStatus.Suggested,
            ).summary
        }

        assertNotEquals(summaries[0], summaries[1])
        assertNotEquals(summaries[1], summaries[2])
        assertNotEquals(summaries[2], summaries[3])
    }

    @Test
    fun `includes orientation note when section is uncertain`() {
        val content = SectionExplanationProvider.contentFor(
            label = SectionLabel.Verse,
            level = LearningLevel.Introductory,
            status = SectionStatus.Uncertain,
        )

        assertNotNull(content.uncertainNote)
        assertTrue(content.uncertainNote.orEmpty().contains("hipótesis de escucha"))
        assertTrue(content.uncertainNote.orEmpty().contains("no una verdad absoluta"))
    }

    @Test
    fun `omits orientation note when section is confirmed`() {
        val content = SectionExplanationProvider.contentFor(
            label = SectionLabel.Verse,
            level = LearningLevel.Introductory,
            status = SectionStatus.Confirmed,
        )

        assertNull(content.uncertainNote)
    }

    @Test
    fun `supports every section label and learning level`() {
        SectionLabel.entries.forEach { label ->
            LearningLevel.entries.forEach { level ->
                val content = SectionExplanationProvider.contentFor(
                    label = label,
                    level = level,
                    status = SectionStatus.Suggested,
                )

                assertTrue(content.summary.isNotBlank())
                assertTrue(content.details.isNotBlank())
            }
        }
    }

    @Test
    fun `verse explanation teaches narrative development and stable energy`() {
        val content = SectionExplanationProvider.contentFor(
            label = SectionLabel.Verse,
            level = LearningLevel.Introductory,
            status = SectionStatus.Suggested,
        )
        val fullText = "${content.summary} ${content.details}".lowercase()

        assertTrue(fullText.contains("historia"))
        assertTrue(fullText.contains("energía más estable"))
    }

    @Test
    fun `chorus explanation teaches repetition memorability and central role`() {
        val content = SectionExplanationProvider.contentFor(
            label = SectionLabel.Chorus,
            level = LearningLevel.Introductory,
            status = SectionStatus.Suggested,
        )
        val fullText = "${content.summary} ${content.details}".lowercase()

        assertTrue(fullText.contains("repetitiva"))
        assertTrue(fullText.contains("memorable"))
        assertTrue(fullText.contains("central"))
    }

    @Test
    fun `bridge explanation teaches contrast transition and change`() {
        val content = SectionExplanationProvider.contentFor(
            label = SectionLabel.Bridge,
            level = LearningLevel.Introductory,
            status = SectionStatus.Suggested,
        )
        val fullText = "${content.summary} ${content.details}".lowercase()

        assertTrue(fullText.contains("contraste"))
        assertTrue(fullText.contains("transición"))
        assertTrue(fullText.contains("cambio"))
    }
}
