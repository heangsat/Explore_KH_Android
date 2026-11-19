package com.example.android.cambodia25.ui.components.explore

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.android.cambodia25.ui.screen.DarkNavy

@Composable
fun ExploreBottomBarComponent(selected: Int, onSelected: (Int) -> Unit) {
    val tabs = listOf(
        "Explore" to Icons.Default.Explore,
        "Discover" to Icons.Default.Search,
        "Saved" to Icons.Default.FavoriteBorder,
        "Profile" to Icons.Default.Person
    )

    NavigationBar(containerColor = DarkNavy) {
        tabs.forEachIndexed { index, (label, icon) ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelected(index) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ExploreGold,
                    selectedTextColor = ExploreGold,
                    unselectedIconColor = SoftText,
                    unselectedTextColor = SoftText
                )
            )
        }
    }
}
