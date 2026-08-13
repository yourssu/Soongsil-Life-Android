package com.yourssu.soongsil.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourssu.data.scholarship.ScholarshipHistory
import com.yourssu.data.scholarship.TuitionHistory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tuitionScholarshipDataStore by
    preferencesDataStore(name = "tuition_scholarship_cache")

@Singleton
class TuitionScholarshipCache @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val tuitionHistoriesKey = stringPreferencesKey("tuition_histories")
    private val scholarshipHistoriesKey = stringPreferencesKey("scholarship_histories")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getTuitionHistories(): List<TuitionHistory>? =
        readCachedData(tuitionHistoriesKey) { json.decodeFromString(it) }

    suspend fun getScholarshipHistories(): List<ScholarshipHistory>? =
        readCachedData(scholarshipHistoriesKey) { json.decodeFromString(it) }

    suspend fun saveTuitionHistories(histories: List<TuitionHistory>): Result<Unit> =
        saveCachedData(tuitionHistoriesKey) { json.encodeToString(histories) }

    suspend fun saveScholarshipHistories(histories: List<ScholarshipHistory>): Result<Unit> =
        saveCachedData(scholarshipHistoriesKey) { json.encodeToString(histories) }

    suspend fun clearCachedData(): Result<Unit> = runCatching {
        context.tuitionScholarshipDataStore.edit { it.clear() }
    }

    private suspend fun <T> readCachedData(
        key: Preferences.Key<String>,
        decode: (String) -> T
    ): T? {
        val encodedData = runCatching {
            context.tuitionScholarshipDataStore.data.first()[key]
        }.getOrNull() ?: return null

        return runCatching { decode(encodedData) }.getOrNull()
    }

    private suspend fun saveCachedData(
        key: Preferences.Key<String>,
        encode: () -> String
    ): Result<Unit> = runCatching {
        context.tuitionScholarshipDataStore.edit { preferences ->
            preferences[key] = encode()
        }
    }
}
