package com.mrmustard.activelistening.domain.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeFormattingTest {
    @Test
    fun `parses valid time code`() {
        assertEquals(125_000L, parseTimeCode("2:05"))
    }

    @Test
    fun `rejects invalid seconds`() {
        assertNull(parseTimeCode("1:60"))
    }
}
