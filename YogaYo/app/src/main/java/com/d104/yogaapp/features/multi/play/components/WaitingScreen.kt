package com.d104.yogaapp.features.multi.play.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.d104.domain.model.PeerUser

@Composable
fun WaitingScreen(
    userList: Map<String, PeerUser>
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "참여자 목록",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                items(userList.keys.toList()) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 원형 테두리가 있는 사용자 아이콘
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, Color.Gray, CircleShape)
                                .clip(CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "Person Icon"
                            )
                        }

                        // 텍스트에 weight를 주어 남은 공간을 차지하도록 함
                        Text(
                            text = "${userList[it]?.nickName}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        // 체크 아이콘
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, if (userList[it]?.isReady == true) Color.Green else Color.Gray, CircleShape)
                                .clip(CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (userList[it]?.isReady == true) Icons.Filled.Check else Icons.Outlined.Check,
                                contentDescription = if (userList[it]?.isReady == true) "Checked" else "Unchecked",
                                tint = if (userList[it]?.isReady == true) Color.Green else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
