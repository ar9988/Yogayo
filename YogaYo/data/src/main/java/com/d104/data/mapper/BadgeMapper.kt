package com.d104.data.mapper

import com.d104.data.remote.dto.BadgeDetailResponseDto
import com.d104.data.remote.dto.BadgeResponseDto
import com.d104.domain.model.Badge
import com.d104.domain.model.BadgeDetail
import javax.inject.Inject

class BadgeMapper @Inject constructor(){
    fun mapToDomain(badgeResponseDto: BadgeResponseDto): Badge {
        return Badge(
            badgeId = badgeResponseDto.badgeId,
            badgeName = badgeResponseDto.badgeName,
            badgeProgress = badgeResponseDto.badgeProgress,
            highLevel = badgeResponseDto.highLevel,
            badgeDetails = badgeResponseDto.badgeDetails.map { mapBadgeDetailToDomain(it) }
        )
    }

    private fun mapBadgeDetailToDomain(badgeDetailResponseDto: BadgeDetailResponseDto): BadgeDetail {
        return BadgeDetail(
            badgeDetailId = badgeDetailResponseDto.badgeDetailId,
            badgeDetailName = badgeDetailResponseDto.badgeDetailName,
            badgeDetailImg = badgeDetailResponseDto.badgeDetailImg,
            badgeDescription = badgeDetailResponseDto.badgeDescription,
            badgeGoal = badgeDetailResponseDto.badgeGoal,
            badgeLevel = badgeDetailResponseDto.badgeLevel
        )
    }

    fun mapToDomainList(badgeResponseDtoList:List<BadgeResponseDto>) :List<Badge> {
        return badgeResponseDtoList.map { mapToDomain(it) }
    }
}