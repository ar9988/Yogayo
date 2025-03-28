package com.d104.yogaapp.features.multi.play.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.yogaapp.R

@Composable
fun GalleryPhotoCard(
    item: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .width(180.dp)
                .clickable {onClick()},
            shape = RoundedCornerShape(12.dp), // Rounded corners
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Standard card shadow
        ) {
            Column {
                // Image Section
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Image
                    contentDescription = "poseName", // Accessibility description
                    modifier = Modifier
                        .fillMaxWidth() // Make image fill the card width
                        .height(160.dp) // Adjust height as needed
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)), // Clip image to top corners
                    contentScale = ContentScale.Crop // Crop image to fit bounds
                )

                // Text Section (Row for potential future icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp), // Padding inside the row
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween, // Pushes elements apart

                    ) {
                    Text(
                        text = "poseName",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium // Slightly bolder
                        // Optional: Add maxLines = 1, overflow = TextOverflow.Ellipsis if text can be long
                    )
                }
            }
        }
    }

}