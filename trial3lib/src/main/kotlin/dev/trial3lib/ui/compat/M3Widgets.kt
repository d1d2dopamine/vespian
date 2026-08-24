package dev.trial3lib.ui.compat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.trial3lib.ui.LocalContentColor
import dev.trial3lib.ui.LocalTrial3TextStyle
import dev.trial3lib.ui.Trial3
import dev.trial3lib.ui.component.Trial3Block
import dev.trial3lib.ui.component.Trial3BottomBar
import dev.trial3lib.ui.component.Trial3Busy
import dev.trial3lib.ui.component.Trial3ButtonShell
import dev.trial3lib.ui.component.Trial3DialogShell
import dev.trial3lib.ui.component.Trial3FieldShell
import dev.trial3lib.ui.component.Trial3IconButtonShell
import dev.trial3lib.ui.component.Trial3NavItemShell
import dev.trial3lib.ui.component.Trial3Radio
import dev.trial3lib.ui.component.Trial3Rule
import dev.trial3lib.ui.component.Trial3Scaffold
import dev.trial3lib.ui.component.Trial3Toggle
import dev.trial3lib.ui.component.Trial3TopBarShell
import dev.trial3lib.ui.graphic.Trial3Glyph
import dev.trial3lib.ui.graphic.Trial3GlyphIcon
import dev.trial3lib.ui.token.Alpha
import dev.trial3lib.ui.token.BarHeight
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Stroke
import dev.trial3lib.ui.token.TouchTarget

/*
 * The rest of the bridge: the widgets.
 *
 * Material3Compat.kt answers the theme and the text; this file answers the
 * components an app actually places on a screen. Same rule as the theme half --
 * the names are Material's, every pixel is Trial3's. Nothing is raised off the
 * page, nothing is rounded, and the only colour is the accent.
 *
 * These are shims, not components. When a screen is rewritten, its Card becomes
 * a Trial3Panel and its Button becomes a Trial3Button, and one more import from this
 * package disappears. When the last one is gone, delete the package.
 */

/** Material marks its bar and dialog APIs experimental; call sites opt in. */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Material 3 marked this API experimental. The compat shim keeps the annotation so existing opt-ins still compile.",
)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalMaterial3Api

// ---------------------------------------------------------------- surfaces

/** A flat region of colour. No elevation, because there is no elevation. */
@Composable
public fun Surface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = Trial3.colors.background,
    contentColor: Color = Trial3.colors.ink,
    content: @Composable () -> Unit,
) {
    Trial3Block(
        modifier = modifier,
        filled = true,
        bordered = false,
        padding = 0.dp,
        spacing = 0.dp,
        fillColor = color,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) { content() }
    }
}

/** The colours a [Card] paints itself with. */
public class CompatCardColors internal constructor(
    public val containerColor: Color,
    public val contentColor: Color,
)

/** Material's CardDefaults, narrowed to the one factory apps actually call. */
public object CardDefaults {
    @Composable
    public fun cardColors(
        containerColor: Color = Trial3.colors.panel,
        contentColor: Color = Trial3.colors.ink,
    ): CompatCardColors = CompatCardColors(containerColor, contentColor)
}

/**
 * A panel. Bordered, never shadowed: the hairline does the work the shadow used
 * to do, which is to say it tells you where the panel ends.
 */
@Composable
public fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    colors: CompatCardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Trial3Block(
        modifier = modifier,
        filled = true,
        bordered = true,
        padding = 0.dp,
        spacing = 0.dp,
        fillColor = colors.containerColor,
        borderColor = Trial3.colors.line.copy(alpha = Alpha.panelBorder),
    ) {
        val scope = this
        CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
            scope.content()
        }
    }
}

/** A rule. One hairline, full width, the colour of the line token. */
@Composable
public fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = Stroke.hair,
    color: Color = Trial3.colors.line,
) {
    Trial3Rule(modifier = modifier, thickness = thickness, color = color)
}

// ----------------------------------------------------------------- buttons

private val ButtonPadding = PaddingValues(horizontal = Space.lg, vertical = Space.sm)

/** The one loud button on a screen: filled with the accent. */
@Composable
public fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = Trial3.colors
    val alpha = if (enabled) 1f else Alpha.disabled
    Trial3ButtonShell(
        onClick = onClick,
        contentColor = colors.background.copy(alpha = alpha),
        labelStyle = Trial3.typography.labelLarge,
        modifier = modifier,
        enabled = enabled,
        fill = colors.ink.copy(alpha = alpha),
        borderColor = colors.ink.copy(alpha = alpha),
        contentPadding = contentPadding,
        content = content,
    )
}

/** The quiet button: a border and nothing else. */
@Composable
public fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = Trial3.colors
    val alpha = if (enabled) 1f else Alpha.disabled
    Trial3ButtonShell(
        onClick = onClick,
        contentColor = colors.ink.copy(alpha = alpha),
        labelStyle = Trial3.typography.labelLarge,
        modifier = modifier,
        enabled = enabled,
        borderColor = colors.ink.copy(alpha = alpha),
        contentPadding = contentPadding,
        content = content,
    )
}

/** The quietest button: accent text, no box at all. */
@Composable
public fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = Space.md, vertical = Space.sm),
    content: @Composable RowScope.() -> Unit,
) {
    val colors = Trial3.colors
    val alpha = if (enabled) 1f else Alpha.disabled
    Trial3ButtonShell(
        onClick = onClick,
        contentColor = colors.ink.copy(alpha = alpha),
        labelStyle = Trial3.typography.labelLarge,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        content = content,
    )
}

/** A tappable square holding one mark. */
@Composable
public fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Trial3IconButtonShell(
        onClick = onClick,
        contentColor = Trial3.colors.ink.copy(alpha = if (enabled) 1f else Alpha.disabled),
        modifier = modifier,
        enabled = enabled,
        size = TouchTarget,
        content = content,
    )
}

