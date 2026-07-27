package com.yourssu.soongsil.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimetableTimeParserTest {
    @Test
    fun parseRange_usesPrimaryTimeFirst() {
        val result = TimetableTimeParser.parseRange(
            primary = "09:00-10:15",
            fallback = "(08:00-08:50)"
        )

        requireNotNull(result)
        assertEquals(540, result.startMinutes)
        assertEquals(615, result.endMinutes)
        assertEquals("09:00-10:15", result.displayText)
    }

    @Test
    fun parseRange_fallsBackToPeriodTime() {
        val result = TimetableTimeParser.parseRange(
            primary = "미정",
            fallback = "(13:30 ~ 14:45)"
        )

        requireNotNull(result)
        assertEquals(810, result.startMinutes)
        assertEquals(885, result.endMinutes)
        assertEquals("13:30-14:45", result.displayText)
    }

    @Test
    fun parseRange_returnsNullWhenNoTimeExists() {
        val result = TimetableTimeParser.parseRange(
            primary = "온라인",
            fallback = "추후 공지"
        )

        assertNull(result)
    }
}
