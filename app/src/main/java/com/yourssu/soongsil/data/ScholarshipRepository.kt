package com.yourssu.soongsil.data

import com.yourssu.data.scholarship.ScholarshipHistory
import io.github.chlwhdtn03.LmsApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScholarshipRepository @Inject constructor() {

    suspend fun getScholarshipHistories(): Result<List<ScholarshipHistory>> = runCatching {
        LmsApi.getScholarshipHistoryTable().items.map { item ->
            ScholarshipHistory(
                year = item.year,
                semester = item.semester,
                scholarshipName = item.scholarshipName,
                paymentMethod = item.paymentMethod,
                processStatus = item.processStatus,
                note = item.note,
                dropReason = item.dropReason,
                processDate = item.processDate,
                selectedAmount = item.selectedAmount,
                actualAmount = item.actualAmount,
                redeemedAmount = item.redeemedAmount,
                replacedAmount = item.replacedAmount,
                replacedScholarshipName = item.replacedScholarshipName,
                workDepartment = item.workDepartment
            )
        }
    }
}
