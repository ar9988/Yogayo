package com.d104.yogaapp.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import kotlin.jvm.Throws

class BestPoseModelUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var interpreter: Interpreter? = null

    // 모델의 기대 입력 크기 (확인된 정보 기반)
    private val modelPath = "all_poses_plus_fixed_best_model_plus.tflite"
//    private val modelPath = "all_poses_raw_best_model_old.tflite"
    private val keypointInputSize = 99
    private val imageInputHeight = 480
    private val imageInputWidth = 480
    private val imageInputChannels = 3
    private val outputSize = 7 // 출력 shape [1, 7] 에서 7
    private var nnApiDelegate: NnApiDelegate? = null
    private var gpuDelegate: GpuDelegate? = null
    init {
        try {
            val modelBuffer = loadModelFile(context, modelPath)
            val options = Interpreter.Options()
            // 필요시 NNAPI 또는 GPU Delegate 설정
//            nnApiDelegate = NnApiDelegate()
//            options.addDelegate(nnApiDelegate)
            gpuDelegate = GpuDelegate()
            options.addDelegate(gpuDelegate)
            interpreter = Interpreter(modelBuffer, options)
            Log.d("ModelInit", "Interpreter 초기화 성공")

            // (선택 사항) 초기화 시 실제 Shape 확인 로그
//            logInputOutputShapes()

        } catch (e: IOException) {
            Log.e("ModelInit", "모델 로드 또는 Interpreter 초기화 실패", e)
            // 사용자에게 알림 등 오류 처리
        }
    }

    // Assets에서 모델 파일 로드
    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        // 리소스 정리
        fileChannel.close()
        inputStream.close()
        fileDescriptor.close()
        return mappedByteBuffer
    }

    private fun logInputOutputShapes() {
        try {
            val inputCount = interpreter?.inputTensorCount ?: 0
            Log.d("ModelIO", "입력 텐서 개수: $inputCount")
            for(i in 0 until inputCount) {
                val tensor = interpreter?.getInputTensor(i)
                Log.d("ModelIO", "  입력 [$i]: Shape=${tensor?.shape()?.contentToString()}, Type=${tensor?.dataType()}")
            }
            val outputCount = interpreter?.outputTensorCount ?: 0
            Log.d("ModelIO", "출력 텐서 개수: $outputCount")
            for(i in 0 until outputCount) {
                val tensor = interpreter?.getOutputTensor(i)
                Log.d("ModelIO", "  출력 [$i]: Shape=${tensor?.shape()?.contentToString()}, Type=${tensor?.dataType()}")
            }
        } catch (e: Exception) {
            Log.w("ModelIO", "IO Shape 로깅 중 오류", e)
        }
    }

    private fun prepareKeypointInputBuffer(keypoints: FloatArray): ByteBuffer {
        if (keypoints.size != keypointInputSize) {
            throw IllegalArgumentException("키포인트 배열 크기는 $keypointInputSize 여야 합니다. 실제: ${keypoints.size}")
        }
        // ByteBuffer 할당: 1 * 99 * 4 bytes (float = 4 bytes)
        val byteBuffer = ByteBuffer.allocateDirect(1 * keypointInputSize * 4)
        byteBuffer.order(ByteOrder.nativeOrder()) // 기기의 네이티브 바이트 순서 사용
        byteBuffer.rewind() // 버퍼 포지션 초기화

        // FloatArray 데이터를 ByteBuffer에 넣기
        byteBuffer.asFloatBuffer().put(keypoints)

        return byteBuffer
    }

    /**
     * 입력 1: Image [1, 224, 224, 3] float32 준비
     */
