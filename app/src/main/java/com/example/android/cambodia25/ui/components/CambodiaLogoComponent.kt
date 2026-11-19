package com.example.android.cambodia25.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.android.cambodia25.R

@Composable
fun CambodiaLogoComponent(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.cambodia_logo),
        contentDescription = "Cambodia 25 logo",
        modifier = modifier
            .width(168.dp)
            .height(46.dp),
        contentScale = ContentScale.Fit
    )
}
