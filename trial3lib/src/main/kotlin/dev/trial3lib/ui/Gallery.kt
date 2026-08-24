package dev.trial3lib.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.trial3lib.ui.component.Trial3Block
import dev.trial3lib.ui.component.Trial3Busy
import dev.trial3lib.ui.component.Trial3Button
import dev.trial3lib.ui.component.Trial3Check
import dev.trial3lib.ui.component.Trial3Chip
import dev.trial3lib.ui.component.Trial3Dialog
import dev.trial3lib.ui.component.Trial3Figure
import dev.trial3lib.ui.component.Trial3HexField
import dev.trial3lib.ui.component.Trial3LatticePlaceholder
import dev.trial3lib.ui.component.Trial3Notice
import dev.trial3lib.ui.component.Trial3Panel
import dev.trial3lib.ui.component.Trial3Progress
import dev.trial3lib.ui.component.Trial3Row
import dev.trial3lib.ui.component.Trial3Rule
import dev.trial3lib.ui.component.Trial3Scaffold
import dev.trial3lib.ui.component.Trial3SectionLabel
import dev.trial3lib.ui.component.Trial3Segmented
import dev.trial3lib.ui.component.Trial3Slider
import dev.trial3lib.ui.component.Trial3Stepper
import dev.trial3lib.ui.component.Trial3Swatch
import dev.trial3lib.ui.component.Trial3Tabs
import dev.trial3lib.ui.component.Trial3Text
import dev.trial3lib.ui.component.Trial3TextButton
import dev.trial3lib.ui.component.Trial3TextField
import dev.trial3lib.ui.component.Trial3Toggle
import dev.trial3lib.ui.component.Trial3TopBar
import dev.trial3lib.ui.component.Trial3WideButton
import dev.trial3lib.ui.graphic.Trial3Glyph
import dev.trial3lib.ui.graphic.Trial3GlyphIcon
import dev.trial3lib.ui.graphic.Trial3MemoryField
import dev.trial3lib.ui.graphic.Trial3Wordmark
import dev.trial3lib.ui.token.Edge
import dev.trial3lib.ui.token.Space
import dev.trial3lib.ui.token.Trial3Palettes
import dev.trial3lib.ui.token.displayName

/**
 * Every component in the library on one screen.
 *
 * Point a debug entry at this before adopting the kit: it is the fastest way to
 * see a palette in use, and the fastest way to notice that a control looks wrong
 * in one of the twelve. It is also the reference for how the pieces are meant to
 * be composed -- a section label, a rule, and content, with no card anywhere.
 */
