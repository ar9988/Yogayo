package com.d104.yogaapp.features.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.domain.model.UserRecord
import com.d104.yogaapp.ui.theme.GrayCardColor

@Composable
fun UserRecordCard(
    userRecord: UserRecord,
    showDetailButton: Boolean = false,
    onClickDetail: ()-> Unit = {}
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        ,
        colors = CardDefaults.cardColors(
            containerColor = GrayCardColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top =  16.dp)
        ) {
            Text(
                text = "내 기록",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                fontSize = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "운동 일수 :")
                Text(text = "${userRecord.exDays}일", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "연속 운동 일수 :")
                Text(text = "${userRecord.exConDays}일", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1위 횟수 :")
                Text(text = "${userRecord.roomWin}회", fontWeight = FontWeight.Bold)
            }
            if(showDetailButton){
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onClickDetail() }) {
                        Text(
                            text = "상세보기 >",
                            fontSize = 18.sp
                        )
                    }
                }
            }else{
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}