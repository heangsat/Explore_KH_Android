package com.example.android.cambodia25.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.android.cambodia25.data.Catalog
import com.example.android.cambodia25.data.ExploreCatalog
import com.example.android.cambodia25.model.Attraction
import com.example.android.cambodia25.model.Province
import com.example.android.cambodia25.ui.components.explore.ExploreBottomBarComponent
import com.example.android.cambodia25.ui.components.explore.ExploreGold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val DarkNavy = Color(0xFF1D3557)

@Composable
fun MainScreen() {
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedProvince by remember { mutableStateOf<Province?>(null) }
    var selectedAttraction by remember { mutableStateOf<Attraction?>(null) }
    var savedIds by remember { mutableStateOf(setOf<String>()) }

    val catalog by produceState<Catalog?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            ExploreCatalog.load(context.applicationContext)
        }
    }

    val data = catalog
    if (data == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ExploreGold)
        }
        return
    }

    val province = selectedProvince
    val attraction = selectedAttraction

    when {
        attraction != null -> AttractionDetailScreen(
            item = attraction,
            province = data.findProvince(attraction.provinceId),
            saved = attraction.id in savedIds,
            onBack = { selectedAttraction = null },
            onSave = { id ->
                savedIds = if (id in savedIds) savedIds - id else savedIds + id
            }
        )

        province != null -> ProvinceScreen(
            catalog = data,
            province = province,
            onAttraction = { selectedAttraction = it },
            onBack = { selectedProvince = null }
        )

        else -> Scaffold(
            containerColor = DarkNavy,
            bottomBar = {
                ExploreBottomBarComponent(selectedTab) { selectedTab = it }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> HomeScreen(data, padding) { selectedProvince = it }
                1 -> DiscoverScreen(data, padding) { selectedAttraction = it }
                2 -> SavedScreen(data, padding, savedIds) { selectedAttraction = it }
                else -> ProfileScreen(padding, savedIds.size)
            }
        }
    }
}
