package com.d104.data.mapper

import com.d104.data.remote.dto.YogaPoseDto
import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseWithOrder
import javax.inject.Inject

class YogaPoseMapper @Inject constructor() {

    fun mapToDomain(dto: YogaPoseDto): YogaPose {
        // poseDescription을 '\n'을 기준으로 나누어 리스트로 변환
        val descriptionList = dto.poseDescription
            .split("\n")
            .filter { it.isNotBlank() } // 빈 줄 제거
            .map { it.trim() } // 앞뒤 공백 제거

        return YogaPose(
            poseId = dto.poseId,
            poseName = dto.poseName,
            poseImg = dto.poseImg,
            poseLevel = dto.poseLevel,
            poseDescriptions = descriptionList,
            poseAnimation = dto.poseAnimation,
            setPoseId = dto.setPoseId?:-1,
            poseVideo = dto.poseVideo
        )
    }

    fun mapToDomainList(dtos: List<YogaPoseDto>): List<YogaPose> {
        return dtos.map { mapToDomain(it) }
    }

    fun mapToDto(domain: YogaPose): YogaPoseDto {
        return YogaPoseDto(
            poseId = domain.poseId,
            poseName = domain.poseName,
            poseImg = domain.poseImg,
            poseLevel = domain.poseLevel,
            poseDescription = domain.poseDescriptions.joinToString("\n"),
            poseAnimation = domain.poseAnimation,
            setPoseId = domain.setPoseId,
            poseVideo = domain.poseVideo
        )
    }

    fun modelToDto(modelList:List<YogaPose>): List<YogaPoseWithOrder>{
        val list = mutableListOf<YogaPoseWithOrder>()
        modelList.forEachIndexed{ i,yogaPose ->
            list.add(YogaPoseWithOrder(
                yogaPose.poseId,
                i
            ))
        }
        return list
    }
}