//    private fun prepareImageInputBuffer(bitmap: Bitmap): ByteBuffer {
//        // 1. 비트맵 리사이즈 (모델 입력 크기에 맞게)
//        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, imageInputWidth, imageInputHeight, true)
//
//        // 2. ByteBuffer 할당: 1 * 224 * 224 * 3 * 4 bytes
//        val byteBuffer = ByteBuffer.allocateDirect(1 * imageInputHeight * imageInputWidth * imageInputChannels * 4)
//        byteBuffer.order(ByteOrder.nativeOrder())
//        byteBuffer.rewind()
//
//        // 3. Bitmap 픽셀 데이터를 ByteBuffer에 넣기 (float32, HWC 순서)
//        val intValues = IntArray(imageInputWidth * imageInputHeight)
//        resizedBitmap.getPixels(intValues, 0, imageInputWidth, 0, 0, imageInputWidth, imageInputHeight)
//
//        var pixel = 0
//        for (i in 0 until imageInputHeight) {
//            for (j in 0 until imageInputWidth) {
//                val value = intValues[pixel++]
//                // RGB 값을 추출하여 float32로 변환 후 ByteBuffer에 넣기
//                // !!! 중요 !!!: 모델 학습 시 사용된 정규화(Normalization) 방식을 동일하게 적용해야 합니다.
//                //              메타데이터가 없으므로 일반적인 방식 중 하나를 가정합니다.
//                // 예시 1: [0, 1] 범위로 스케일링
//                byteBuffer.putFloat(((value shr 16) and 0xFF) / 255.0f) // Red
//                byteBuffer.putFloat(((value shr 8) and 0xFF) / 255.0f)  // Green
//                byteBuffer.putFloat((value and 0xFF) / 255.0f)          // Blue
//
//                // 예시 2: [-1, 1] 범위로 스케일링
//                // byteBuffer.putFloat((((value shr 16) and 0xFF) / 127.5f) - 1.0f)
//                // byteBuffer.putFloat((((value shr 8) and 0xFF) / 127.5f) - 1.0f)
//                // byteBuffer.putFloat(((value and 0xFF) / 127.5f) - 1.0f)
//
//                // 예시 3: 특정 평균/표준편차 사용 (값이 있다면)
//                // val IMAGE_MEAN = 127.5f
//                // val IMAGE_STD = 127.5f
//                // byteBuffer.putFloat(((Color.red(value) - IMAGE_MEAN) / IMAGE_STD))
//                // byteBuffer.putFloat(((Color.green(value) - IMAGE_MEAN) / IMAGE_STD))
//                // byteBuffer.putFloat(((Color.blue(value) - IMAGE_MEAN) / IMAGE_STD))
//            }
//        }
//
//        // 리사이즈된 비트맵 메모리 해제 (더 이상 필요 없다면)
//        if (!resizedBitmap.isRecycled && resizedBitmap != bitmap) { // 원본과 다를 경우에만
//            resizedBitmap.recycle()
//        }
//
//        return byteBuffer
//    }

    private fun prepareImageInputBufferWithSupportLib(bitmap: Bitmap): ByteBuffer {
        val tensorImage = TensorImage.fromBitmap(bitmap)

        // ImageProcessor 설정 (리사이즈, 정규화 등)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imageInputHeight, imageInputWidth, ResizeOp.ResizeMethod.BILINEAR))
            // 모델 학습 시 사용한 정규화 방식 적용 (예: 0~1)
            .add(NormalizeOp(0.0f, 255.0f))
            // 또는 다른 정규화 방식 (예: -1~1)
            // .add(NormalizeOp(127.5f, 127.5f))
            .build()

        val processedImage = imageProcessor.process(tensorImage)
        return processedImage.buffer // 바로 ByteBuffer 얻기
    }

    fun runInference(keypoints: FloatArray, bitmap: Bitmap): Pair<FloatArray?,Long>? {
        if (interpreter == null) {
            Log.e("Inference", "Interpreter가 초기화되지 않았습니다.")
            return null
        }

        // 입력 버퍼 준비
        val keypointInputBuffer = prepareKeypointInputBuffer(keypoints)
//        val imageInputBuffer = prepareImageInputBuffer(bitmap)
        val imageInputBuffer = prepareImageInputBufferWithSupportLib(bitmap)

        // 입력 배열 생성 (인덱스 순서 중요: 0번=키포인트, 1번=이미지)
        val inputs = arrayOf<Any>(keypointInputBuffer, imageInputBuffer)

        // 출력 버퍼 준비 (Map 형태)
        // 출력 Shape [1, 7], float32
        val outputBuffer = ByteBuffer.allocateDirect(1 * outputSize * 4)
        outputBuffer.order(ByteOrder.nativeOrder())
        outputBuffer.rewind()

        val outputs = HashMap<Int, Any>()
        outputs[0] = outputBuffer // 출력 텐서 인덱스 0에 결과 버퍼 할당

        // 추론 실행 및 시간 측정
        val startTime = SystemClock.uptimeMillis()
        try {
            interpreter?.runForMultipleInputsOutputs(inputs, outputs)
        } catch (e: Exception) {
            Log.e("Inference", "추론 중 오류 발생", e)
            return null // 오류 발생 시 null 반환
        }
        val endTime = SystemClock.uptimeMillis()
        val inferenceTime = endTime-startTime
        Log.i("Inference", "추론 시간: ${inferenceTime} ms")

        // 결과 처리
        val outputArray = FloatArray(outputSize)
        // 출력 버퍼에서 FloatBuffer를 얻어 결과 배열에 복사
        outputs[0]?.let { buffer ->
            (buffer as ByteBuffer).rewind() // 읽기 전에 포지션 초기화 필수!
            buffer.asFloatBuffer().get(outputArray)
        } ?: run {
            Log.e("Inference", "출력 버퍼를 찾을 수 없습니다 (Index 0).")
            return null
        }

        return Pair(outputArray,inferenceTime)
    }

    // --- 추론 및 리소스 해제 함수 (아래에 추가) ---

    fun close() {
        interpreter?.close()
        nnApiDelegate?.close()
        gpuDelegate?.close()
        interpreter = null
        Log.d("ModelProcessor", "Interpreter closed.")
    }
}