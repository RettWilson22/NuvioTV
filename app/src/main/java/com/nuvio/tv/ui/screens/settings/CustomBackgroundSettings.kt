@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.screens.settings

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.R
import com.nuvio.tv.domain.model.CUSTOM_BACKGROUND_MAX_BLUR
import com.nuvio.tv.domain.model.CUSTOM_BACKGROUND_MAX_DIM
import com.nuvio.tv.domain.model.CUSTOM_BACKGROUND_MIN_CARD_OPACITY
import com.nuvio.tv.domain.model.CustomBackground
import com.nuvio.tv.domain.model.CustomBackgroundColorPresets
import com.nuvio.tv.domain.model.CustomBackgroundGradientPresets
import com.nuvio.tv.domain.model.CustomBackgroundMode
import com.nuvio.tv.ui.components.NuvioDialog
import com.nuvio.tv.ui.screens.detail.requestFocusAfterFrames
import com.nuvio.tv.ui.theme.NuvioTheme

@Composable
private fun CustomBackgroundMode.label(): String = stringResource(
    when (this) {
        CustomBackgroundMode.OFF -> R.string.appearance_background_mode_off
        CustomBackgroundMode.COLOR -> R.string.appearance_background_mode_color
        CustomBackgroundMode.GRADIENT -> R.string.appearance_background_mode_gradient
        CustomBackgroundMode.IMAGE -> R.string.appearance_background_mode_image
    }
)

/** Appearance → Background: theme default, preset color/gradient, or any image URL. */
@Composable
internal fun CustomBackgroundSettingsGroup(
    background: CustomBackground,
    onEvent: (ThemeSettingsEvent) -> Unit
) {
    var showUrlDialog by remember { mutableStateOf(false) }
    val firstModeFocusRequester = remember { FocusRequester() }
    val firstSwatchFocusRequester = remember { FocusRequester() }

    SettingsGroupCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.appearance_background),
        subtitle = stringResource(R.string.appearance_background_subtitle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NuvioTheme.spacing.xs, vertical = NuvioTheme.spacing.xs)
                .settingsOptionRow(firstModeFocusRequester),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CustomBackgroundMode.entries.forEachIndexed { index, mode ->
                BackgroundModeCard(
                    label = mode.label(),
                    isSelected = background.mode == mode,
                    onClick = {
                        if (mode == CustomBackgroundMode.IMAGE && background.imageUrl == null) {
                            showUrlDialog = true
                        } else {
                            onEvent(ThemeSettingsEvent.SetBackgroundMode(mode))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .then(if (index == 0) Modifier.focusRequester(firstModeFocusRequester) else Modifier)
                )
            }
        }

        when (background.mode) {
            CustomBackgroundMode.COLOR -> BackgroundSwatchRow(
                count = CustomBackgroundColorPresets.size,
                selectedIndex = background.colorIndex,
                brushFor = { SolidColor(CustomBackgroundColorPresets[it]) },
                onSelect = { onEvent(ThemeSettingsEvent.SetBackgroundColor(it)) },
                firstFocusRequester = firstSwatchFocusRequester
            )
            CustomBackgroundMode.GRADIENT -> BackgroundSwatchRow(
                count = CustomBackgroundGradientPresets.size,
                selectedIndex = background.gradientIndex,
                brushFor = { Brush.linearGradient(CustomBackgroundGradientPresets[it]) },
                onSelect = { onEvent(ThemeSettingsEvent.SetBackgroundGradient(it)) },
                firstFocusRequester = firstSwatchFocusRequester
            )
            CustomBackgroundMode.IMAGE -> SettingsActionRow(
                title = stringResource(R.string.appearance_background_image_url),
                subtitle = stringResource(R.string.appearance_background_image_url_subtitle),
                value = background.imageUrl?.let(::shortenUrl),
                onClick = { showUrlDialog = true }
            )
            CustomBackgroundMode.OFF -> Unit
        }

        if (background.mode != CustomBackgroundMode.OFF) {
            SliderSettingsItem(
                title = stringResource(R.string.appearance_background_dim),
                subtitle = stringResource(R.string.appearance_background_dim_subtitle),
                value = background.dim,
                valueText = "${background.dim}%",
                minValue = 0,
                maxValue = CUSTOM_BACKGROUND_MAX_DIM,
                step = 5,
                onValueChange = { onEvent(ThemeSettingsEvent.SetBackgroundDim(it)) }
            )
            if (background.mode == CustomBackgroundMode.IMAGE) {
                SliderSettingsItem(
                    title = stringResource(R.string.appearance_background_blur),
                    subtitle = stringResource(R.string.appearance_background_blur_subtitle),
                    value = background.blur,
                    valueText = background.blur.toString(),
                    minValue = 0,
                    maxValue = CUSTOM_BACKGROUND_MAX_BLUR,
                    step = 4,
                    onValueChange = { onEvent(ThemeSettingsEvent.SetBackgroundBlur(it)) }
                )
            }
            SliderSettingsItem(
                title = stringResource(R.string.appearance_background_card_opacity),
                subtitle = stringResource(R.string.appearance_background_card_opacity_subtitle),
                value = background.cardOpacity,
                valueText = "${background.cardOpacity}%",
                minValue = CUSTOM_BACKGROUND_MIN_CARD_OPACITY,
                maxValue = 100,
                step = 5,
                onValueChange = { onEvent(ThemeSettingsEvent.SetBackgroundCardOpacity(it)) }
            )
            SettingsActionRow(
                title = stringResource(R.string.appearance_background_reset),
                subtitle = null,
                onClick = { onEvent(ThemeSettingsEvent.ResetBackground) }
            )
        }
    }

    if (showUrlDialog) {
        BackgroundImageUrlDialog(
            currentUrl = background.imageUrl.orEmpty(),
            onSave = { url ->
                onEvent(ThemeSettingsEvent.SetBackgroundImageUrl(url))
                showUrlDialog = false
            },
            onDismiss = { showUrlDialog = false }
        )
    }
}

