# trial3lib

A Compose design system for Android that is not Material.

No elevation, no ripple, no rounded corners, no tonal surfaces. Panels are
separated by a one-pixel line instead of a shadow, controls are rectangles, and
every colour on screen comes from one of twelve hand-authored palettes that each
ship a dark and a light lighting. The look is carried by geometry and contrast,
so it stays legible at the two extremes Material blurs: pure black on OLED and
paper white in sunlight.

## Adding it

```kotlin
// settings.gradle.kts
include(":trial3lib")

// app/build.gradle.kts
dependencies {
    implementation(project(":trial3lib"))
}
```

Then wrap the app once:

```kotlin
setContent {
    Trial3Theme(palette = trial3PaletteSpec("ink")) {
        Trial3Scaffold(topBar = { Trial3TopBar(title = "Today") }) { padding ->
            Trial3Block(modifier = Modifier.padding(padding)) {
                Trial3Text("Hello")
                Trial3Button(label = "Continue", onClick = { })
            }
        }
    }
}
```

The library depends on `compose.foundation` and nothing else. Material 3 is not
a dependency, and a unit test asserts that its classes are not even loadable, so
an accidental import cannot quietly bring the rounded geometry back.

## Tokens

Everything reads from `Trial3`, which is only valid inside `Trial3Theme`.

| Token | What it is |
| --- | --- |
| `Trial3.colors.background` | The page. |
| `Trial3.colors.panel` | A region lifted off the page, mixed 7% toward the ink. |
| `Trial3.colors.ink` | Text and controls at full strength. |
| `Trial3.colors.muted` | Secondary text. Never below 4.5:1 on the background. |
| `Trial3.colors.line` | The hairline that does the work a shadow used to do. |
| `Trial3.colors.accent` | The one loud colour. One per screen, ideally. |
| `Trial3.colors.onAccent` | What stays legible on the accent. |
| `Trial3.colors.danger` | Destructive only, hue-guarded away from the accent. |
| `Trial3.typography` | Fifteen slots, each with a line height. |
| `Trial3.palette` | The palette in force, including both lightings. |
| `Trial3.motionEnabled` | False when the system asks for less motion. |
| `Space`, `Stroke`, `Alpha`, `Motion` | Spacing, hairlines, opacities, durations. |

Sizes are named, not typed at call sites: `Space.sm` is 8dp everywhere or it is
nowhere. `Trial3Shape.square` exists so that the absence of a corner radius is
something the code says out loud.

## Palettes

Twelve palettes, each authored twice rather than derived, because a light theme
computed by inverting a dark one comes out brown. `PaletteContrastTest` asserts
every one of them readable in both lightings, so a palette that fails contrast
breaks the build rather than the reader.

Names are English in the library. An app that is translated installs a namer and
keeps its own wording:

```kotlin
CompositionLocalProvider(
    LocalTrial3PaletteNamer provides { id -> translations[id] },
) { /* pickers now read in the reader own language */ }
```

Returning null for an unknown id is normal, not an error: the English name is
used, so adding a palette here never breaks a translated app.

## The Material shim

`dev.trial3lib.ui.compat` exists for one reason: an app with hundreds of
`Text(...)`, `Card { }` and `Scaffold { }` call sites can change its imports and
nothing else. Every function in it has Material signature and draws nothing
itself, forwarding to the real component instead, so a screen that has been
migrated and a screen that has not look identical on the same day.

The shim is a migration aid, not an API. `tools/check-design.py` counts the names
an app still imports from it, which is the number that should only go down.

## Checks

None of these need an Android SDK, so they answer in seconds:

```
python3 tools/check-design.py      geometry stays out of the shim, no circles
python3 tools/check-tests.py       tests match real signatures
python3 tools/dump-api.py --check  the public surface is the reviewed one
```

The public surface lives in `api/trial3lib.api`, 245 declarations. Renaming one
of them is a broken build in somebody else repository, so the diff has to be
visible. Regenerate it with `python3 tools/dump-api.py`.
