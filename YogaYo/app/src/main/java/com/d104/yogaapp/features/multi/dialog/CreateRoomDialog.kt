package com.d104.yogaapp.features.multi.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.d104.domain.model.UserCourse
import com.d104.yogaapp.features.multi.MultiIntent
import com.d104.yogaapp.features.solo.PosesRowWithArrows
import timber.log.Timber

@Composable
fun CreateRoomDialog(
    showDialog: Boolean,
    roomTitle: String,
    roomPassword: String,
    onRoomTitleChange: (String) -> Unit,
    onRoomPasswordChange: (String) -> Unit,
    onRoomPasswordChecked: (Boolean) -> Unit,
    onMaxCountChanged: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onCourseSelect: (UserCourse) -> Unit,
    userCourses: List<UserCourse>,
    onEditCourse: () -> Unit,

    ) {
    var peopleExpanded by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var selectedMaxCount by remember { mutableStateOf("2 명") }
    var selectedCourse by remember { mutableStateOf<UserCourse?>(null) }
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = roomTitle,
                        onValueChange = onRoomTitleChange,
                        label = { Text("그룹 이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "최대 인원 수"
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            TextButton(
                                onClick = { peopleExpanded = !peopleExpanded }
                            ) {
                                Text(selectedMaxCount, color = Color.Black)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "드롭다운 메뉴 열기",
                                    tint = Color.Black
                                )
                            }

                            DropdownMenu(
                                expanded = peopleExpanded,
                                onDismissRequest = { peopleExpanded = false }
                            ) {
                                listOf("2 명", "3 명", "4 명").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedMaxCount = option
                                            var count = 2
                                            when (option) {
                                                "2 명" -> count = 2
                                                "3 명" -> count = 3
                                                "4 명" -> count = 4
                                            }
                                            onMaxCountChanged(count)
                                            peopleExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "비밀번호 설정"
                        )
                        Checkbox(
                            checked = showPassword,
                            onCheckedChange = {
                                showPassword = it
                                onRoomPasswordChecked(it)
                            },
                        )
                    }
                    if (showPassword) {

                        OutlinedTextField(
                            value = roomPassword,
                            onValueChange = onRoomPasswordChange,
                            label = { Text("비밀번호") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCourse == null) Text("코스 선택")
                        else Text(selectedCourse!!.courseName)
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(
                            onClick = { courseExpanded = !courseExpanded },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("코스 선택하기", color = Color.Black)
                        }
                        DropdownMenu(
                            expanded = courseExpanded,
                            onDismissRequest = { courseExpanded = false }
                        ) {
                            userCourses.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.courseName) },
                                    onClick = {
                                        selectedCourse = option
                                        courseExpanded = false
                                        onCourseSelect(selectedCourse!!)
                                    }
                                )
                            }
                        }
                        if (selectedCourse != null) {
                            IconButton(
                                onClick = onEditCourse
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Button"
                                )
                            }
                        }
                    }
                    if (selectedCourse != null) {
                        PosesRowWithArrows(selectedCourse!!)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("취소")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = onConfirm) {
                            Text("만들기")
                        }
                    }
                }
            }
        }
    }
}
