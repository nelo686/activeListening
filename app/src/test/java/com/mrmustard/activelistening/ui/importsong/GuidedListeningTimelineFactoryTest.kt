package com.mrmustard.activelistening.ui.importsong

import com.mrmustard.activelistening.ui.song.guide.GuidedListeningTimelineFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidedListeningTimelineFactoryTest {

    @Test
    fun `returns no markers when duration is unknown`() {
        val markers = GuidedListeningTimelineFactory.create(durationMillis = 0L)

        assertTrue(markers.isEmpty())
    }

    @Test
    fun `creates ordered markers inside duration`() {
        val durationMillis = 240_000L

        val markers = GuidedListeningTimelineFactory.create(durationMillis)

        assertEquals(5, markers.size)
        assertEquals(0L, markers.first().positionMillis)
        assertTrue(markers.zipWithNext().all { (left, right) ->
            left.positionMillis < right.positionMillis
        })
        assertTrue(markers.all { marker ->
            marker.positionMillis in 0L..durationMillis
        })
    }

    @Test
    fun `keeps short song markers unique and inside duration`() {
        val durationMillis = 8_000L

        val markers = GuidedListeningTimelineFactory.create(durationMillis)

        assertTrue(markers.isNotEmpty())
        assertEquals(markers.map { it.positionMillis }.distinct(), markers.map { it.positionMillis })
        assertTrue(markers.all { marker ->
            marker.positionMillis in 0L..durationMillis
        })
    }
}

