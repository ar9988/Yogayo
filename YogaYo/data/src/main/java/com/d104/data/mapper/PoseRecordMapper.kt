package com.d104.data.mapper

import com.d104.data.remote.dto.BestPoseHistoryResponseDto
import com.d104.data.remote.dto.PoseRecordRequestDto
import com.d104.data.remote.dto.PoseRecordResponseDto
import com.d104.domain.model.BestPoseRecord
import com.d104.domain.model.YogaPoseRecord
import javax.inject.Inject

class PoseRecordMapper @Inject constructor() { // Hilt 주입을 위해 @Inject constructor 추가

    fun toYogaPoseRecord(response: PoseRecordResponseDto): YogaPoseRecord {
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

    fun toYogaPoseRecordList(responses: List<PoseRecordResponseDto>?): List<YogaPoseRecord> {
        return responses?.map { toYogaPoseRecord(it) } ?: emptyList()
    }

    fun toRequest(record: YogaPoseRecord): PoseRecordRequestDto {
        return PoseRecordRequestDto(
            roomId = record.roomRecordId,
            accuracy = record.accuracy,
            ranking = record.ranking,
            poseTime = record.poseTime
        )
    }

    fun toRequestList(records: List<YogaPoseRecord>?): List<PoseRecordRequestDto> {
        return records?.map { toRequest(it) } ?: emptyList()
    }

    fun toBestPoseRecord(record:BestPoseHistoryResponseDto):BestPoseRecord{
        return BestPoseRecord(
            poseId = record.poseId,
            poseName = record.poseName,
            poseImg = record.poseImg,
            bestAccuracy = record.bestAccuracy,
            bestTime = record.bestTime
        )
    }
    fun toBestPoseRecordList(records:List<BestPoseHistoryResponseDto>?):List<BestPoseRecord>{
        return records?.map { toBestPoseRecord(it) } ?: emptyList()
    }
}
