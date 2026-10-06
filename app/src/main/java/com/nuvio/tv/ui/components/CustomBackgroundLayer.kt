package com.nuvio.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nuvio.tv.domain.model.CustomBackground
import com.nuvio.tv.domain.model.CustomBackgroundMode

/** Full-screen user background drawn behind every screen when a custom background is active. */
@Composable
fun CustomBackgroundLayer(
    background: CustomBackground,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when (background.mode) {
            CustomBackgroundMode.COLOR -> Box(Modifier.fillMaxSize().background(background.color))
            CustomBackgroundMode.GRADIENT -> Box(
                Modifier.fillMaxSize().background(Brush.linearGradient(background.gradient))
            )
            CustomBackgroundMode.IMAGE -> AsyncImage(
                model = background.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (background.blur > 0) Modifier.blur(background.blur.dp) else Modifier)
            )
            CustomBackgroundMode.OFF -> Unit
        }
        if (background.dim > 0) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = background.dim / 100f)))
        }
    }
}
