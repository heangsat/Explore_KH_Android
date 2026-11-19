package com.example.android.cambodia25.ui.components.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.android.cambodia25.R
import com.example.android.cambodia25.ui.screen.DarkNavy

@Composable
fun MapHeroComponent(onLayers: () -> Unit = {}, onLocate: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(315.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(DarkNavy)
    ) {
        Image(
            painter = painterResource(R.drawable.cambodia_map),
            contentDescription = "Illustrated map of Cambodia",
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledIconButton(
                onClick = onLayers,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFFFFF5DC),
                    contentColor = DarkNavy
                ),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Map, contentDescription = "Map layers")
            }

            FilledIconButton(
                onClick = onLocate,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFFFFF5DC),
                    contentColor = DarkNavy
                ),
                shape = CircleShape
            ) {
                Icon(Icons.Default.NearMe, contentDescription = "My location")
            }
        }
    }
}
