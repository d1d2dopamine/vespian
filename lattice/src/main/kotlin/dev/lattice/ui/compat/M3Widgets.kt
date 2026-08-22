package dev.lattice.ui.compat

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
import dev.lattice.ui.Lattice
import dev.lattice.ui.LocalContentColor
import dev.lattice.ui.LocalLatTextStyle
import dev.lattice.ui.component.LatBusy
import dev.lattice.ui.graphic.LatGlyph
import dev.lattice.ui.graphic.LatGlyphIcon
import dev.lattice.ui.token.Alpha
import dev.lattice.ui.token.BarHeight
import dev.lattice.ui.token.Space
import dev.lattice.ui.token.Stroke
import dev.lattice.ui.token.TouchTarget

/*
 * The rest of the bridge: the widgets.
 *
 * Material3Compat.kt answers the theme and the text; this file answers the
 * components an app actually places on a screen. Same rule as the theme half --
 * the names are Material's, every pixel is Lattice's. Nothing is raised off the
 * page, nothing is rounded, and the only colour is the accent.
 *
 * These are shims, not components. When a screen is rewritten, its Card becomes
 * a LatPanel and its Button becomes a LatButton, and one more import from this
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
    color: Color = Lattice.colors.background,
    contentColor: Color = Lattice.colors.ink,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.background(color, shape)) {
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
        containerColor: Color = Lattice.colors.panel,
        contentColor: Color = Lattice.colors.ink,
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
    Column(
        modifier = modifier
            .background(colors.containerColor, shape)
            .border(Stroke.hair, Lattice.colors.line.copy(alpha = Alpha.panelBorder), shape),
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
    color: Color = Lattice.colors.line,
) {
    Box(modifier = modifier.fillMaxWidth().height(thickness).background(color))
}

// ----------------------------------------------------------------- buttons

@Composable
private fun CompatButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    fill: Color,
    ink: Color,
    borderColor: Color?,
    contentPadding: PaddingValues,
    content: @Composable RowScope.() -> Unit,
) {
    val shown = if (enabled) ink else ink.copy(alpha = Lattice.disabledAlpha)
    var shape: Modifier = Modifier.background(fill, RectangleShape)
    if (borderColor != null) shape = shape.border(Stroke.hair, borderColor, RectangleShape)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = TouchTarget)
            .then(shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val scope = this
        CompositionLocalProvider(
            LocalContentColor provides shown,
            LocalLatTextStyle provides Lattice.typography.labelLarge,
        ) {
            scope.content()
        }
    }
}

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
    CompatButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        fill = if (enabled) Lattice.colors.accent else Lattice.colors.panel,
        ink = if (enabled) Lattice.colors.onAccent else Lattice.colors.muted,
        borderColor = null,
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
    CompatButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        fill = Color.Transparent,
        ink = Lattice.colors.ink,
        borderColor = Lattice.colors.line.copy(alpha = Alpha.border),
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
    CompatButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        fill = Color.Transparent,
        ink = Lattice.colors.accent,
        borderColor = null,
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
    Box(
        modifier = modifier
            .size(TouchTarget)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val ink = Lattice.colors.ink
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) ink else ink.copy(alpha = Lattice.disabledAlpha),
        ) { content() }
    }
}

// ------------------------------------------------------------------- marks

/**
 * Material's Icon, drawn from the Lattice mark set.
 *
 * The vector type is [LatGlyph], not ImageVector: an app that reaches this shim
 * has already swapped its icon imports, and there is no Material dependency
 * left to hand a vector in.
 */
@Composable
public fun Icon(
    imageVector: LatGlyph,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    LatGlyphIcon(
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
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth().background(Lattice.colors.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(BarHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon()
            Spacer(Modifier.width(Space.md))
            Box(modifier = Modifier.weight(1f)) {
                CompositionLocalProvider(
                    LocalLatTextStyle provides Lattice.typography.titleMedium,
                    LocalContentColor provides Lattice.colors.ink,
                ) { title() }
            }
            val scope = this
            CompositionLocalProvider(LocalContentColor provides Lattice.colors.ink) {
                scope.actions()
            }
        }
        HorizontalDivider()
    }
}

/** Page frame: a bar, the content, a bar. Insets are handled here, once. */
@Composable
public fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    containerColor: Color = Lattice.colors.background,
    contentColor: Color = Lattice.colors.ink,
    content: @Composable (PaddingValues) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor)
            .systemBarsPadding(),
    ) {
        topBar()
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content(PaddingValues(0.dp))
            }
        }
        bottomBar()
    }
}

