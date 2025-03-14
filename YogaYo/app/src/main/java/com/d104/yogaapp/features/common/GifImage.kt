package com.d104.yogaapp.features.common

import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.d104.yogaapp.R

@Composable
fun GifImage(
    poseVideo: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
        },
        update = { imageView ->
            // 파일 이름에서 리소스 ID 가져오기
            val resId = context.resources.getIdentifier(
                poseVideo.substringBeforeLast("."), // 확장자 제거
                "drawable",
                context.packageName
            )

            Glide.with(context)
                .asGif()
                .load(if (resId != 0) resId else R.drawable.img_sample_pose) // 기본 포즈 이미지 fallback
                .into(imageView)
        },
        modifier = modifier
    )
}