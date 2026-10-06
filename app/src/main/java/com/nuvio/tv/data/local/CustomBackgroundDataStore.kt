package com.nuvio.tv.data.local

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nuvio.tv.core.profile.ProfileManager
import com.nuvio.tv.domain.model.CUSTOM_BACKGROUND_MAX_BLUR
import com.nuvio.tv.domain.model.CUSTOM_BACKGROUND_MAX_DIM
import com.nuvio.tv.domain.model.CUSTOM_BACKGROUND_MIN_CARD_OPACITY
import com.nuvio.tv.domain.model.CustomBackground
import com.nuvio.tv.domain.model.CustomBackgroundColorPresets
import com.nuvio.tv.domain.model.CustomBackgroundGradientPresets
import com.nuvio.tv.domain.model.CustomBackgroundMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomBackgroundDataStore @Inject constructor(
    private val factory: ProfileDataStoreFactory,
    private val profileManager: ProfileManager
) {
    companion object {
        private const val FEATURE = "custom_background"
    }

    private fun store(profileId: Int = profileManager.activeProfileId.value) =
        factory.get(profileId, FEATURE)

    private val modeKey = stringPreferencesKey("mode")
    private val colorIndexKey = intPreferencesKey("color_index")
    private val gradientIndexKey = intPreferencesKey("gradient_index")
    private val imageUrlKey = stringPreferencesKey("image_url")
    private val dimKey = intPreferencesKey("dim")
    private val blurKey = intPreferencesKey("blur")
    private val cardOpacityKey = intPreferencesKey("card_opacity")

    val background: Flow<CustomBackground> = profileManager.activeProfileId.flatMapLatest { pid ->
        factory.get(pid, FEATURE).data.map(::decode)
    }

    private fun decode(prefs: Preferences): CustomBackground {
        val defaults = CustomBackground()
        return CustomBackground(
            mode = prefs[modeKey]?.let { name -> CustomBackgroundMode.entries.firstOrNull { it.name == name } }
                ?: CustomBackgroundMode.OFF,
            colorIndex = (prefs[colorIndexKey] ?: 0).coerceIn(CustomBackgroundColorPresets.indices),
            gradientIndex = (prefs[gradientIndexKey] ?: 0).coerceIn(CustomBackgroundGradientPresets.indices),
            imageUrl = prefs[imageUrlKey]?.takeIf { it.isNotBlank() },
            dim = (prefs[dimKey] ?: defaults.dim).coerceIn(0, CUSTOM_BACKGROUND_MAX_DIM),
            blur = (prefs[blurKey] ?: defaults.blur).coerceIn(0, CUSTOM_BACKGROUND_MAX_BLUR),
            cardOpacity = (prefs[cardOpacityKey] ?: defaults.cardOpacity)
                .coerceIn(CUSTOM_BACKGROUND_MIN_CARD_OPACITY, 100)
        )
    }

    suspend fun setMode(mode: CustomBackgroundMode) {
        store().edit { it[modeKey] = mode.name }
    }

    suspend fun setColorIndex(index: Int) {
        store().edit {
            it[colorIndexKey] = index.coerceIn(CustomBackgroundColorPresets.indices)
            it[modeKey] = CustomBackgroundMode.COLOR.name
        }
    }

    suspend fun setGradientIndex(index: Int) {
        store().edit {
            it[gradientIndexKey] = index.coerceIn(CustomBackgroundGradientPresets.indices)
            it[modeKey] = CustomBackgroundMode.GRADIENT.name
        }
    }

    suspend fun setImageUrl(url: String) {
        store().edit {
            it[imageUrlKey] = url.trim()
            it[modeKey] = CustomBackgroundMode.IMAGE.name
        }
    }

    suspend fun setDim(dim: Int) {
        store().edit { it[dimKey] = dim.coerceIn(0, CUSTOM_BACKGROUND_MAX_DIM) }
    }

    suspend fun setBlur(blur: Int) {
        store().edit { it[blurKey] = blur.coerceIn(0, CUSTOM_BACKGROUND_MAX_BLUR) }
    }

    suspend fun setCardOpacity(opacity: Int) {
        store().edit { it[cardOpacityKey] = opacity.coerceIn(CUSTOM_BACKGROUND_MIN_CARD_OPACITY, 100) }
    }

    suspend fun reset() {
        store().edit { it.clear() }
    }
}