private fun shortenUrl(url: String): String {
    val withoutScheme = url.substringAfter("://")
    return if (withoutScheme.length <= 32) withoutScheme else withoutScheme.take(29) + "…"
}

@Composable
private fun BackgroundModeCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Card(
        onClick = onClick,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        colors = CardDefaults.colors(
            containerColor = if (isSelected) NuvioTheme.colors.BackgroundCard else NuvioTheme.colors.Background,
            focusedContainerColor = NuvioTheme.colors.BackgroundCard
        ),
        border = CardDefaults.border(
            border = if (isSelected) {
                Border(border = NuvioTheme.focusRing.border(NuvioTheme.spacing.hairline), shape = shape)
            } else {
                Border.None
            },
            focusedBorder = Border(border = NuvioTheme.focusRing.border(NuvioTheme.spacing.xxs), shape = shape)
        ),
        shape = CardDefaults.shape(shape),
        scale = CardDefaults.scale(focusedScale = 1f, pressedScale = 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = NuvioTheme.spacing.md, horizontal = NuvioTheme.spacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = NuvioTheme.colors.TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isFocused || isSelected) NuvioTheme.colors.TextPrimary else NuvioTheme.colors.TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BackgroundSwatchRow(
    count: Int,
    selectedIndex: Int,
    brushFor: (Int) -> Brush,
    onSelect: (Int) -> Unit,
    firstFocusRequester: FocusRequester
) {
    val shape = RoundedCornerShape(12.dp)
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .settingsOptionRow(firstFocusRequester),
        contentPadding = PaddingValues(horizontal = NuvioTheme.spacing.xs, vertical = NuvioTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items((0 until count).toList()) { index ->
            val isSelected = index == selectedIndex
            Card(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .size(width = 72.dp, height = 48.dp)
                    .then(if (index == 0) Modifier.focusRequester(firstFocusRequester) else Modifier),
                colors = CardDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                ),
                border = CardDefaults.border(
                    border = if (isSelected) {
                        Border(border = NuvioTheme.focusRing.border(NuvioTheme.spacing.hairline), shape = shape)
                    } else {
                        Border.None
                    },
                    focusedBorder = Border(border = NuvioTheme.focusRing.border(NuvioTheme.spacing.xxs), shape = shape)
                ),
                shape = CardDefaults.shape(shape),
                scale = CardDefaults.scale(focusedScale = 1.08f, pressedScale = 1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(width = 72.dp, height = 48.dp)
                        .clip(shape)
                        .background(brushFor(index)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.cd_selected),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundImageUrlDialog(
    currentUrl: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(currentUrl) }
    var isInputFocused by remember { mutableStateOf(false) }
    val inputFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val trimmed = value.trim()
    val isValid = trimmed.startsWith("https://") || trimmed.startsWith("http://")
    val fieldShape = RoundedCornerShape(10.dp)

    LaunchedEffect(Unit) { inputFocusRequester.requestFocusAfterFrames() }

    NuvioDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.appearance_background_image_url),
        subtitle = stringResource(R.string.appearance_background_image_url_dialog_hint),
        width = 700.dp
    ) {
        Card(
            onClick = { inputFocusRequester.requestFocus() },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isInputFocused = it.isFocused || it.hasFocus },
            colors = CardDefaults.colors(
                containerColor = NuvioTheme.colors.BackgroundElevated,
                focusedContainerColor = NuvioTheme.colors.BackgroundElevated
            ),
            border = CardDefaults.border(
                border = Border(
                    border = androidx.compose.foundation.BorderStroke(NuvioTheme.spacing.hairline, NuvioTheme.colors.Border),
                    shape = fieldShape
                ),
                focusedBorder = Border(border = NuvioTheme.focusRing.border(NuvioTheme.spacing.xxs), shape = fieldShape)
            ),
            shape = CardDefaults.shape(fieldShape),
            scale = CardDefaults.scale(focusedScale = 1f)
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = NuvioTheme.spacing.md)) {
                BasicTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(inputFocusRequester)
                        .onKeyEvent { event ->
                            event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER &&
                                event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = NuvioTheme.colors.TextPrimary),
                    cursorBrush = SolidColor(if (isInputFocused) NuvioTheme.colors.Primary else Color.Transparent),
                    decorationBox = { innerTextField ->
                        if (value.isBlank()) {
                            Text(
                                text = "https://…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NuvioTheme.colors.TextTertiary
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.colors(
                    containerColor = NuvioTheme.colors.BackgroundElevated,
                    contentColor = NuvioTheme.colors.TextPrimary
                )
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            Spacer(modifier = Modifier.width(NuvioTheme.spacing.sm))
            Button(
                onClick = { if (isValid) onSave(trimmed) },
                enabled = isValid,
                colors = ButtonDefaults.colors(
                    containerColor = NuvioTheme.colors.BackgroundCard,
                    contentColor = NuvioTheme.colors.TextPrimary
                )
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
