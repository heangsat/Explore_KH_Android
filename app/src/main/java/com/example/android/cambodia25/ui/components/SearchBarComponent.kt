package com.example.android.cambodia25.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.cambodia25.ui.components.explore.ExploreGold
import com.example.android.cambodia25.ui.screen.DarkNavy

@Composable
fun SearchBarComponent(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Explore Cambodia's 25 provinces"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = ExploreGold,
            unfocusedTextColor = DarkNavy,
            focusedTextColor = DarkNavy,
            unfocusedPlaceholderColor = Color.Gray,
            focusedPlaceholderColor = Color.Gray,
            unfocusedLeadingIconColor = Color.Gray,
            focusedLeadingIconColor = DarkNavy
        )
    )
}
