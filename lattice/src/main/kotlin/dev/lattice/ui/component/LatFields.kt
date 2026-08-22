package dev.lattice.ui.component

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
import dev.lattice.ui.Lattice
import dev.lattice.ui.token.SmallControlHeight
import dev.lattice.ui.token.Space
import dev.lattice.ui.token.Stroke

/*
 * Fields.
 *
 * A rectangular shell with a hairline border and a text cursor in the palette's
 * ink. No floating label, no filled container, no focus ring that changes the
 * height of the row.
 */

/** Ordinary single-line text. */
@Composable
public fun LatTextField(
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
    val colors = Lattice.colors
    Box(
        modifier = modifier
            .height(height)
            .border(Stroke.hair, colors.line)
            .padding(horizontal = Space.md),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.take(maxLength)) },
            enabled = enabled,
            textStyle = Lattice.typography.bodyLarge.copy(color = colors.ink),
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
                    LatText(
                        text = placeholder,
                        style = Lattice.typography.bodyLarge,
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
public fun LatHexField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Lattice.colors
    Box(
        modifier = modifier
            .height(SmallControlHeight)
            .border(Stroke.hair, colors.line)
            .padding(horizontal = Space.md),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { text -> onValueChange(text.take(7)) },
            textStyle = Lattice.typography.labelLarge.copy(color = colors.ink),
            singleLine = true,
            cursorBrush = SolidColor(colors.ink),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** A colour, shown as a colour. Bordered, so black on black is still visible. */
@Composable
public fun LatSwatch(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color)
            .border(Stroke.hair, Lattice.colors.line),
    )
}
