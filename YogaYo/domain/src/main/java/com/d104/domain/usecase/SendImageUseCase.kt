package com.d104.domain.usecase

import com.d104.domain.repository.ImageSenderRepository
import javax.inject.Inject
import kotlin.Result // 명시적 import

class SendImageUseCase @Inject constructor(
    private val imageSenderRepository: ImageSenderRepository // 인터페이스에 의존
) {
    // operator fun invoke 를 사용하여 UseCase 를 함수처럼 호출 가능
    suspend operator fun invoke(params: Params): Result<Unit> {
        return if (params.targetPeerId != null) {
            // 특정 피어에게 전송
            imageSenderRepository.sendImageToPeer(
                peerId = params.targetPeerId,
                imageBytes = params.imageBytes,
                quality = params.quality
            )
        } else {
            // 브로드캐스트
            imageSenderRepository.broadcastImage(
                imageBytes = params.imageBytes,
                quality = params.quality
            )
        }
    }

    // UseCase 파라미터를 위한 데이터 클래스
    data class Params(
        val imageBytes: ByteArray,
        val targetPeerId: String? = null, // null 이면 브로드캐스트
        val quality: Int = 85 // 기본 압축 품질 설정
    ) {
        // ByteArray 비교는 contentEquals 사용 필요
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Params

            if (!imageBytes.contentEquals(other.imageBytes)) return false
            if (targetPeerId != other.targetPeerId) return false
            if (quality != other.quality) return false

            return true
        }

        override fun hashCode(): Int {
            var result = imageBytes.contentHashCode()
            result = 31 * result + (targetPeerId?.hashCode() ?: 0)
            result = 31 * result + quality
            return result
        }
    }
}