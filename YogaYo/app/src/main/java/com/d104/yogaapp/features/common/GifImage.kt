package com.d104.yogaapp.features.common

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.d104.yogaapp.R

@Composable
fun GifImage(
    url: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val context = LocalContext.current
    var gifDrawable by remember { mutableStateOf<GifDrawable?>(null) }

    // 리소스 ID 계산
    val resId = remember(url) {
        context.resources.getIdentifier(
            url.substringBeforeLast("."), // 확장자 제거
            "drawable",
            context.packageName
        )
    }

    // 실제 리소스 ID (없으면 기본값)
    val actualResId = if (resId != 0) resId else R.drawable.img_sample_pose

    // isPlaying 상태에 따라 GIF 애니메이션 제어
    DisposableEffect(isPlaying) {
        gifDrawable?.let { drawable ->
            if (isPlaying) {
                drawable.start()
            } else {
                drawable.stop()
            }
        }

        onDispose { }
    }

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
            // GIF 로드 및 표시
            Glide.with(context)
                .asGif()
                .load(actualResId)
                .into(object : CustomTarget<GifDrawable>() {
                    override fun onResourceReady(
                        resource: GifDrawable,
                        transition: Transition<in GifDrawable>?
                    ) {
                        // GifDrawable 참조 저장
                        gifDrawable = resource

                        // isPlaying 상태에 따라 애니메이션 제어
                        if (isPlaying) {
                            resource.start()
                        } else {
                            resource.stop()
                        }

                        // ImageView에 drawable 설정
                        imageView.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        imageView.setImageDrawable(placeholder)
                        gifDrawable = null
                    }
                })
        },
        modifier = modifier
    )
}