// ------------------------------------------------------------------- marks

/**
 * Material's Icon, drawn from the Trial3 mark set.
 *
 * The vector type is [Trial3Glyph], not ImageVector: an app that reaches this shim
 * has already swapped its icon imports, and there is no Material dependency
 * left to hand a vector in.
 */
@Composable
public fun Icon(
    imageVector: Trial3Glyph,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Trial3GlyphIcon(
        glyph = imageVector,
        modifier = modifier,
        color = tint,
        size = 24.dp,
        label = contentDescription,
    )
}

// ------------------------------------------------------------------ frames

/** A bar across the top: title, an optional mark on the left, a rule below. */
@ExperimentalMaterial3Api
@Composable
public fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val nav = navigationIcon
    val acts = actions
    var leadingSlot: (@Composable RowScope.() -> Unit)? = null
    if (nav != null) leadingSlot = { nav() }
    var trailingSlot: (@Composable RowScope.() -> Unit)? = null
    if (acts != null) trailingSlot = { acts() }
    Trial3TopBarShell(
        modifier = modifier,
        leading = leadingSlot,
        trailing = trailingSlot,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Trial3.colors.ink,
            LocalTrial3TextStyle provides Trial3.typography.titleMedium,
        ) {
            title()
        }
    }
}

/** Page frame: a bar, the content, a bar. Insets are handled here, once. */
@Composable
public fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    containerColor: Color = Trial3.colors.background,
    contentColor: Color = Trial3.colors.ink,
    content: @Composable (PaddingValues) -> Unit,
) {
    Trial3Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        content(PaddingValues(horizontal = 0.dp, vertical = 0.dp))
    }
}

/** A bar across the bottom holding the tabs. */
@Composable
public fun NavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = Trial3.colors.background,
    content: @Composable RowScope.() -> Unit,
) {
    Trial3BottomBar(modifier = modifier, containerColor = containerColor, content = content)
}

/**
 * One tab. Selection is carried by the accent and by a rule under the cell,
 * never by a pill behind the label.
 */
@Composable
public fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
) {
    val alpha = if (!enabled) Alpha.disabled else if (selected) 1f else Alpha.quiet
    val lab = label
    var labelSlot: (@Composable () -> Unit)? = null
    if (lab != null && (alwaysShowLabel || selected)) labelSlot = { lab() }
    Trial3NavItemShell(
        selected = selected,
        onClick = onClick,
        contentColor = Trial3.colors.ink.copy(alpha = alpha),
        labelStyle = Trial3.typography.labelSmall,
        modifier = modifier,
        enabled = enabled,
        label = labelSlot,
        icon = icon,
    )
}

// ---------------------------------------------------------------- controls

/** A switch: a rail and a square knob that slides nowhere it cannot go. */
@Composable
public fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val callback = onCheckedChange
    Trial3Toggle(
        checked = checked,
        onCheckedChange = { value -> callback?.invoke(value) },
        modifier = modifier,
        enabled = enabled && callback != null,
    )
}

/** A radio mark. A square, because a circle would be the only one on screen. */
@Composable
public fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val callback = onClick
    Trial3Radio(
        selected = selected,
        onClick = { callback?.invoke() },
        modifier = modifier,
        enabled = enabled && callback != null,
    )
}

/**
 * A field. The label sits above the box in the quiet tone instead of floating
 * into the border, which is one animation and one clipping bug fewer.
 */
@Composable
public fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = Trial3.colors
    val lab = label
    Column(modifier = modifier) {
        if (lab != null) {
            CompositionLocalProvider(
                LocalContentColor provides colors.muted,
                LocalTrial3TextStyle provides Trial3.typography.labelSmall,
            ) {
                lab()
            }
            Spacer(Modifier.height(Space.xs))
        }
        Trial3FieldShell(
            modifier = Modifier.fillMaxWidth(),
            height = if (singleLine) 48.dp else null,
            contentPadding = PaddingValues(
                horizontal = Space.md,
                vertical = if (singleLine) 0.dp else Space.sm,
            ),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                textStyle = Trial3.typography.bodyLarge.copy(color = colors.ink),
                cursorBrush = SolidColor(colors.ink),
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun textFieldStyle(ink: Color): TextStyle {
    return Trial3.typography.bodyMedium.copy(color = ink)
}

// ---------------------------------------------------------------- feedback

/** Material's spinner, answered by the Trial3 row of cells. No rotation. */
@Composable
public fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Trial3.colors.accent,
) {
    Trial3Busy(modifier = modifier, color = color)
}

/** A dialog: a bordered panel, a rule, and the answers on one row. */
@ExperimentalMaterial3Api
@Composable
public fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
) {
    val colors = Trial3.colors
    val leadIcon = icon
    val heading = title
    val bodyText = text
    val dismiss = dismissButton
    Trial3DialogShell(onDismiss = onDismissRequest, modifier = modifier) {
        if (leadIcon != null) {
            CompositionLocalProvider(LocalContentColor provides colors.accent) {
                leadIcon()
            }
        }
        if (heading != null) {
            CompositionLocalProvider(
                LocalContentColor provides colors.ink,
                LocalTrial3TextStyle provides Trial3.typography.titleMedium,
            ) {
                heading()
            }
        }
        if (bodyText != null) {
            CompositionLocalProvider(
                LocalContentColor provides colors.muted,
                LocalTrial3TextStyle provides Trial3.typography.bodyMedium,
            ) {
                bodyText()
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (dismiss != null) {
                dismiss()
                Spacer(Modifier.width(Space.sm))
            }
            confirmButton()
        }
    }
}
