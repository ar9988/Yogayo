package com.d104.data.mapper

import com.d104.domain.model.YogaPose
import com.d104.domain.model.YogaPoseWithOrder
import javax.inject.Inject

class PoseMapper @Inject constructor()
    : Mapper<List<YogaPose>, List<YogaPoseWithOrder>> {
    override fun map(input: List<YogaPose>): List<YogaPoseWithOrder> {
        return input.mapIndexed { index, yogaPose ->
            YogaPoseWithOrder(
                poseId = yogaPose.poseId,
                userOrderIndex = index
            )
        }
    }

}