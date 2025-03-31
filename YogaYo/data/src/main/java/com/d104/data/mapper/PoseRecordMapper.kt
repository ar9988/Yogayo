package com.d104.data.mapper

import com.d104.data.remote.dto.PoseRecordRequest
import com.d104.data.remote.dto.PoseRecordResponse
import com.d104.domain.model.YogaPoseRecord
import javax.inject.Inject

class PoseRecordMapper @Inject constructor() { // Hilt 주입을 위해 @Inject constructor 추가

    fun toYogaPoseRecord(response: PoseRecordResponse): YogaPoseRecord {
        return YogaPoseRecord(
            poseRecordId = response.poseRecordId,
            poseId = response.poseId,
            roomRecordId = response.roomRecordId,
            accuracy = response.accuracy,
            ranking = response.ranking,
            poseTime = response.poseTime,
            recordTime = response.recordImg,
            createdAt = response.createdAt
        )
    }

    fun toYogaPoseRecordList(responses: List<PoseRecordResponse>?): List<YogaPoseRecord> {
        return responses?.map { toYogaPoseRecord(it) } ?: emptyList()
    }

    fun toRequest(record: YogaPoseRecord): PoseRecordRequest {
        return PoseRecordRequest(
            roomRecordId = record.roomRecordId,
            accuracy = record.accuracy,
            ranking = record.ranking,
            poseTime = record.poseTime
        )
    }

    fun toRequestList(records: List<YogaPoseRecord>?): List<PoseRecordRequest> {
        return records?.map { toRequest(it) } ?: emptyList()
    }
}
