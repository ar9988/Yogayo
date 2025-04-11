package com.d104.yogaapp.features.mypage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d104.domain.model.Badge
import com.d104.domain.model.BadgeDetail
import com.d104.domain.model.MyPageInfo
import com.d104.yogaapp.R
import com.d104.yogaapp.features.common.UserRecordCard
import com.d104.yogaapp.ui.theme.PastelLigtBlue
import com.d104.yogaapp.ui.theme.PrimaryColor
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onNavigateSoloScreen: () -> Unit,
    onNavigateToDetailRecord:(myPageInfo:MyPageInfo)->Unit
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initalize()
    }

    LaunchedEffect(uiState.isLogoutSuccessful){
        if(uiState.isLogoutSuccessful){
            onNavigateSoloScreen()
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }





    val itemsPerRow = 3
    val gridSpacing = 8.dp // 그리드 아이템 간 간격



    // --- 전체 화면을 LazyVerticalGrid로 변경 ---
    LazyVerticalGrid(
        columns = GridCells.Fixed(itemsPerRow), // 3열 고정
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp), // 그리드 전체 패딩
        horizontalArrangement = Arrangement.spacedBy(gridSpacing), // 아이템 간 수평 간격
        verticalArrangement = Arrangement.spacedBy(gridSpacing * 2) // 아이템 간 수직 간격 (수평보다 약간 넓게)
    ) {
        // --- 1. 프로필 섹션 (헤더 - 전체 너비 차지) ---
        item(span = { GridItemSpan(maxLineSpan) }) { // maxLineSpan: 현재 줄의 최대 스팬 (여기선 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                // .padding(16.dp) // contentPadding으로 대체 또는 조정
            ) {
                // 로그아웃 버튼 (우측 상단)
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 0.dp) // 위치 미세 조정
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "로그아웃",
                        modifier = Modifier.size(32.dp),
                    )
                }

                // 프로필 (중앙)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 프로필 이미지
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uiState.myPageInfo.userProfile?.ifEmpty { R.drawable.ic_profile })
                                .crossfade(true)
                                .error(R.drawable.ic_profile)
                                .build(),
                            contentDescription = "프로필 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // 사용자 이름
                    Text(
                        text = uiState.myPageInfo.userNickName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // 프로필 섹션과 다음 섹션 사이 간격
            // Spacer(modifier = Modifier.height(16.dp)) // verticalArrangement로 대체 또는 유지
        }

        // --- 2. 통계 섹션 (헤더 - 전체 너비 차지) ---
        item(span = { GridItemSpan(maxLineSpan) }) {
            UserRecordCard(
                myPageInfo = uiState.myPageInfo,
                showDetailButton = true,
                onClickDetail = {onNavigateToDetailRecord(uiState.myPageInfo)}
            )
            // 통계 카드와 다음 섹션 사이 간격
            // Spacer(modifier = Modifier.height(8.dp)) // verticalArrangement로 대체 또는 유지
        }

        // --- 3. 뱃지 섹션 타이틀 (헤더 - 전체 너비 차지) ---
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center // 중앙 정렬
            ) {
                Text(
                    text = "뱃지",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    // modifier = Modifier.padding(horizontal = 16.dp), // contentPadding으로 처리됨
                    textAlign = TextAlign.Center
                )
            }
            // 타이틀과 뱃지 그리드 사이 간격
            // Spacer(modifier = Modifier.height(12.dp)) // verticalArrangement로 대체 또는 유지
        }

        // --- 4. 뱃지 그리드 아이템들 ---
        // LazyVerticalGrid의 items 사용 (NonScrollableGrid 제거)
        items(uiState.myBadgeList, key = { it.badgeId ?: it.hashCode() }) { badge -> // 안정적인 고유 ID 사용 권장
            BadgeItem(badge = badge) // BadgeItem 직접 사용
        }
    }
    if (uiState.showBadgeOverlay && uiState.newBadgeList.isNotEmpty()) {
        val currentBadge = uiState.newBadgeList[uiState.currentNewBadgeIndex]
        // 현재 뱃지의 highLevel에 해당하는 BadgeDetail 찾기
        val highLevelDetail = currentBadge.badgeDetails.find { it.badgeLevel == currentBadge.highLevel }

        highLevelDetail?.let {
            BadgeOverlay(
                badge = currentBadge,
                highLevelBadgeDetail = it,
                onDismiss = { viewModel.showNextBadge() }
            )
        }
    }
}


