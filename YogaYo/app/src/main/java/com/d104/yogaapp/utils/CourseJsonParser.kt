package com.d104.yogaapp.utils

import android.content.Context
import android.util.Log
import com.d104.domain.model.UserCourse
import com.d104.domain.model.YogaPose
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.FileInputStream
import java.lang.reflect.Type
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

class CourseJsonParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // YogaPose 클래스를 위한 커스텀 디시리얼라이저 정의
    private class YogaPoseDeserializer : JsonDeserializer<YogaPose> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): YogaPose {
            val jsonObject = json.asJsonObject

            // JSON에서 필요한 값들 추출
            val poseId = jsonObject.get("poseId").asLong
            val poseName = jsonObject.get("poseName").asString
            val poseImg = jsonObject.get("poseImg").asString
            val poseLevel = jsonObject.get("poseLevel").asInt
            val poseAnimation = jsonObject.get("poseAnimation").asString
            val setPoseIdElement = jsonObject.get("setPoseId")
            val setPoseId = if (setPoseIdElement != null && !setPoseIdElement.isJsonNull)
                setPoseIdElement.asLong
            else
                null
            val poseVideo = jsonObject.get("poseVideo").asString

            // poseDescription을 \n 기준으로 분할하여 리스트로 변환
            val poseDescription = jsonObject.get("poseDescription").asString
            val poseDescriptions = poseDescription
                .split("\n")
                .map { it.trim() }  // 각 라인의 앞뒤 공백 제거
                .filter { it.isNotEmpty() }  // 빈 라인 제거

            // YogaPose 객체 생성하여 반환
            return YogaPose(
                poseId = poseId,
                poseName = poseName,
                poseImg = poseImg,
                poseLevel = poseLevel,
                poseDescriptions = poseDescriptions,
                poseAnimation = poseAnimation,
                setPoseId = setPoseId?:-1,
                poseVideo = poseVideo
            )
        }
    }

    // Gson 인스턴스 생성 (YogaPose 커스텀 디시리얼라이저 등록)
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(YogaPose::class.java, YogaPoseDeserializer())
        .create()

    // assets에서 JSON 파일 읽어서 UserCourse 리스트로 변환
    fun loadUserCoursesFromAssets(fileName: String): List<UserCourse> {
        try {
            // assets에서 파일 읽기
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }

            // JSON 문자열을 List<UserCourse>로 변환
            val courseListType = object : TypeToken<List<UserCourse>>() {}.type
            return gson.fromJson(jsonString, courseListType)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun printModelMetadata(modelPath: String = "all_poses_raw_best_model_old.tflite") {
        try {
            val fileDescriptor = context.assets.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelByteBuffer: ByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            val metadataExtractor = MetadataExtractor(modelByteBuffer)

            if (metadataExtractor.hasMetadata()) {
                Log.i("ModelMetadata", "모델 메타데이터 있음:")

                // --- 입력 텐서 정보 ---
                val inputTensorCount = metadataExtractor.inputTensorCount
                Log.i("ModelMetadata", "  입력 텐서 개수: $inputTensorCount")
                for (i in 0 until inputTensorCount) {
                    val inputTensor = metadataExtractor.getInputTensorMetadata(i)
                    Log.i("ModelMetadata", "    입력 텐서 [$i]:")
                    Log.i("ModelMetadata", "      이름: ${inputTensor?.name()?:"없음"}")
                    Log.i("ModelMetadata", "      설명: ${inputTensor?.description()?:"없음"}")
//                    Log.i("ModelMetadata", "      Shape: ${inputTensor.shape().contentToString()}") // 배열 형태로 출력
//                    Log.i("ModelMetadata", "      데이터 타입: ${inputTensor.dataType()}") // 예: FLOAT32, UINT8
//
//                    // 정규화 정보 (존재한다면)
//                    val normalizationParams = inputTensor.processUnitsList?.find {it-> it.optionsType == ProcessUnitOptions.NormalizationOptions }?.options as? NormalizationOptions
//                    if (normalizationParams != null) {
//                        Log.i("ModelMetadata", "      정규화 평균: ${normalizationParams.meanList}")
//                        Log.i("ModelMetadata", "      정규화 표준편차: ${normalizationParams.stdList}")
//                    }

                    // 양자화 정보 (존재한다면)
//                    val quantizationParams = inputTensor.quantizationParams()
//                    if (quantizationParams != null && quantizationParams.scale != 0.0f) {
//                        Log.i("ModelMetadata", "      양자화 Scale: ${quantizationParams.scale}")
//                        Log.i("ModelMetadata", "      양자화 ZeroPoint: ${quantizationParams.zeroPoint}")
//                    }

                    // 라벨 파일 정보 (입력에 연결된 경우 - 드물지만 가능)
//                    val associatedFiles = metadataExtractor.getAssociatedFileNames(inputTensor.name())
//                    if (associatedFiles.isNotEmpty()){
//                        Log.i("ModelMetadata", "      연관 파일: $associatedFiles")
//                    }
                }

                // --- 출력 텐서 정보 ---
                val outputTensorCount = metadataExtractor.outputTensorCount
                Log.i("ModelMetadata", "  출력 텐서 개수: $outputTensorCount")
                for (i in 0 until outputTensorCount) {
                    val outputTensor = metadataExtractor.getOutputTensorMetadata(i)
                    Log.i("ModelMetadata", "    출력 텐서 [$i]:")
                    Log.i("ModelMetadata", "      이름: ${outputTensor?.name()?:"없음"}")
                    Log.i("ModelMetadata", "      설명: ${outputTensor?.description()?:"없음"}")
//                    Log.i("ModelMetadata", "      Shape: ${outputTensor.shape().contentToString()}")
//                    Log.i("ModelMetadata", "      데이터 타입: ${outputTensor.dataType()}")

                    // 라벨 파일 정보 (출력에 연결된 경우 - 분류 모델에서 흔함)
//                    val labelFiles = outputTensor.associatedFilesList?.filter { it.type == AssociatedFileType.TENSOR_AXIS_LABELS }
//                    if (!labelFiles.isNullOrEmpty()) {
//                        labelFiles.forEach { labelFile ->
//                            Log.i("ModelMetadata", "      라벨 파일: ${labelFile.name}")
//                            // 필요시 라벨 파일 직접 로드 및 파싱
//                            // val labels = metadataExtractor.loadAssociatedFile(labelFile.name)
//                        }
//                    }

                    // 양자화 정보 (존재한다면)
//                    val quantizationParams = outputTensor.quantizationParams()
//                    if (quantizationParams != null && quantizationParams.scale != 0.0f) {
//                        Log.i("ModelMetadata", "      양자화 Scale: ${quantizationParams.scale}")
//                        Log.i("ModelMetadata", "      양자화 ZeroPoint: ${quantizationParams.zeroPoint}")
//                    }
                }

            } else {
                Log.w("ModelMetadata", "모델에 메타데이터가 없습니다.")
                // 메타데이터가 없다면 다른 방법 (Netron, Python 등) 시도
            }

            inputStream.close()

        } catch (e: Exception) {
            Log.e("ModelMetadata", "메타데이터 읽기 오류", e)
        }
    }
}