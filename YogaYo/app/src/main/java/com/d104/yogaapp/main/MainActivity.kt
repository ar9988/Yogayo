package com.d104.yogaapp.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.d104.yogaapp.R
import com.d104.yogaapp.features.multi.MultiScreen
import com.d104.yogaapp.features.mypage.MyPageScreen
import com.d104.yogaapp.features.solo.SoloScreen
import com.d104.yogaapp.features.solo.play.SoloYogaPlayScreen
import com.d104.yogaapp.ui.theme.YogaYoTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YogaYoTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    MainNavigation()
                }
            }
        }
    }
}
@Composable
fun MainNavigation(viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    // 화면 전환 시 바텀바 표시 여부 설정
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    LaunchedEffect(currentDestination) {
        val shouldShowBottomBar = when (currentDestination?.route) {
            "main_tabs" -> true
            else -> false
        }
        viewModel.processIntent(MainIntent.SetBottomBarVisibility(shouldShowBottomBar))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (state.showBottomBar) {
                CustomBottomNavigation(
                    selectedTab = state.selectedTab,
                    onTabSelected = { tab ->
                        viewModel.processIntent(MainIntent.SelectTab(tab))
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main_tabs",
            modifier = Modifier.padding(paddingValues)
        ) {
            // 메인 탭 화면들
            composable("main_tabs") {
                MainTabScreen(
                    selectedTab = state.selectedTab,
                    onNavigateToYogaPlay = {
                        navController.navigate("solo_yoga_play")
                    }
                )
            }

            // 요가 플레이 화면
            composable("solo_yoga_play") {
                SoloYogaPlayScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun MainTabScreen(
    selectedTab: Tab,
    onNavigateToYogaPlay: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (selectedTab) {
            Tab.Solo -> SoloScreen(onNavigateToYogaPlay)
            Tab.Multi -> MultiScreen()
            Tab.MyPage -> MyPageScreen()
        }
    }
}

@Composable
fun CustomBottomNavigation(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    val selectedColor = Color(0xFFF2ABB3) // F2ABB3 색상
    val unselectedColor = Color.Gray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabItem(
                selected = selectedTab == Tab.Solo,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = { onTabSelected(Tab.Solo) },
                iconRes = R.drawable.ic_yoga,
                label = "Solo"
            )

            TabItem(
                selected = selectedTab == Tab.Multi,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = { onTabSelected(Tab.Multi) },
                iconRes = R.drawable.ic_group,
                label = "Multi"
            )

            TabItem(
                selected = selectedTab == Tab.MyPage,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                onClick = { onTabSelected(Tab.MyPage) },
                iconRes = R.drawable.ic_reward,
                label = "MyPage"
            )
        }
    }
}

@Composable
fun TabItem(
    selected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    iconRes: Int,
    label: String
) {
    val color = if (selected) selectedColor else unselectedColor
    val offset = if (selected) (-4).dp else 0.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .offset(y = offset)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .background(color = selectedColor, shape = CircleShape)
            )
        }
    }
}