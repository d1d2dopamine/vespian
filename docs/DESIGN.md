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


## Round four: one real library defect and three loose wires in the app

The library itself compiled. What failed was a test of it, and then the app.

### The library defect: three type slots had no line height

`labelLarge`, `labelMedium` and `labelSmall` set a font size and never set a
line height. An unset `lineHeight` is `TextUnit.Unspecified`, whose `.value` is
NaN, and `NaN >= fontSize` is false -- which is how the test caught it.

The test was right and the scale was wrong. A slot without a line height leans
on whatever leading the font file carries, so the same label sits at one height
in the default face and at another as soon as a user picks a font in settings.
That is exactly the kind of drift a design system exists to remove. The three
labels now read 13/18, 11/16 and 10/14, near the 1.4 ratio the body slots use.

Nothing clips: the tallest label lives in `LatNavItem`, a 20dp mark plus 4dp
plus a 14sp line plus 4dp plus a 2dp underline, inside a 56dp bar.

### App wire 1: the icon type

The migration rewrote imports but not type names, and
`androidx.compose.ui.graphics.vector.ImageVector` is still on the classpath --
`ui-graphics` is a dependency -- so `NavItem(icon: ImageVector)` and
`Step(icon: ImageVector)` compiled fine on their own and only failed where a
`LatGlyph` met them. Thirteen errors, one cause. Both signatures now name
`LatGlyph`.

A missing import is loud. A type that still resolves but no longer means
anything in this module is quiet, which is why this needed a checker rather
than a grep for "material".

### App wire 2: BuildConfig

AGP 8 generates `BuildConfig` only when `buildFeatures.buildConfig` is true, and
the build file this module inherited -- Ikna's -- never asked, because Ikna has
no about screen. Vespian reads five fields from it, twice over. Two of those
fields, `GIT_SHA` and `BUILD_AT`, are not AGP's to generate; they are declared
with `buildConfigField` now. `BUILD_AT` is cut to the minute in UTC so two
builds of one commit still hit the build cache.

### App wire 3: a constant that is not in the pinned client

`HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY` does not exist in
connect-client 1.1.0-alpha07; `PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND`, one
line above it, does. Bumping the artifact to guess which alpha added it would
be a second guess on top of the first. The value is a platform permission name,
the same string the manifest already declares, so `HealthRepo` writes it out
and the code stops caring which alpha is resolved.

### tools/check-app-wiring.py

Six checks, one per bug this round or an earlier one could produce:

- **A** types the app can no longer produce, named in app declarations
- **B** `BuildConfig` fields read but not generated or declared
- **C** a type slot with no line height, or one below its font size -- the unit
  test, run here instead of in CI
- **D** constants read off a third-party class the pinned version may not carry
- **E** compat icon names used by the app and absent from `M3Icons`
- **F** `R.string` names with no entry in the default `strings.xml`

Current state: 15 type slots, 30 icons, 463 string names, no failures.
