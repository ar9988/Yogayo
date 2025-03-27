package com.d104.domain.usecase

import com.d104.domain.model.YogaPose
import com.d104.domain.repository.YogaPoseRepository
import com.neungi.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetYogaPosesUseCase @Inject constructor(
    private val yogaPoseRepository:YogaPoseRepository
) {
    suspend operator fun invoke() : Flow<Result<List<YogaPose>>> {
        return yogaPoseRepository.getYogaPoses()
    }
}