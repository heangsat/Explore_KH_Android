package com.example.android.cambodia25.ui.components.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.cambodia25.model.Attraction
import com.example.android.cambodia25.ui.components.DestinationImage

@Composable
fun AttractionTileComponent(
    item: Attraction,
    onClick: (Attraction) -> Unit,
    compact: Boolean = false
) {
    val cardWidth = if (compact) 128.dp else 168.dp
    val cardHeight = if (compact) 170.dp else 205.dp
    val imageHeight = if (compact) 88.dp else 118.dp

    Card(
        onClick = { onClick(item) },
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            DestinationImage(
                url = item.imageUrl,
                contentDescription = item.titleEn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(item.titleEn, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    text = item.titleKh,
                    color = Color(0xFF6B7C8E),
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = "★ ${item.rating} · ${item.category}",
                    color = Color(0xFFB17824),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AttractionListItemComponent(
    item: Attraction,
    provinceName: String,
    onClick: (Attraction) -> Unit
) {
    Card(
        onClick = { onClick(item) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.height(112.dp)) {
            DestinationImage(
                url = item.imageUrl,
                contentDescription = item.titleEn,
                modifier = Modifier
                    .width(112.dp)
                    .fillMaxHeight()
            )

            Column(Modifier.padding(10.dp)) {
                Text(item.titleEn, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(item.titleKh, color = Color(0xFF6B7C8E), fontSize = 12.sp)
                Text(
                    text = "$provinceName · ${item.category}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "${item.priceUsd} · ${item.priceKhr}",
                    color = Color(0xFFB17824),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
