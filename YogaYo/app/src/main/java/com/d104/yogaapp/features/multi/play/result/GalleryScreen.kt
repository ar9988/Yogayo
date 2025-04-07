package com.d104.yogaapp.features.multi.play.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer // Import Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Import height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.domain.model.YogaPose
import com.d104.yogaapp.features.multi.play.components.GalleryPhotoCard

// Assuming GalleryPhotoCard exists and takes an Int item index
// import com.d104.yogaapp.features.multi.play.components.GalleryPhotoCard



@Composable
fun GalleryScreen(
    onCheckClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    poseList: List<YogaPose>,
    bestUrls:List<String>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Keep overall padding if desired, or remove if contentPadding in Grid is enough
            .padding(horizontal = 0.dp, vertical = 16.dp), // Adjusted padding, vertical applied outside grid
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f) // <-- Make the grid take up available vertical space
                .fillMaxWidth() // Ensure grid takes full width available to it
                .padding(horizontal = 24.dp), // Horizontal padding applied here now
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            // Padding inside the grid scroll area
            contentPadding = PaddingValues(vertical = 16.dp) // Only vertical padding inside grid needed now
        ) {
            itemsIndexed(bestUrls) { index, url -> // index와 url(String)을 받음
                GalleryPhotoCard(
                    url = url,
                    name = poseList[index].poseName,
                    onClick = { onItemClick(index) } // 아이템의 인덱스(Int)를 전달
                )
            }
        }

        // Optional: Add some space between the grid and the button
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCheckClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp), // Apply horizontal padding consistent with grid area
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF48FB1)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "확인",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
