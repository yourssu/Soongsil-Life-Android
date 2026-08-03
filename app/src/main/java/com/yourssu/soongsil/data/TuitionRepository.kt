package com.yourssu.soongsil.data

import com.yourssu.data.scholarship.TuitionHistory
import io.github.chlwhdtn03.LmsApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TuitionRepository @Inject constructor(
    private val cache: TuitionScholarshipCache
) {
    suspend fun getCachedTuitionHistories(): List<TuitionHistory>? =
        cache.getTuitionHistories()

    suspend fun getTuitionHistories(): Result<List<TuitionHistory>> = runCatching {
        val histories = LmsApi.getTuitionTable().items.map { item ->
            TuitionHistory(
                year = item.year,
                semester = item.semester,
                grade = item.grade,
                registrationType = item.registrationType,
                registrationDate = item.registrationDate,
                amount = item.amount,
                reduction = item.reduction,
                paymentAmount = item.paymentAmount
            )
        }
        cache.saveTuitionHistories(histories)
        histories
    }
}
