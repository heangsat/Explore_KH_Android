package com.example.android.cambodia25.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.cambodia25.R
import com.example.android.cambodia25.data.Catalog
import com.example.android.cambodia25.model.Attraction
import com.example.android.cambodia25.model.Province
import com.example.android.cambodia25.ui.components.DestinationImage
import com.example.android.cambodia25.ui.components.ProvinceCarouselComponent
import com.example.android.cambodia25.ui.components.SearchBarComponent
import com.example.android.cambodia25.ui.components.TopBarComponent
import com.example.android.cambodia25.ui.components.explore.AttractionListItemComponent
import com.example.android.cambodia25.ui.components.explore.AttractionTileComponent
import com.example.android.cambodia25.ui.components.explore.DetailToolbarComponent
import com.example.android.cambodia25.ui.components.explore.ExploreGold
import com.example.android.cambodia25.ui.components.explore.MapHeroComponent
import com.example.android.cambodia25.ui.components.explore.SectionTitleComponent
import com.example.android.cambodia25.ui.components.explore.SoftText
import com.example.android.cambodia25.ui.components.explore.SurfaceNavy

@Composable
fun HomeScreen(
    catalog: Catalog,
    padding: PaddingValues,
    onProvince: (Province) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val provinces = catalog.provinces.filter {
        it.nameEn.contains(query, ignoreCase = true) || it.nameKh.contains(query)
    }
    val trending = catalog.attractions.sortedByDescending { it.rating }.take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TopBarComponent()
        SearchBarComponent(
            value = query,
            onValueChange = { query = it },
            placeholder = "Explore Cambodia's 25 provinces"
        )

        Spacer(Modifier.height(14.dp))
        MapHeroComponent()
        Spacer(Modifier.height(14.dp))

        SectionTitleComponent("Explore provinces", "Swipe all")
        ProvinceCarouselComponent(provinces, onProvince)

        SectionTitleComponent("Trending discoveries", "See all")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(trending) { attraction ->
                AttractionTileComponent(attraction, onClick = {}, compact = true)
            }
        }

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
fun ProvinceScreen(
    catalog: Catalog,
    province: Province,
    onAttraction: (Attraction) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val attractions = catalog.attractionsFor(province.id)
    val results = attractions.filter {
        it.titleEn.contains(query, ignoreCase = true) || it.titleKh.contains(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        DetailToolbarComponent(title = province.nameEn, onBack = onBack)

        DestinationImage(
            url = province.imageUrl,
            contentDescription = province.nameEn,
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = province.nameEn,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = province.nameKh,
                color = ExploreGold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "${province.region} · ${attractions.size} destinations",
                color = ExploreGold,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = province.description,
                color = Color.White,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(12.dp))
            SearchBarComponent(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search in ${province.nameEn}..."
            )

            Spacer(Modifier.height(10.dp))
            SectionTitleComponent("Popular landmarks", "Swipe all")
            if (results.isEmpty()) {
                Text(
                    text = "No landmarks match your search.",
                    color = SoftText,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(results) { attraction ->
                        AttractionTileComponent(attraction, onAttraction)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            SectionTitleComponent("Things to do")
            Text(
                text = "Local culture, food, nature and memorable experiences curated for your trip.",
                color = SoftText
            )

            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceNavy)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Quick info", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("• Best time: November – April", color = SoftText)
                    Text("• Respect local culture and dress modestly at temples", color = SoftText)
                }
            }
        }
    }
}

@Composable
fun DiscoverScreen(
    catalog: Catalog,
    padding: PaddingValues,
    onAttraction: (Attraction) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    val categories = listOf("All", "Heritage", "Eco-Tourism", "Culture", "Leisure")
    val results = catalog.attractions.filter { attraction ->
        val provinceName = catalog.provinceName(attraction.provinceId)
        val matchesCategory = category == "All" || attraction.category == category
        val matchesQuery = query.isBlank() ||
            attraction.titleEn.contains(query, ignoreCase = true) ||
            attraction.titleKh.contains(query) ||
            provinceName.contains(query, ignoreCase = true)

        matchesCategory && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(padding)
            .padding(16.dp)
    ) {
        TopBarComponent("Cambodia Explore")
        SearchBarComponent(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search attractions, activities, or food..."
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(categories) { item ->
                FilterChip(
                    selected = category == item,
                    onClick = { category = item },
                    label = { Text(item) }
                )
            }
        }

        SectionTitleComponent("${results.size} destinations")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(results) { attraction ->
                AttractionListItemComponent(
                    item = attraction,
                    provinceName = catalog.provinceName(attraction.provinceId),
                    onClick = onAttraction
                )
            }
        }
    }
}

