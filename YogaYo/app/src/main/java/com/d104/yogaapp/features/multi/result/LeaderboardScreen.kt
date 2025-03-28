package com.d104.yogaapp.features.multi.result
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.yogaapp.R
import kotlinx.coroutines.delay

// Dummy Data Class
data class PlayerScore(
    val name: String,
    val score: Int
)

// Sample Data (Matches the image)
val leaderboardScores = listOf(
    PlayerScore("RedLaw", 58),
    PlayerScore("YogaYo", 50),
    PlayerScore("김싸피", 43),
    PlayerScore("이싸피", 36)
)

@Composable
fun LeaderboardScreen(
    scores: List<PlayerScore> = leaderboardScores,
    onBackPressed: () -> Boolean
) {
    // State to control the visibility of list items for animation
    var visibleItems by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    // Trigger the animation when the composable enters the composition
    LaunchedEffect(key1 = scores) { // Re-trigger if scores change
        visibleItems = 0 // Reset if scores change
        scores.indices.forEach { index ->
            delay(300L) // Delay between each item appearing (adjust as needed)
            visibleItems = index + 1
        }
    }
    BackHandler {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onBackPressed()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Winner Section ---
        WinnerSection(winner = scores.first()) // Assuming the first item is always the winner

        Spacer(modifier = Modifier.height(40.dp))

        // --- Rankings List ---
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp) // Spacing between rank items
        ) {
            scores.forEachIndexed { index, player ->
                // Animate each item's appearance
                AnimatedVisibility(
                    visible = index < visibleItems,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 }, // Start from halfway down
                        animationSpec = tween(durationMillis = 500)
                    ) + fadeIn(animationSpec = tween(durationMillis = 500)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300)) // Optional exit animation
                ) {
                    RankItem(rank = index + 1, player = player)
                }
            }
        }

        // Pushes the button to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // --- Next Button ---
        Button(
            onClick = { /* Handle Next button click */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF48FB1) // Pinkish color from image
            ),
            shape = MaterialTheme.shapes.medium // Or RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "다음",
                color = Color.White, // Text color looks white
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun WinnerSection(winner: PlayerScore) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.ic_crown), // Replace with your crown drawable
            contentDescription = "Winner Crown",
            modifier = Modifier.size(80.dp) // Adjust size as needed
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Person Icon"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = winner.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium // Slightly bolder
            )
        }
    }
}

@Composable
fun RankItem(rank: Int, player: PlayerScore) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Indicator (Medal or Text)
        RankIndicator(rank = rank)

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            Icons.Filled.Person,
            contentDescription = "Person Icon"
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Player Name (takes up available space)
        Text(
            text = player.name,
            modifier = Modifier.weight(1f), // Occupy remaining horizontal space
            fontSize = 16.sp
        )

        // Score
        Text(
            text = "${player.score}PT",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray // Slightly less prominent than black
        )
    }
}

@Composable
fun RankIndicator(rank: Int) {
    Box(
        modifier = Modifier.width(40.dp), // Fixed width for alignment
        contentAlignment = Alignment.Center
    ) {
        when (rank) {
            1 -> Image(
                painter = painterResource(id = R.drawable.ic_gold_medal), // Replace
                contentDescription = "1st Place",
                modifier = Modifier.size(32.dp) // Adjust medal size
            )
            2 -> Image(
                painter = painterResource(id = R.drawable.ic_silver_medal), // Replace
                contentDescription = "2nd Place",
                modifier = Modifier.size(32.dp) // Adjust medal size
            )
            3 -> Image(
                painter = painterResource(id = R.drawable.ic_bronze_medal), // Replace
                contentDescription = "3rd Place",
                modifier = Modifier.size(32.dp) // Adjust medal size
            )
            else -> Text(
                text = "${rank}th",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Gray // Color for ranks > 3
            )
        }
    }
}
