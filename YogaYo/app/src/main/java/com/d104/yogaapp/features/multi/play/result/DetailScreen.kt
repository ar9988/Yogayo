package com.d104.yogaapp.features.multi.play.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d104.domain.model.MultiPhoto
import com.d104.domain.model.YogaPose
import com.d104.yogaapp.features.multi.play.components.DetailPhotoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBackButtonClick: () -> Unit,
    poseList: List<YogaPose>,
    selectedPoseId : Int,
    allUrls: List<MultiPhoto>,
)
{
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(poseList[selectedPoseId].poseName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackButtonClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets(0, 0, 0, 0) // 모든 insets 제거
            )
        },
        containerColor = Color(0xFFF5F5F5) // Light gray background for the screen body
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Apply padding from Scaffold
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between cards
        ) {
            itemsIndexed(allUrls) { index,it -> // 각 아이템은 이제 String 타입의 URL
                DetailPhotoCard(
                    resultData = it,
//                    url = imageUrl // DetailPhotoCard에 URL 전달
//                    // DetailPhotoCard에 필요한 다른 정보가 있다면 추가 전달
//                    // 예: onClick = { /* 클릭 처리 */ }
                )
            }
        }
    }
}