package com.example.android.cambodia25.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android.cambodia25.model.Province

@Composable
fun ProvinceCarouselComponent(
    provinces: List<Province>,
    onProvinceClick: (Province) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        items(provinces) { province ->
            Card(
                onClick = { onProvinceClick(province) },
                modifier = Modifier
                    .width(138.dp)
                    .height(164.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box {
                    DestinationImage(
                        url = province.imageUrl,
                        contentDescription = province.nameEn,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = province.nameEn,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = province.nameKh,
                            color = Color(0xFFE4E9EF),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
