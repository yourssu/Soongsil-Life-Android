package com.yourssu.soongsil.screen.timetable

import com.yourssu.data.timetable.TimetableCourse
import com.yourssu.data.timetable.TimetableDayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimetableScreenColorMappingTest {
    @Test
    fun buildTimetableCoursePaletteIndices_assignsDistinctIndicesForTenSubjects() {
        val courses = (1..10).map { index ->
            createCourse(subject = "과목$index", dayOfWeek = TimetableDayOfWeek.MONDAY)
        }

        val result = buildTimetableCoursePaletteIndices(courses)

        assertEquals(10, result.size)
        assertEquals(10, result.values.distinct().size)
        assertTrue(result.values.all { it in 0..9 })
    }

    @Test
    fun buildTimetableCoursePaletteIndices_keepsSameSubjectOnSameIndex() {
        val courses = listOf(
            createCourse(subject = "자료구조", dayOfWeek = TimetableDayOfWeek.MONDAY),
            createCourse(subject = "운영체제", dayOfWeek = TimetableDayOfWeek.TUESDAY),
            createCourse(subject = "자료구조", dayOfWeek = TimetableDayOfWeek.FRIDAY)
        )
        val shuffledCourses = courses.reversed()

        val result = buildTimetableCoursePaletteIndices(courses)
        val shuffledResult = buildTimetableCoursePaletteIndices(shuffledCourses)

        assertEquals(result["자료구조"], shuffledResult["자료구조"])
        assertEquals(result["운영체제"], shuffledResult["운영체제"])
    }

    private fun createCourse(
        subject: String,
        dayOfWeek: TimetableDayOfWeek
    ) = TimetableCourse(
        subject = subject,
        professor = "홍길동",
        classroom = "정보과학관 101호",
        dayOfWeek = dayOfWeek,
        startMinutes = 9 * 60,
        endMinutes = 10 * 60,
        periodText = "09:00-10:00"
    )
}
