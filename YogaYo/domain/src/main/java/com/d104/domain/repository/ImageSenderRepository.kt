package com.d104.domain.repository

interface ImageSenderRepository {
    /**
     * 이미지를 특정 피어에게 전송합니다.
     * @param peerId 대상 피어의 ID
     * @param imageBytes 전송할 이미지 데이터
     * @param quality 압축 품질 (0-100)
     * @return 전송 시작 결과 (성공/실패) - 실제 완료는 비동기적으로 처리됨
     */
    suspend fun sendImageToPeer(peerId: String, imageBytes: ByteArray, quality: Int): Result<Unit>

    /**
     * 이미지를 연결된 모든 피어에게 브로드캐스트합니다.
     * @param imageBytes 전송할 이미지 데이터
     * @param quality 압축 품질 (0-100)
     * @return 전송 시작 결과 (성공/실패)
     */
    suspend fun broadcastImage(imageBytes: ByteArray, quality: Int): Result<Unit>
}