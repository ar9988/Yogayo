package com.d104.data.mapper

import com.d104.data.remote.dto.YogaHistoryResponseDto
import com.d104.data.remote.dto.YogaPoseHistoryDetailResponseDto
import com.d104.domain.model.YogaHistory
import com.d104.domain.model.YogaPoseHistoryDetail
import javax.inject.Inject

class YogaPoseHistoryDetailMapper @Inject constructor() {

    fun mapToDomain(dto: YogaPoseHistoryDetailResponseDto): YogaPoseHistoryDetail {
        return YogaPoseHistoryDetail(
            poseId = dto.poseId,
            poseName = dto.poseName,
            poseImg = dto.poseImg,
            bestAccuracy = dto.bestAccuracy,
            bestTime = dto.bestTime,
            winCount = dto.winCount,
            histories = dto.histories.map { historyDto -> // 내부 리스트도 매핑
                mapHistoryDtoToDomain(
                    historyDto = historyDto,
                    // YogaHistory에 필요한 poseId, poseName, poseImg는 상위 DTO에서 가져옴
                    poseId = dto.poseId,
                    poseName = dto.poseName,
                    poseImg = dto.poseImg
                )
            }
        )
    }

    // 내부 YogaHistory 매핑 함수 (private 또는 internal로 선언 가능)
    private fun mapHistoryDtoToDomain(
        historyDto: YogaHistoryResponseDto,
        poseId: Long,
        poseName: String,
        poseImg: String
    ): YogaHistory {
        return YogaHistory(
            // YogaHistory에 필요한 필드 채우기
            poseId = poseId, // 상위 DTO에서 전달받음
            poseName = poseName, // 상위 DTO에서 전달받음
            // roomRecordId는 DTO에 없으므로 기본값(-1) 사용 (YogaHistory 정의에 따라)
            // userId는 타입 변환 (Long -> Int), DTO에 있으므로 DTO 값 사용
            userId = historyDto.userId.toInt(), // Long을 Int로 변환 (필요 시 예외 처리 추가)
            accuracy = historyDto.accuracy,
            ranking = historyDto.ranking, // Nullable이므로 그대로 전달
            poseTime = historyDto.poseTime,
            recordImg = historyDto.recordImg?:"",
            // isSkipped는 DTO에 없으므로 기본값(false) 사용 (YogaHistory 정의에 따라)
            poseImg = poseImg, // 상위 DTO에서 전달받음
            createdAt = historyDto.createdAt // DTO는 Long, Domain은 Long? 이지만 DTO가 non-null이므로 직접 할당
        )
    }
}