/** A bar across the bottom holding the tabs. */
@Composable
public fun NavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = Lattice.colors.background,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth().background(containerColor)) {
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().height(BarHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
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
    val tint = when {
        !enabled -> Lattice.colors.muted.copy(alpha = Lattice.disabledAlpha)
        selected -> Lattice.colors.accent
        else -> Lattice.colors.muted
    }
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxSize()
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides tint,
            LocalLatTextStyle provides Lattice.typography.labelSmall,
        ) {
            icon()
            if (label != null && (alwaysShowLabel || selected)) {
                Spacer(Modifier.height(Space.hair))
                label()
            }
        }
        Spacer(Modifier.height(Space.xs))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Stroke.heavy)
                .background(if (selected) Lattice.colors.accent else Color.Transparent),
        )
    }
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
    val on = Lattice.colors.accent
    val off = Lattice.colors.ink.copy(alpha = Alpha.trackOff)
    val track = if (checked) on else off
    Box(
        modifier = modifier
            .size(width = 48.dp, height = TouchTarget)
            .clickable(
                enabled = enabled && onCheckedChange != null,
                onClick = { onCheckedChange?.invoke(!checked) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 20.dp)
                .background(track.copy(alpha = if (enabled) track.alpha else Alpha.disabled))
                .border(Stroke.hair, Lattice.colors.line, RectangleShape),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 18.dp)
                    .padding(1.dp)
                    .background(if (checked) Lattice.colors.onAccent else Lattice.colors.ink),
            )
        }
    }
}

/** A radio mark. A square, because a circle would be the only one on screen. */
@Composable
public fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(TouchTarget)
            .clickable(enabled = enabled && onClick != null, onClick = { onClick?.invoke() }),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(
                    Stroke.hair,
                    if (selected) Lattice.colors.accent else Lattice.colors.line,
                    RectangleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(modifier = Modifier.size(10.dp).background(Lattice.colors.accent))
            }
        }
    }
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
    Column(modifier = modifier) {
        if (label != null) {
            CompositionLocalProvider(
                LocalContentColor provides Lattice.colors.muted,
                LocalLatTextStyle provides Lattice.typography.labelSmall,
            ) { label() }
            Spacer(Modifier.height(Space.xs))
        }
        val ink = if (enabled) Lattice.colors.ink else Lattice.colors.muted
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(Stroke.hair, Lattice.colors.line.copy(alpha = Alpha.border), RectangleShape)
                .padding(horizontal = Space.md, vertical = Space.md),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textFieldStyle(ink),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(Lattice.colors.accent),
        )
    }
}

@Composable
private fun textFieldStyle(ink: Color): TextStyle {
    return Lattice.typography.bodyMedium.copy(color = ink)
}

// ---------------------------------------------------------------- feedback

/** Material's spinner, answered by the Lattice row of cells. No rotation. */
@Composable
public fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Lattice.colors.accent,
) {
    LatBusy(modifier = modifier, color = color)
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
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Lattice.colors.panel)
                .border(Stroke.hair, Lattice.colors.line, RectangleShape)
                .padding(Space.lg),
        ) {
            if (icon != null) {
                CompositionLocalProvider(LocalContentColor provides Lattice.colors.accent) {
                    icon()
                }
                Spacer(Modifier.height(Space.md))
            }
            if (title != null) {
                CompositionLocalProvider(
                    LocalContentColor provides Lattice.colors.ink,
                    LocalLatTextStyle provides Lattice.typography.titleMedium,
                ) { title() }
                Spacer(Modifier.height(Space.sm))
            }
            if (text != null) {
                CompositionLocalProvider(
                    LocalContentColor provides Lattice.colors.muted,
                    LocalLatTextStyle provides Lattice.typography.bodyMedium,
                ) { text() }
                Spacer(Modifier.height(Space.lg))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (dismissButton != null) {
                    dismissButton()
                    Spacer(Modifier.width(Space.sm))
                }
                confirmButton()
            }
        }
    }
}
