package com.example.android.cambodia25.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopBarComponent(
    title: String = "Cambodia 25",
    showNotification: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        if (title == "Cambodia 25" || title == "Cambodia Explore") {
            CambodiaLogoComponent(Modifier.align(Alignment.Center))
        } else {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showNotification) {
            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
        }
    }
}