@Composable
public fun Trial3Gallery(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    var toggle by remember { mutableStateOf(true) }
    var checked by remember { mutableStateOf(false) }
    var chip by remember { mutableStateOf(1) }
    var tab by remember { mutableStateOf(0) }
    var slider by remember { mutableStateOf(0.42f) }
    var count by remember { mutableStateOf(3) }
    var field by remember { mutableStateOf("") }
    var hex by remember { mutableStateOf("#0B1120") }
    var dialog by remember { mutableStateOf(false) }

    Trial3Scaffold(
        modifier = modifier,
        topBar = {
            Trial3TopBar(
                title = "Trial3",
                subtitle = "${Trial3Palettes.size} palettes, no material",
                trailing = {
                    if (onBack != null) {
                        Trial3TextButton(label = "CLOSE", onClick = onBack)
                    }
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = Edge,
                vertical = Space.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            item {
                Trial3Wordmark(text = "trial3lib")
            }
            item {
                Trial3SectionLabel("TYPE")
                Trial3Text("Display small", style = Trial3.typography.displaySmall)
                Trial3Text("Body large, the size a sentence is read at.", style = Trial3.typography.bodyLarge)
                Trial3Text("Body small, for the second line.", style = Trial3.typography.bodySmall, color = Trial3.colors.muted)
            }
            item {
                Trial3SectionLabel("FIGURES")
                Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
                    Trial3Figure(value = "128", caption = "CARDS")
                    Trial3Figure(value = "6.4H", caption = "SLEEP")
                    Trial3Figure(value = "91%", caption = "RECALL", color = Trial3.colors.accent)
                }
            }
            item {
                Trial3SectionLabel("FIELD")
                Trial3MemoryField(fraction = slider)
            }
            item {
                Trial3SectionLabel("PROGRESS")
                Trial3Progress(fraction = slider)
                Spacer(Modifier.height(Space.md))
                Trial3Busy()
                Spacer(Modifier.height(Space.md))
                Trial3LatticePlaceholder()
            }
            item {
                Trial3SectionLabel("MARKS")
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    Trial3Glyph.entries.take(9).forEach { glyph ->
                        Trial3GlyphIcon(glyph = glyph, color = Trial3.colors.ink, size = 22.dp)
                    }
                }
                Spacer(Modifier.height(Space.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    Trial3Glyph.entries.drop(22).take(9).forEach { glyph ->
                        Trial3GlyphIcon(glyph = glyph, color = Trial3.colors.accent, size = 22.dp)
                    }
                }
            }
            item {
                Trial3SectionLabel("BUTTONS")
                Trial3WideButton(label = "PRIMARY", onClick = {}, filled = true)
                Spacer(Modifier.height(Space.sm))
                Trial3WideButton(label = "SECONDARY", onClick = {})
                Spacer(Modifier.height(Space.sm))
                Trial3WideButton(label = "QUIET", onClick = {}, quiet = true)
                Spacer(Modifier.height(Space.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Trial3Button(label = "OK", onClick = {}, filled = true)
                    Trial3Button(label = "CANCEL", onClick = {})
                    Trial3Button(label = "ERASE", onClick = { dialog = true }, danger = true)
                }
            }
            item {
                Trial3SectionLabel("CONTROLS")
                Trial3Row(
                    title = "Animations",
                    subtitle = "Everything settles instead of bouncing",
                    trailing = { Trial3Toggle(checked = toggle, onCheckedChange = { toggle = it }, label = "Animations") },
                )
                Trial3Rule()
                Trial3Row(
                    title = "Remind me",
                    trailing = { Trial3Check(checked = checked, onCheckedChange = { checked = it }, label = "Remind me") },
                )
                Trial3Rule()
                Spacer(Modifier.height(Space.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    listOf("ALL", "DUE", "NEW").forEachIndexed { index, label ->
                        Trial3Chip(label = label, selected = chip == index, onClick = { chip = index })
                    }
                }
                Spacer(Modifier.height(Space.md))
                Trial3Segmented(
                    options = listOf("DARK", "LIGHT", "SYSTEM"),
                    selectedIndex = tab,
                    onSelect = { tab = it },
                )
                Spacer(Modifier.height(Space.md))
                Trial3Slider(value = slider, onValueChange = { slider = it }, steps = 9, label = "Target")
                Trial3Stepper(value = count, onValueChange = { count = it }, range = 1..12)
            }
            item {
                Trial3SectionLabel("FIELDS")
                Trial3TextField(
                    value = field,
                    onValueChange = { field = it },
                    placeholder = "Deck name",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Space.sm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Trial3Swatch(color = Trial3.colors.accent)
                    Trial3HexField(value = hex, onValueChange = { hex = it }, modifier = Modifier.fillMaxWidth())
                }
            }
            item {
                Trial3SectionLabel("TABS")
                Trial3Tabs(tabs = listOf("TODAY", "DRIFT", "MODEL"), selectedIndex = tab.coerceIn(0, 2), onSelect = { tab = it })
            }
            item {
                Trial3SectionLabel("BLOCKS")
                Trial3Panel {
                    Trial3Text(
                        "A panel is for what the app is telling you. Its border is heavier " +
                            "than a rule so the explaining is visibly separate from the choosing.",
                        style = Trial3.typography.bodyMedium,
                        color = Trial3.colors.muted,
                    )
                }
                Spacer(Modifier.height(Space.md))
                Trial3Block(onClick = {}) {
                    Trial3Text("A block. What a card would be, if a card were allowed.", style = Trial3.typography.bodyMedium)
                }
                Spacer(Modifier.height(Space.md))
                Trial3Notice(
                    text = "Could not reach the server. Your answers are stored on the device.",
                    danger = true,
                    actionLabel = "RETRY",
                    onAction = {},
                )
            }
            item {
                Trial3SectionLabel("PALETTES")
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    Trial3Palettes.forEach { spec ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Space.xs),
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Trial3Swatch(color = spec.dark.background, size = 20.dp)
                            Trial3Swatch(color = spec.dark.ink, size = 20.dp)
                            Trial3Swatch(color = spec.dark.muted, size = 20.dp)
                            Trial3Swatch(color = spec.dark.accent, size = 20.dp)
                            Trial3Text(
                                text = spec.displayName(),
                                style = Trial3.typography.labelMedium,
                                color = Trial3.colors.muted,
                            )
                        }
                    }
                }
            }
        }
    }

    if (dialog) {
        Trial3Dialog(
            title = "Erase everything?",
            body = "Every deck, every answer and every setting on this device. There is no undo.",
            confirmLabel = "ERASE",
            onConfirm = { dialog = false },
            dismissLabel = "KEEP",
            onDismiss = { dialog = false },
            confirmColor = Trial3.colors.danger,
        )
    }
}
