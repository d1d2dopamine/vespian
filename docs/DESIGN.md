# Interface

Vespian's screens are drawn by `:lattice` -- Ikna's design system, extracted into
its own Android library module. There is no `androidx.compose.material3` and no
icon artifact on the classpath; a test in the library (`NoMaterialDependencyTest`)
and a grep in CI keep it that way.

## What changed in this app

| Before | Now |
| --- | --- |
| `MaterialTheme(colorScheme = NightScheme, ...)` | `LatticeTheme(palette = NightPalette, ...)` |
| ~30 colour slots filled in by hand | four colours; panel tone and hairline are derived |
| `Typography(...)` with 5 slots set | Lattice's 15-slot scale, applied all at once |
| `androidx.compose.material3.*` imports | `dev.lattice.ui.compat.*` imports |
| `Icons.Filled.Alarm` (icon artifact) | `Icons.Filled.Alarm` (a mark drawn from lines) |

`ui/Theme.kt` keeps the same `VespianTheme(dark, content)` signature, so not one
screen call site changed. The previous file is kept beside it as
`ui/Theme.kt.pre-lattice`.

## The palette

| Slot | Night | Day |
| --- | --- | --- |
| background | `#0A1018` (Ink) | `#F6F8FC` |
| ink | `#E4EAF2` (Mist) | `#10161F` |
| muted | `#97A6BA` (MistDim) | `#4A5769` |
| accent | `#4FD1C5` (Teal) | `#00695C` |

`panel` is the background mixed 7% toward the ink, `line` 28%: a panel is always
exactly one step off the page in both lightings and cannot drift. `Slate` and
`SlateHi` are therefore no longer wired into the theme, but the constants stay in
`ui/Theme.kt` because `ui/Plots.kt` and `ui/Ring.kt` draw with Canvas and pick
their own colours.

## The compat package

`dev.lattice.ui.compat` answers Material's names -- `MaterialTheme.colorScheme`,
`Text`, `Card`, `Scaffold`, `TopAppBar`, `NavigationBar`, `Switch`,
`OutlinedTextField`, `AlertDialog`, `Icons.Filled.*` -- with Lattice pixels. It is
scaffolding, not architecture: when a screen is rewritten, its `Card` becomes a
`LatPanel` and its `Button` a `LatButton`, and one more import from that package
disappears. When the last one is gone, delete the package.

What this means visually: nothing is raised off the page (no elevation), nothing
is rounded (every shape is a rectangle), a rule separates and a border encloses,
and the accent is the only colour on screen.

## Marks

The 30 Material icons this app used map onto Lattice marks; where Material had a
mark Lattice does not draw, the nearest honest one is used and said so in a
comment in `M3Icons.kt`. `Refresh` and `Sync` are the same two arrows, and `Error`
borrows the warning triangle.

## Two compile errors that got through the first time

The sandbox this was assembled in has no Android SDK, so the first pass was
checked by parsing rather than by compiling, and two things only a compiler
catches got through:

1. `Material3Compat.kt` -- the compat `Text` takes `style: TextStyle?` (null
   means "inherit whatever the surrounding block set"), and passed it straight
   into `LatText`, whose `style` is not nullable. Now resolved at the boundary:
   `style = style ?: LocalLatTextStyle.current`.
2. `LatControls.kt` -- `role` inside a `semantics { }` block is an extension
   property, so it needs `import androidx.compose.ui.semantics.role` of its own,
   next to `Role`. `LatBars.kt` and `LatButtons.kt` had it; this file did not.

Both were in `:lattice`, not in the app, and both are the same shape: a symbol
that looks resolved because a neighbouring name is imported.

## And two more, in the tests rather than the library

After the main source compiled, `:lattice:compileReleaseUnitTestKotlin` failed on
two of the library's own tests. Both were tests written against an API I had
remembered rather than read:

- `PaletteContrastTest` called `dangerFor(palette.background)`, but `dangerFor`
  takes the whole `LatPalette` -- it has to, because when the accent is itself
  red it returns the ink colour instead of a red that would be indistinguishable
  from an ordinary control. It also passed raw `Int` hex literals to
  `clashesWithDanger`, which takes a `Color`.
- `TypeScaleTest` treated `LatTypography.all()` as a list of `TextStyle`, but it
  returns each style paired with its slot name. Now destructured, so a failure
  names the slot that broke rather than only counting how many did.

The tests are checked against the real declarations by `tools/check-tests.py`,
which indexes every signature in `src/main` and every data class member, then
re-reads each call in `src/test` against them.
