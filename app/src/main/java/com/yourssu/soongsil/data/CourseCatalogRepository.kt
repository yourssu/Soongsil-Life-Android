package com.yourssu.soongsil.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.coursecatalog.CourseCatalogCategoryData
import com.yourssu.data.coursecatalog.CourseCatalogCourseData
import com.yourssu.data.coursecatalog.CourseCatalogData
import com.yourssu.data.coursecatalog.CourseCatalogFilterData
import com.yourssu.data.coursecatalog.CourseCatalogFilterOptionData
import com.yourssu.data.coursecatalog.CourseCatalogSearchOptionsData
import com.yourssu.data.coursecatalog.CourseCatalogSelectedFilterData
import com.yourssu.data.coursecatalog.CourseCatalogSemester
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.chlwhdtn03.LmsApi
import io.github.chlwhdtn03.data.Lms.CourseCatalogCategory
import io.github.chlwhdtn03.data.Lms.CourseCatalogCourse
import io.github.chlwhdtn03.data.Lms.CourseCatalogFilter
import io.github.chlwhdtn03.data.Lms.CourseCatalogQuery
import io.github.chlwhdtn03.data.Lms.CourseCatalogSearchOptions
import io.github.chlwhdtn03.data.Lms.CourseCatalogTable
import io.github.chlwhdtn03.data.Lms.Semester
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val Context.courseCatalogDataStore by preferencesDataStore(name = "course_catalog_cache")

@Singleton
class CourseCatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val courseCatalogDataKey = stringPreferencesKey("course_catalog_data_v2")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCachedData(): CourseCatalogData? {
        val encodedData = withContext(Dispatchers.IO) {
            context.courseCatalogDataStore.data.first()[courseCatalogDataKey]
        } ?: return null
        return withContext(Dispatchers.Default) {
            runCatching {
                json.decodeFromString<CourseCatalogData>(encodedData)
            }.getOrNull()
        }
    }

    suspend fun getSearchOptions(
        year: String,
        semester: CourseCatalogSemester,
        category: CourseCatalogCategoryData,
        filterKeys: List<String>
    ): Result<CourseCatalogSearchOptionsData> = suspendRunCatching {
        withContext(Dispatchers.Default) {
            LmsApi.getCourseCatalogSearchOptions(
                CourseCatalogQuery(
                    year = year,
                    semester = Semester.valueOf(semester.name),
                    category = CourseCatalogCategory.valueOf(category.name),
                    filterKeys = filterKeys
                )
            ).toCourseCatalogSearchOptionsData()
        }
    }

    suspend fun search(
        year: String,
        semester: CourseCatalogSemester,
        category: CourseCatalogCategoryData,
        filterKeys: List<String>,
        selectedFilters: List<CourseCatalogSelectedFilterData>,
        keyword: String
    ): Result<CourseCatalogData> = suspendRunCatching {
        val query = CourseCatalogQuery(
            year = year,
            semester = Semester.valueOf(semester.name),
            category = CourseCatalogCategory.valueOf(category.name),
            filterKeys = filterKeys,
            keyword = keyword
        )
        val data = withContext(Dispatchers.Default) {
            LmsApi.getCourseCatalogTable(query).toCourseCatalogData(
                category = category,
                filterKeys = filterKeys,
                selectedFilters = selectedFilters,
                keyword = keyword
            )
        }
        val encodedData = withContext(Dispatchers.Default) {
            json.encodeToString(data)
        }
        withContext(Dispatchers.IO) {
            context.courseCatalogDataStore.edit { preferences ->
                preferences[courseCatalogDataKey] = encodedData
            }
        }
        data
    }

    suspend fun loadPlan(course: CourseCatalogCourseData): Result<ByteArray> =
        suspendRunCatching {
            withContext(Dispatchers.Default) {
                course.toLmsCourse().loadPlan()
            }
        }

    private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (throwable: Throwable) {
        if (throwable is CancellationException) throw throwable
        Result.failure(throwable)
    }

    private fun CourseCatalogTable.toCourseCatalogData(
        category: CourseCatalogCategoryData,
        filterKeys: List<String>,
        selectedFilters: List<CourseCatalogSelectedFilterData>,
        keyword: String
    ): CourseCatalogData = CourseCatalogData(
        year = year,
        semester = CourseCatalogSemester.valueOf(semester.name),
        category = category,
        filterKeys = filterKeys,
        selectedFilters = selectedFilters,
        keyword = keyword,
        totalCourseCount = totalCourseCount,
        courses = items.map { it.toCourseCatalogCourseData() }
    )

    private fun CourseCatalogSearchOptions.toCourseCatalogSearchOptionsData() =
        CourseCatalogSearchOptionsData(
            year = year,
            semester = CourseCatalogSemester.valueOf(semester.name),
            category = CourseCatalogCategoryData.valueOf(category.name),
            filters = filters.map { it.toCourseCatalogFilterData() },
            acceptsKeyword = acceptsKeyword
        )

    private fun CourseCatalogFilter.toCourseCatalogFilterData() = CourseCatalogFilterData(
        index = index,
        name = name,
        selectedKey = selectedKey,
        selectedLabel = selectedLabel,
        options = options.map { option ->
            CourseCatalogFilterOptionData(key = option.key, label = option.label)
        }
    )

    private fun CourseCatalogCourse.toCourseCatalogCourseData(): CourseCatalogCourseData =
        CourseCatalogCourseData(
            plan = plan,
            primaryClassification = primaryClassification,
            multiMajorClassification = multiMajorClassification,
            engineeringCertification = engineeringCertification,
            curriculumArea = curriculumArea,
            subjectCode = subjectCode,
            subjectName = subjectName,
            registrationNotice = registrationNotice,
            courseType = courseType,
            section = section,
            professor = professor,
            department = department,
            hoursCredits = hoursCredits,
            enrollmentCapacity = enrollmentCapacity,
            remainingSeats = remainingSeats,
            schedule = schedule,
            targetStudents = targetStudents,
            year = year,
            semester = CourseCatalogSemester.valueOf(requireNotNull(semester).name)
        )

    private fun CourseCatalogCourseData.toLmsCourse(): CourseCatalogCourse = CourseCatalogCourse(
        plan = plan,
        primaryClassification = primaryClassification,
        multiMajorClassification = multiMajorClassification,
        engineeringCertification = engineeringCertification,
        curriculumArea = curriculumArea,
        subjectCode = subjectCode,
        subjectName = subjectName,
        registrationNotice = registrationNotice,
        courseType = courseType,
        section = section,
        professor = professor,
        department = department,
        hoursCredits = hoursCredits,
        enrollmentCapacity = enrollmentCapacity,
        remainingSeats = remainingSeats,
        schedule = schedule,
        targetStudents = targetStudents,
        year = year,
        semester = Semester.valueOf(semester.name)
    )
}
