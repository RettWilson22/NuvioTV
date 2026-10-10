package com.nuvio.tv.domain.model

import androidx.compose.ui.graphics.Color

enum class CustomBackgroundMode {
    OFF,
    COLOR,
    GRADIENT,
    IMAGE
}

/** Preset solid colors offered in Appearance > Background. */
val CustomBackgroundColorPresets: List<Color> = listOf(
    Color(0xFF0B1426),
    Color(0xFF1B0F2E),
    Color(0xFF0F2A1E),
    Color(0xFF2B0D12),
    Color(0xFF2A1C08),
    Color(0xFF14181F),
    Color(0xFF3A1F3D),
    Color(0xFF0E2F35)
)

/** Preset gradients, top-left to bottom-right, offered in Appearance > Background. */
val CustomBackgroundGradientPresets: List<List<Color>> = listOf(
    listOf(Color(0xFF1E3C72), Color(0xFF0B0F1A)),
    listOf(Color(0xFF42275A), Color(0xFF734B6D), Color(0xFF0D0D0D)),
    listOf(Color(0xFF134E5E), Color(0xFF0B2B26), Color(0xFF050807)),
    listOf(Color(0xFFCB356B), Color(0xFF3A0F1F), Color(0xFF0A0507)),
    listOf(Color(0xFFF7971E), Color(0xFF5A2A00), Color(0xFF0D0804)),
    listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0), Color(0xFF07031A)),
    listOf(Color(0xFF00C9FF), Color(0xFF0D3B66), Color(0xFF020812)),
    listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF0C0C0C))
)

const val CUSTOM_BACKGROUND_MAX_DIM = 90
const val CUSTOM_BACKGROUND_MAX_BLUR = 40
const val CUSTOM_BACKGROUND_MIN_CARD_OPACITY = 30

data class CustomBackground(
    val mode: CustomBackgroundMode = CustomBackgroundMode.OFF,
    val colorIndex: Int = 0,
    val gradientIndex: Int = 0,
    /** Any http(s) image link the user pasted in. */
    val imageUrl: String? = null,
    /** Percent of black drawn over the background, so text stays readable over bright images. */
    val dim: Int = 45,
    /** Blur radius in dp for image backgrounds (needs Android 12+ on the TV). */
    val blur: Int = 0,
    /** Opacity percent of the cards drawn over the background. */
    val cardOpacity: Int = 85
) {
    val isActive: Boolean
        get() = when (mode) {
            CustomBackgroundMode.OFF -> false
            CustomBackgroundMode.IMAGE -> !imageUrl.isNullOrBlank()
            else -> true
        }

    val color: Color
        get() = CustomBackgroundColorPresets[colorIndex.coerceIn(CustomBackgroundColorPresets.indices)]

    val gradient: List<Color>
        get() = CustomBackgroundGradientPresets[gradientIndex.coerceIn(CustomBackgroundGradientPresets.indices)]
}
