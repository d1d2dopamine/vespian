package dev.trial3lib.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.token.SmallControlHeight
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Stroke

/*
 * Fields.
 *
 * A rectangular shell with a hairline border and a text cursor in the palette's
 * ink. No floating label, no filled container, no focus ring that changes the
 * height of the row.
 */

/** Ordinary single-line text. */
@Composable
public fun Trial3TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    maxLength: Int = 80,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    height: Dp = 48.dp,
) {
    val colors = Trial3.colors
    Trial3FieldShell(modifier = modifier, height = height) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(maxLength)) },
            enabled = enabled,
            textStyle = Trial3.typography.bodyLarge.copy(color = colors.ink),
            singleLine = true,
            cursorBrush = SolidColor(colors.ink),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onSearch = { onImeAction() },
                onGo = { onImeAction() },
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Trial3Text(
                        text = placeholder,
                        style = Trial3.typography.bodyLarge,
                        color = colors.muted,
                        maxLines = 1,
                    )
                }
                inner()
            },
        )
    }
}

/**
 * Six hex digits, typed.
 *
 * A colour picker is a wheel, a wheel is a circle, and there are no circles here.
 * A hex field is also the only colour control that can be checked for contrast
 * before it is applied, which is the part that actually matters when somebody can
 * pick their own background.
 */
@Composable
public fun Trial3HexField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Trial3.colors
    Trial3FieldShell(modifier = modifier, height = SmallControlHeight) {
        BasicTextField(
            value = value,
            onValueChange = { text -> onValueChange(text.take(7)) },
            textStyle = Trial3.typography.labelLarge.copy(color = colors.ink),
            singleLine = true,
            cursorBrush = SolidColor(colors.ink),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** A colour, shown as a colour. Bordered, so black on black is still visible. */
@Composable
public fun Trial3Swatch(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color)
            .border(Stroke.hair, Trial3.colors.line),
    )
}
