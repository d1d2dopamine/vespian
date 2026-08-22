package dev.lattice.ui

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
import dev.lattice.ui.component.LatBlock
import dev.lattice.ui.component.LatBusy
import dev.lattice.ui.component.LatButton
import dev.lattice.ui.component.LatCheck
import dev.lattice.ui.component.LatChip
import dev.lattice.ui.component.LatDialog
import dev.lattice.ui.component.LatFigure
import dev.lattice.ui.component.LatHexField
import dev.lattice.ui.component.LatLatticePlaceholder
import dev.lattice.ui.component.LatNotice
import dev.lattice.ui.component.LatPanel
import dev.lattice.ui.component.LatProgress
import dev.lattice.ui.component.LatRow
import dev.lattice.ui.component.LatRule
import dev.lattice.ui.component.LatScaffold
import dev.lattice.ui.component.LatSectionLabel
import dev.lattice.ui.component.LatSegmented
import dev.lattice.ui.component.LatSlider
import dev.lattice.ui.component.LatStepper
import dev.lattice.ui.component.LatSwatch
import dev.lattice.ui.component.LatTabs
import dev.lattice.ui.component.LatText
import dev.lattice.ui.component.LatTextButton
import dev.lattice.ui.component.LatTextField
import dev.lattice.ui.component.LatToggle
import dev.lattice.ui.component.LatTopBar
import dev.lattice.ui.component.LatWideButton
import dev.lattice.ui.graphic.LatGlyph
import dev.lattice.ui.graphic.LatGlyphIcon
import dev.lattice.ui.graphic.LatMemoryField
import dev.lattice.ui.graphic.LatWordmark
import dev.lattice.ui.token.Edge
import dev.lattice.ui.token.LatPalettes
import dev.lattice.ui.token.Space

/**
 * Every component in the library on one screen.
 *
 * Point a debug entry at this before adopting the kit: it is the fastest way to
 * see a palette in use, and the fastest way to notice that a control looks wrong
 * in one of the twelve. It is also the reference for how the pieces are meant to
 * be composed -- a section label, a rule, and content, with no card anywhere.
 */
