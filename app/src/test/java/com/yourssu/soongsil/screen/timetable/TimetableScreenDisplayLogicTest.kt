package com.yourssu.soongsil.screen.timetable

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourssu.data.timetable.TimetableDayOfWeek
import com.yourssu.data.timetable.TimetableSemester
import com.yourssu.data.timetable.TimetableTerm
import java.time.DayOfWeek
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimetableScreenDisplayLogicTest {
    @Test
    fun courseTitleFontSize_returnsExpectedStepForLength() {
        assertEquals(9.5.sp, courseTitleFontSize("12345678"))
        assertEquals(8.5.sp, courseTitleFontSize("123456789"))
        assertEquals(7.5.sp, courseTitleFontSize("12345678901234"))
    }

    @Test
    fun splitClassroom_splitsBuildingAndRoomSafely() {
        assertEquals(
            ClassroomDisplay(building = "Science Hall", room = "21303"),
            splitClassroom("Science Hall 21303")
        )
        assertEquals(
            ClassroomDisplay(building = "Engineering Hall", room = "3207"),
            splitClassroom("Engineering Hall 3207")
        )
        assertEquals(
            ClassroomDisplay(building = "Library"),
            splitClassroom("Library")
        )
        assertEquals(
            ClassroomDisplay(),
            splitClassroom(null)
        )
        assertEquals(
            ClassroomDisplay(),
            splitClassroom(" ")
        )
    }

    @Test
    fun isCurrentTimetableTerm_returnsExpectedValues() {
        val currentTerm = TimetableTerm(
            year = "2026",
            semester = TimetableSemester.SECOND,
            sourceName = "2026 second semester",
            startAt = "2026-07-01T00:00:00Z",
            endAt = "2026-12-31T23:59:59Z"
        )
        val pastTerm = currentTerm.copy(
            startAt = "2026-01-01T00:00:00Z",
            endAt = "2026-06-30T23:59:59Z"
        )
        val invalidTerm = currentTerm.copy(
            startAt = "invalid",
            endAt = "invalid"
        )

        assertTrue(isCurrentTimetableTerm(currentTerm, Instant.parse("2026-08-03T02:20:00Z")))
        assertFalse(isCurrentTimetableTerm(pastTerm, Instant.parse("2026-08-03T02:20:00Z")))
        assertFalse(isCurrentTimetableTerm(invalidTerm, Instant.parse("2026-08-03T02:20:00Z")))
    }

    @Test
    fun buildCourseCardContentSpec_adjustsLinesByCardHeight() {
        assertEquals(
            TimetableCourseCardContentSpec(titleMaxLines = 2, locationLineCount = 2, isCompact = true),
            buildCourseCardContentSpec(48.dp)
        )
        assertEquals(
            TimetableCourseCardContentSpec(titleMaxLines = 2, locationLineCount = 1, isCompact = false),
            buildCourseCardContentSpec(60.dp)
        )
        assertEquals(
            TimetableCourseCardContentSpec(titleMaxLines = 2, locationLineCount = 2, isCompact = false),
            buildCourseCardContentSpec(90.dp)
        )
    }

    @Test
    fun buildCourseCardLocationLines_respectsLocationLineCount() {
        val classroom = ClassroomDisplay(building = "정보과학관", room = "21303")

        assertEquals(
            listOf("정보과학관"),
            buildCourseCardLocationLines(classroom, buildCourseCardContentSpec(60.dp))
        )
        assertEquals(
            listOf("정보과학관", "21303"),
            buildCourseCardLocationLines(classroom, buildCourseCardContentSpec(90.dp))
        )
    }

    @Test
    fun saturday_mapsToTimetableDay() {
        assertEquals(
            TimetableDayOfWeek.SATURDAY,
            DayOfWeek.SATURDAY.toTimetableDayOfWeekOrNull()
        )
    }
}
