package com.yourssu.soongsil.data

import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.data.timetable.TimetableTerm
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

    @Test
    fun toTimetableTermOrNull_parsesFirstSemesterName() {
        assertEquals(
            TimetableTerm(year = "2026", semester = TimetableSemester.FIRST, sourceName = "2026년 1학기"),
            "2026년 1학기".toTimetableTermOrNull()
        )
    }

    @Test
    fun toTimetableTermOrNull_parsesSecondSemesterName() {
        assertEquals(
            TimetableTerm(year = "2025", semester = TimetableSemester.SECOND, sourceName = "2025년 2학기"),
            "2025년 2학기".toTimetableTermOrNull()
        )
    }

    @Test
    fun toTimetableTermOrNull_parsesHyphenatedFirstSemesterName() {
        assertEquals(
            TimetableTerm(year = "2022", semester = TimetableSemester.FIRST, sourceName = "2022-1학기"),
            "2022-1학기".toTimetableTermOrNull()
        )
    }

    @Test
    fun toTimetableTermOrNull_parsesSummerSeasonName() {
        assertEquals(
            TimetableTerm(year = "2024", semester = TimetableSemester.SUMMER, sourceName = "2024-하계계절제"),
            "2024-하계계절제".toTimetableTermOrNull()
        )
    }

    @Test
    fun toTimetableTermOrNull_parsesWinterSeasonName() {
        assertEquals(
            TimetableTerm(year = "2023", semester = TimetableSemester.WINTER, sourceName = "2023-동계계절제"),
            "2023-동계계절제".toTimetableTermOrNull()
        )
    }

    @Test
    fun toTimetableTermOrNull_excludesNonRegularCourse() {
        assertNull("비정규과정(2026)".toTimetableTermOrNull())
    }

    @Test
    fun toTimetableTermOrNull_excludesDefaultTerm() {
        assertNull("Default Term".toTimetableTermOrNull())
    }

    @Test
    fun toTimetableTermOrNull_excludesBlankName() {
        assertNull("".toTimetableTermOrNull())
    }

    @Test
    fun toAvailableTimetableTerms_removesDuplicateYearAndSemester() {
        val result = listOf(
            "2025년 2학기",
            "2025년 2학기",
            "2025-2학기"
        ).parseAvailableTimetableTerms()

        assertEquals(
            listOf(
                TimetableTerm(year = "2025", semester = TimetableSemester.SECOND, sourceName = "2025년 2학기")
            ),
            result
        )
    }

    @Test
    fun toAvailableTimetableTerms_sortsLatestTermsFirst() {
        val result = listOf(
            "2024-1학기",
            "2024-하계계절제",
            "2024년 2학기",
            "2024-동계계절제",
            "2023-동계계절제",
            "2026년 1학기",
            "비정규과정(2026)",
            "Default Term",
            ""
        ).parseAvailableTimetableTerms()

        assertEquals(
            listOf(
                TimetableTerm(year = "2026", semester = TimetableSemester.FIRST, sourceName = "2026년 1학기"),
                TimetableTerm(year = "2024", semester = TimetableSemester.WINTER, sourceName = "2024-동계계절제"),
                TimetableTerm(year = "2024", semester = TimetableSemester.SECOND, sourceName = "2024년 2학기"),
                TimetableTerm(year = "2024", semester = TimetableSemester.SUMMER, sourceName = "2024-하계계절제"),
                TimetableTerm(year = "2024", semester = TimetableSemester.FIRST, sourceName = "2024-1학기"),
                TimetableTerm(year = "2023", semester = TimetableSemester.WINTER, sourceName = "2023-동계계절제")
            ),
            result
        )
    }
}