@Composable
public fun LatticeGallery(
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

    LatScaffold(
        modifier = modifier,
        topBar = {
            LatTopBar(
                title = "Lattice",
                subtitle = "${LatPalettes.size} palettes, no material",
                trailing = {
                    if (onBack != null) {
                        LatTextButton(label = "CLOSE", onClick = onBack)
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
                LatWordmark(text = "lattice")
            }
            item {
                LatSectionLabel("TYPE")
                LatText("Display small", style = Lattice.typography.displaySmall)
                LatText("Body large, the size a sentence is read at.", style = Lattice.typography.bodyLarge)
                LatText("Body small, for the second line.", style = Lattice.typography.bodySmall, color = Lattice.colors.muted)
            }
            item {
                LatSectionLabel("FIGURES")
                Row(horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
                    LatFigure(value = "128", caption = "CARDS")
                    LatFigure(value = "6.4H", caption = "SLEEP")
                    LatFigure(value = "91%", caption = "RECALL", color = Lattice.colors.accent)
                }
            }
            item {
                LatSectionLabel("FIELD")
                LatMemoryField(fraction = slider)
            }
            item {
                LatSectionLabel("PROGRESS")
                LatProgress(fraction = slider)
                Spacer(Modifier.height(Space.md))
                LatBusy()
                Spacer(Modifier.height(Space.md))
                LatLatticePlaceholder()
            }
            item {
                LatSectionLabel("MARKS")
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    LatGlyph.entries.take(9).forEach { glyph ->
                        LatGlyphIcon(glyph = glyph, color = Lattice.colors.ink, size = 22.dp)
                    }
                }
                Spacer(Modifier.height(Space.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    LatGlyph.entries.drop(22).take(9).forEach { glyph ->
                        LatGlyphIcon(glyph = glyph, color = Lattice.colors.accent, size = 22.dp)
                    }
                }
            }
            item {
                LatSectionLabel("BUTTONS")
                LatWideButton(label = "PRIMARY", onClick = {}, filled = true)
                Spacer(Modifier.height(Space.sm))
                LatWideButton(label = "SECONDARY", onClick = {})
                Spacer(Modifier.height(Space.sm))
                LatWideButton(label = "QUIET", onClick = {}, quiet = true)
                Spacer(Modifier.height(Space.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    LatButton(label = "OK", onClick = {}, filled = true)
                    LatButton(label = "CANCEL", onClick = {})
                    LatButton(label = "ERASE", onClick = { dialog = true }, danger = true)
                }
            }
            item {
                LatSectionLabel("CONTROLS")
                LatRow(
                    title = "Animations",
                    subtitle = "Everything settles instead of bouncing",
                    trailing = { LatToggle(checked = toggle, onCheckedChange = { toggle = it }, label = "Animations") },
                )
                LatRule()
                LatRow(
                    title = "Remind me",
                    trailing = { LatCheck(checked = checked, onCheckedChange = { checked = it }, label = "Remind me") },
                )
                LatRule()
                Spacer(Modifier.height(Space.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    listOf("ALL", "DUE", "NEW").forEachIndexed { index, label ->
                        LatChip(label = label, selected = chip == index, onClick = { chip = index })
                    }
                }
                Spacer(Modifier.height(Space.md))
                LatSegmented(
                    options = listOf("DARK", "LIGHT", "SYSTEM"),
                    selectedIndex = tab,
                    onSelect = { tab = it },
                )
                Spacer(Modifier.height(Space.md))
                LatSlider(value = slider, onValueChange = { slider = it }, steps = 9, label = "Target")
                LatStepper(value = count, onValueChange = { count = it }, range = 1..12)
            }
            item {
                LatSectionLabel("FIELDS")
                LatTextField(
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
                    LatSwatch(color = Lattice.colors.accent)
                    LatHexField(value = hex, onValueChange = { hex = it }, modifier = Modifier.fillMaxWidth())
                }
            }
            item {
                LatSectionLabel("TABS")
                LatTabs(tabs = listOf("TODAY", "DRIFT", "MODEL"), selectedIndex = tab.coerceIn(0, 2), onSelect = { tab = it })
            }
            item {
                LatSectionLabel("BLOCKS")
                LatPanel {
                    LatText(
                        "A panel is for what the app is telling you. Its border is heavier " +
                            "than a rule so the explaining is visibly separate from the choosing.",
                        style = Lattice.typography.bodyMedium,
                        color = Lattice.colors.muted,
                    )
                }
                Spacer(Modifier.height(Space.md))
                LatBlock(onClick = {}) {
                    LatText("A block. What a card would be, if a card were allowed.", style = Lattice.typography.bodyMedium)
                }
                Spacer(Modifier.height(Space.md))
                LatNotice(
                    text = "Could not reach the server. Your answers are stored on the device.",
                    danger = true,
                    actionLabel = "RETRY",
                    onAction = {},
                )
            }
            item {
                LatSectionLabel("PALETTES")
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    LatPalettes.forEach { spec ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Space.xs),
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            LatSwatch(color = spec.dark.background, size = 20.dp)
                            LatSwatch(color = spec.dark.ink, size = 20.dp)
                            LatSwatch(color = spec.dark.muted, size = 20.dp)
                            LatSwatch(color = spec.dark.accent, size = 20.dp)
                            LatText(text = spec.id, style = Lattice.typography.labelMedium, color = Lattice.colors.muted)
                        }
                    }
                }
            }
        }
    }

    if (dialog) {
        LatDialog(
            title = "Erase everything?",
            body = "Every deck, every answer and every setting on this device. There is no undo.",
            confirmLabel = "ERASE",
            onConfirm = { dialog = false },
            dismissLabel = "KEEP",
            onDismiss = { dialog = false },
            confirmColor = Lattice.colors.danger,
        )
    }
}