@Composable
fun BadgeItem(badge: Badge) {

    var showDialog by remember { mutableStateOf(false) }

    // 다이얼로그가 표시될 때 보여줄 컴포넌트
    if (showDialog) {
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { showDialog = false }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            showDialog=true
        }
        // .padding(4.dp) // Grid의 spacing으로 간격 처리, 필요시 내부 패딩 추가
        // .height(140.dp) // 높이 고정보다는 내용에 맞게 조절되도록 하는 것이 Grid에 더 적합할 수 있음
        // 고정 높이가 필요하다면 유지
    ) {
        // 뱃지 이미지
        Box(
            modifier = Modifier.size(80.dp), // 크기 유지 또는 조정
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if(badge.highLevel==0) R.drawable.ic_badge_lv0 else badge.badgeDetails[badge.highLevel-1].badgeDetailImg)
                    .crossfade(true)
                    .error(R.drawable.ic_badge_lv0)
                    .build(),
                contentDescription = "프로필 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 뱃지 이름
        Text(
            text = badge.badgeName,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 다음 레벨 정보 및 진행 상태 표시 (이전과 동일)
        val nextLevelIndex = badge.highLevel
        val nextLevel = if (nextLevelIndex < badge.badgeDetails.size) {
            badge.badgeDetails[nextLevelIndex]
        } else null

        nextLevel?.let { level ->
            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress = (badge.badgeProgress.toFloat() / level.badgeGoal.toFloat())
                    .coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progress }, // Compose 1.6+ 권장 람다 사용
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp)),
                    color = PrimaryColor,
                    trackColor = Color.LightGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${badge.badgeProgress}/${level.badgeGoal}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } ?: run {
            Text(
                text = "최고 레벨",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    // 선택된 뱃지 레벨 (탭)
    var selectedTabIndex by remember { mutableStateOf(badge.highLevel.coerceAtMost(badge.badgeDetails.size - 1).coerceAtLeast(0)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 다이얼로그 헤더: 타이틀 및 닫기 버튼
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = badge.badgeName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))



                Spacer(modifier = Modifier.height(16.dp))

                // 선택된 뱃지 상세 정보
                if (badge.badgeDetails.isNotEmpty() && selectedTabIndex < badge.badgeDetails.size) {
                    val selectedBadgeDetail = badge.badgeDetails[selectedTabIndex]
                    BadgeDetailContent(
                        badge = badge,
                        badgeDetail = selectedBadgeDetail
                    )
                }
                // 뱃지 레벨 탭 (가로 스크롤 가능)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,

                ) {
                    itemsIndexed(badge.badgeDetails) { index, badgeDetail ->
                        BadgeTab(
                            badgeDetail = badgeDetail,
                            isSelected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeTab(
    badgeDetail: BadgeDetail,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) PrimaryColor else Color.LightGray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(badgeDetail.badgeDetailImg)
                .crossfade(true)
                .error(R.drawable.ic_badge_lv0)
                .build(),
            contentDescription = "뱃지 레벨 ${badgeDetail.badgeLevel} 이미지",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

@Composable
fun BadgeDetailContent(
    badge: Badge,
    badgeDetail: BadgeDetail
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 뱃지 레벨 이름
        Text(
            text = badgeDetail.badgeDetailName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 뱃지 이미지 (큰 사이즈)
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(badgeDetail.badgeDetailImg)
                    .crossfade(true)
                    .error(R.drawable.ic_badge_lv0)
                    .build(),
                contentDescription = "뱃지 상세 이미지",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 뱃지 설명
        Text(
            text = badgeDetail.badgeDescription,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 현재 진행 상황
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 현재 뱃지 레벨의 진행 상태 계산
            val progress = if (badge.highLevel >= badgeDetail.badgeLevel) {
                // 이미 달성한 레벨
                1.0f
            } else {
                // 현재 진행 중인 레벨
                (badge.badgeProgress.toFloat() / badgeDetail.badgeGoal.toFloat()).coerceIn(0f, 1f)
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryColor,
                trackColor = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 진행 상태 텍스트
            if (badge.highLevel >= badgeDetail.badgeLevel) {
                Text(
                    text = "달성 완료!",
                    fontSize = 14.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "${badge.badgeProgress}/${badgeDetail.badgeGoal}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BadgeOverlay(
    badge: Badge, // 사용되지 않지만 파라미터 유지
    highLevelBadgeDetail: BadgeDetail,
    onDismiss: () -> Unit
) {
    // 애니메이션 상태 관리
    var isVisible by remember { mutableStateOf(false) }

    // 무한 반복 애니메이션 트랜지션
    val infiniteTransition = rememberInfiniteTransition(label = "badge_halo_effect")

    // --- 회전 애니메이션 제거 ---
    // val rotationAngle by ...

    // 밝기 펄스 애니메이션 (후광에 사용)
    val haloPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, // 시작 알파값 (조절 가능)
        targetValue = 0.8f, // 최대 알파값 (조절 가능)
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing), // 속도 조절 가능
            repeatMode = RepeatMode.Reverse
        ), label = "halo_pulse"
    )

    // 등장/사라짐 애니메이션 (기존 유지)
    val alphaAnim by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "overlay_alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "overlay_scale"
    )

    // 컴포넌트가 합성될 때 애니메이션 시작
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // --- UI 구조 ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f * alphaAnim)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.9f)
                .scale(scaleAnim)
                .alpha(alphaAnim)
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "축하합니다!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- 원형 후광 효과와 뱃지 이미지 ---
                Box(
                    modifier = Modifier
                        // 후광 포함 전체 영역 크기. 후광 Box 크기보다 약간 크게 설정 가능
                        .size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. 원형 후광 배경 (Box + radialGradient)
                    val haloColor = Color.White // 후광 색상 (흰색 또는 다른 색상 선택 가능)
                    val haloSize = 190.dp      // 후광 Box 크기 (뱃지 이미지보다 커야 함)

                    Box(
                        modifier = Modifier
                            .size(haloSize)
                            .clip(CircleShape) // 원형으로 클리핑
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        // 중앙 색상 (펄스 알파 적용, 약간 더 밝게)
                                        haloColor.copy(alpha = haloPulseAlpha * 0.8f),
                                        // 중간 색상 (펄스 알파 적용)
                                        haloColor.copy(alpha = haloPulseAlpha * 0.5f),
                                        // 가장자리 색상 (거의 투명)
                                        Color.Transparent
                                    ),
                                    // radius는 Box 크기에 맞춰 자동으로 계산됨
                                    // center는 기본값인 중앙 사용
                                )
                            )
                    )

                    // 2. 뱃지 이미지 (후광 위에 표시)
                    val badgeImageSize = 150.dp // 뱃지 이미지 크기 (후광보다 작게)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(highLevelBadgeDetail.badgeDetailImg)
                            .crossfade(true)
                            .build(),
                        contentDescription = "뱃지 이미지: ${highLevelBadgeDetail.badgeDetailName}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(badgeImageSize)
                        // 이미지 자체에 투명 영역이 있으므로 clip 불필요 (필요하면 추가)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 뱃지 이름
                Text(
                    text = highLevelBadgeDetail.badgeDetailName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 뱃지 설명
                Text(
                    text = highLevelBadgeDetail.badgeDescription,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 안내 메시지
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "화면을 탭하여 계속하기",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss, // 이미지 클릭 시 onDismiss 호출
                interactionSource = remember { MutableInteractionSource() }, // 상호작용 상태 관리 (선택 사항)
                indication = null // 클릭 시 시각적 효과(리플) 제거
            ),
    )
}

//@Composable
//fun BadgeOverlay(
//    badge: Badge,
//    highLevelBadgeDetail: BadgeDetail,
//    onDismiss: () -> Unit
//) {
//    // 애니메이션 상태 관리
//    var isVisible by remember { mutableStateOf(false) }
//
//    // 회전 애니메이션
//    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
//    val rotationAngle by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(10000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ), label = "rotation"
//    )
//
//    // 무지개 색상 펄스 애니메이션
//    val colorAnimAlpha by infiniteTransition.animateFloat(
//        initialValue = 0.5f,
//        targetValue = 0.8f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(2000, easing = LinearEasing),
//            repeatMode = RepeatMode.Reverse
//        ), label = "pulse"
//    )
//
//    // 애니메이션 효과 적용
//    val alphaAnim by animateFloatAsState(
//        targetValue = if (isVisible) 1f else 0f,
//        animationSpec = tween(durationMillis = 500)
//    )
//    val scaleAnim by animateFloatAsState(
//        targetValue = if (isVisible) 1f else 0.8f,
//        animationSpec = spring(dampingRatio = 0.8f)
//    )
//
//    // 컴포넌트가 합성될 때 애니메이션 시작
//    LaunchedEffect(Unit) {
//        isVisible = true
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black.copy(alpha = 0.7f * alphaAnim))
//            .clickable { onDismiss() },
//        contentAlignment = Alignment.Center
//    ) {
//        Box(
//            modifier = Modifier
//                .padding(24.dp)
//                .fillMaxWidth(0.9f)
//                .scale(scaleAnim)
//                .alpha(alphaAnim),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                modifier = Modifier.padding(36.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "축하합니다!",
//                    style = MaterialTheme.typography.headlineLarge,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White,
//                    textAlign = TextAlign.Center
//                )
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                // 무지개빛 회전 효과와 뱃지 이미지
//                Box(
//                    modifier = Modifier
//                        .size(240.dp)
//                        .clip(CircleShape) // 원형으로 클리핑
//                        .background(
//                            brush = Brush.radialGradient( // 또는 sweepGradient
//                                colors = listOf(
//                                    PrimaryColor.copy(alpha = colorAnimAlpha), // 앱 테마 색상 등
//                                    PastelLigtBlue.copy(alpha = colorAnimAlpha * 0.5f),
//                                    Color.Transparent // 바깥쪽은 투명하게
//                                ),
//                                radius = 1600.dp.value * (1f + (colorAnimAlpha - 0.5f) * 0.1f) // 알파값에 따라 미세하게 크기 변화
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//
//
//                    // 뱃지 이미지
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(highLevelBadgeDetail.badgeDetailImg)
//                            .crossfade(true)
//                            .build(),
//                        contentDescription = "뱃지 이미지",
//                        contentScale = ContentScale.Fit,
//                        modifier = Modifier
//                            .size(160.dp) // 이미지는 배경보다 작게 하여 빛나는 효과가 잘 보이도록 함
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                // 뱃지 이름
//                Text(
//                    text = highLevelBadgeDetail.badgeDetailName,
//                    style = MaterialTheme.typography.headlineMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White,
//                    textAlign = TextAlign.Center
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // 뱃지 설명
//                Text(
//                    text = highLevelBadgeDetail.badgeDescription,
//                    style = MaterialTheme.typography.titleMedium,
//                    color = Color.White.copy(alpha = 0.9f),
//                    textAlign = TextAlign.Center
//                )
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                // 안내 메시지
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.TouchApp,
//                        contentDescription = null,
//                        tint = Color.White.copy(alpha = 0.7f),
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(
//                        text = "화면을 탭하여 계속하기",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = Color.White.copy(alpha = 0.7f)
//                    )
//                }
//            }
//        }
//    }
//}