@Composable
fun AttractionDetailScreen(
    item: Attraction,
    province: Province?,
    saved: Boolean,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val provinceName = province?.nameEn ?: "Cambodia"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .verticalScroll(rememberScrollState())
    ) {
        DetailToolbarComponent(
            title = provinceName,
            onBack = onBack,
            isSaved = saved,
            onSave = { onSave(item.id) }
        )

        DestinationImage(
            url = item.imageUrl,
            contentDescription = item.titleEn,
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.titleEn,
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.titleKh,
                color = ExploreGold,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = "★ ${item.rating} · $provinceName · ${item.category}",
                color = ExploreGold,
                modifier = Modifier.padding(top = 6.dp)
            )

            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {}) {
                    Text("Get ticket")
                }

                OutlinedButton(
                    onClick = {
                        val query = Uri.encode("${item.titleEn}, $provinceName, Cambodia")
                        val uri = Uri.parse("geo:0,0?q=$query")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Directions")
                }

                OutlinedButton(onClick = {}) {
                    Text("Audio guide")
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = SurfaceNavy)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Important information", color = ExploreGold, fontWeight = FontWeight.Bold)
                    Text("• Opening hours: ${item.openingHours}", color = Color.White)
                    Text("• Admission: ${item.priceUsd} / ${item.priceKhr}", color = Color.White)
                    Text("• Coordinates: ${item.latitude}, ${item.longitude}", color = Color.White)
                    Text("• Dress code: modest (knees & shoulders covered)", color = Color.White)
                }
            }

            Text(
                text = "About",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp, bottom = 5.dp)
            )
            Text(item.descriptionEn, color = SoftText)
            Text(
                text = item.descriptionKh,
                color = SoftText,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                text = "Reviews uploaded by travelers",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 22.dp)
            )
            HorizontalDivider(color = SoftText.copy(alpha = 0.3f))
            Text(
                text = "Photos uploaded by travelers",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun SavedScreen(
    catalog: Catalog,
    padding: PaddingValues,
    savedIds: Set<String>,
    onAttraction: (Attraction) -> Unit
) {
    val saved = catalog.attractions.filter { it.id in savedIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(padding)
            .padding(16.dp)
    ) {
        TopBarComponent("My Saved Places", showNotification = false)
        SearchBarComponent(
            value = "",
            onValueChange = {},
            placeholder = "Search saved places..."
        )

        Spacer(Modifier.height(12.dp))
        SectionTitleComponent("My saved places")

        if (saved.isEmpty()) {
            Text(
                text = "Save destinations to build your travel bucket list.",
                color = SoftText,
                modifier = Modifier.padding(top = 30.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(saved) { attraction ->
                    AttractionListItemComponent(
                        item = attraction,
                        provinceName = catalog.provinceName(attraction.provinceId),
                        onClick = onAttraction
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(padding: PaddingValues, savedCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        TopBarComponent("My Profile", showNotification = false)

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = ExploreGold
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Somnak Kim",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Local Explorer · Level 5", color = SoftText)
            }

            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = {}) {
                Text("Edit profile")
            }
        }

        Card(
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("Visited provinces", "12/25")
                ProfileStat("Saved places", savedCount.toString())
                ProfileStat("Reviews", "6")
            }
        }

        Text(
            text = "My travel stats",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(18.dp, 20.dp, 18.dp, 8.dp)
        )

        Image(
            painter = painterResource(R.drawable.cambodia_map),
            contentDescription = "Travel map",
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .padding(horizontal = 16.dp),
            contentScale = ContentScale.Fit
        )

        val profileItems = listOf(
            "My reviews",
            "Travel history",
            "Achievements & badges",
            "Download offline maps",
            "Help & feedback"
        )

        profileItems.forEach { label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ExploreGold
                )
                Text(label, color = Color.White, modifier = Modifier.weight(1f))

                if (label == "Download offline maps") {
                    Switch(checked = false, onCheckedChange = {})
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = DarkNavy, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}
