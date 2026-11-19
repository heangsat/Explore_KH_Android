package com.example.android.cambodia25.ui.components.explore

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SectionTitleComponent(
    title: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.weight(1f))

        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action, color = ExploreGold)
            }
        }
    }
}
