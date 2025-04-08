package com.d104.yogaapp.features.multi.play.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // Import Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator // Import CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
// Remove PeerUser import if not used directly here
// import com.d104.domain.model.PeerUser

@Composable
fun RoundResultScreen(
    isLoading: Boolean,
    resultImage: Painter,
    contentDescription: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(8.dp),
                        spotColor = Color.Gray
                    )
                    .width(240.dp), // Keep the Surface width constraint
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Box to hold either the Image or the Loading Indicator
                    Box(
                        modifier = Modifier
                            .size(200.dp) // Maintain the size of the image area
                            .border( // Keep the border around the area
                                width = 1.dp,
                                color = Color.LightGray,
                                // Apply border shape if needed, though not strictly necessary for Box
                                // shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center // Center the content (Indicator or Image)
                    ) {
                        if (isLoading) {
                            // Show loading indicator
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp), // Adjust size as needed
                                color = MaterialTheme.colorScheme.primary, // Optional: Customize color
                                strokeWidth = 4.dp // Optional: Customize thickness
                            )
                        } else {
                            // Show the actual image
                            Image(
                                painter = resultImage,
                                contentDescription = contentDescription,
                                modifier = Modifier.fillMaxSize() // Fill the Box
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 텍스트 (Content Description as Text)
                    Text(
                        text = contentDescription, // Use the contentDescription for the text below
                        style = MaterialTheme.typography.titleMedium, // Adjust style if needed
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}