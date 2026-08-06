package com.yourssu.soongsil.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.keep.KeepCourse
import com.yourssu.data.keep.KeepData
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.PreRegistrationCourse
import io.github.chlwhdtn03.data.Lms.PreRegistrationTable
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.keepDataStore by preferencesDataStore(name = "keep_cache")

@Singleton
class KeepRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keepDataKey = stringPreferencesKey("keep_data")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCachedData(): KeepData? {
        val encodedData = context.keepDataStore.data.first()[keepDataKey] ?: return null
        return runCatching {
            json.decodeFromString<KeepData>(encodedData)
        }.getOrNull()
    }

    suspend fun refreshData(): Result<KeepData> = runCatching {
        val keepData = LmsApi.getPreRegistrationTable().toKeepData()
        context.keepDataStore.edit { preferences ->
            preferences[keepDataKey] = json.encodeToString(keepData)
        }
        keepData
    }

    suspend fun clearCachedData(): Result<Unit> = runCatching {
        context.keepDataStore.edit { it.clear() }
    }

    suspend fun loadPlan(course: KeepCourse): Result<ByteArray> = runCatching {
        course.toPreRegistrationCourse().loadPlan()
    }

    private fun PreRegistrationTable.toKeepData(): KeepData = KeepData(
        period = period,
        reservationStatus = reservationStatus,
        totalCourseCount = totalCourseCount,
        totalCredits = totalCredits,
        availableCredits = availableCredits,
        courses = items.map { it.toKeepCourse() }
    )

    private fun PreRegistrationCourse.toKeepCourse(): KeepCourse = KeepCourse(
        priority = priority,
        plan = plan,
        classification = classification,
        multiMajorClassification = multiMajorClassification,
        engineeringCertification = engineeringCertification,
        curriculumArea = curriculumArea,
        subjectCode = subjectCode,
        subjectName = subjectName,
        section = section,
        professor = professor,
        hoursCredits = hoursCredits,
        schedule = schedule,
        applicationDate = applicationDate,
        note = note,
        savedStudentCount = savedStudentCount
    )

    private fun KeepCourse.toPreRegistrationCourse(): PreRegistrationCourse =
        PreRegistrationCourse(
            priority = priority,
            plan = plan,
            classification = classification,
            multiMajorClassification = multiMajorClassification,
            engineeringCertification = engineeringCertification,
            curriculumArea = curriculumArea,
            subjectCode = subjectCode,
            subjectName = subjectName,
            section = section,
            professor = professor,
            hoursCredits = hoursCredits,
            schedule = schedule,
            applicationDate = applicationDate,
            note = note,
            savedStudentCount = savedStudentCount
        )
}
