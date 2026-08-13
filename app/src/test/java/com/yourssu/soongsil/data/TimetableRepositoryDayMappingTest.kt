package com.yourssu.soongsil.data

import com.yourssu.data.timetable.TimetableDayOfWeek
import io.github.chlwhdtn03.data.Lms.DayOfWeek as LmsDayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimetableRepositoryDayMappingTest {
    @Test
    fun saturday_isPreservedFromLmsData() {
        assertEquals(
            TimetableDayOfWeek.SATURDAY,
            LmsDayOfWeek.SATURDAY.toTimetableDayOfWeek()
        )
    }

    @Test
    fun sunday_isStillIgnored() {
        assertNull(LmsDayOfWeek.SUNDAY.toTimetableDayOfWeek())
    }
}
