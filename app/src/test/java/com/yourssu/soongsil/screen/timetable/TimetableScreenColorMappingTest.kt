package com.yourssu.soongsil.screen.timetable

import androidx.compose.ui.graphics.Color
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

    @Test
    fun getTimetableCoursePalette_usesDarkFigmaPalette() {
        val expectedPalettes = listOf(
            TimetableCoursePalette(Color(0xFF2563EB).copy(alpha = 0.28f), Color(0xFF93C5FD)),
            TimetableCoursePalette(Color(0xFFD97706).copy(alpha = 0.28f), Color(0xFFFDBA74)),
            TimetableCoursePalette(Color(0xFF059669).copy(alpha = 0.28f), Color(0xFF6EE7B7)),
            TimetableCoursePalette(Color(0xFF7C3AED).copy(alpha = 0.28f), Color(0xFFD8B4FE)),
            TimetableCoursePalette(Color(0xFFDC2626).copy(alpha = 0.28f), Color(0xFFFCA5A5)),
            TimetableCoursePalette(Color(0xFF0891B2).copy(alpha = 0.28f), Color(0xFF67E8F9)),
            TimetableCoursePalette(Color(0xFFCA8A04).copy(alpha = 0.28f), Color(0xFFFDE68A)),
            TimetableCoursePalette(Color(0xFF4F46E5).copy(alpha = 0.28f), Color(0xFFA5B4FC)),
            TimetableCoursePalette(Color(0xFFDB2777).copy(alpha = 0.28f), Color(0xFFF9A8D4)),
            TimetableCoursePalette(Color(0xFF475569).copy(alpha = 0.28f), Color(0xFFCBD5E1))
        )

        val actualPalettes = expectedPalettes.indices.map { paletteIndex ->
            getTimetableCoursePalette(
                paletteIndex = paletteIndex,
                isDarkTheme = true
            )
        }

        assertEquals(expectedPalettes, actualPalettes)
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
