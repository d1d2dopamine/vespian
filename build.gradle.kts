// Plugin versions live here and only here. Modules apply the ids without a
// version, which is what makes app/ and trial3lib/ resolve the same AGP and the
// same Kotlin compiler -- two versions of either in one build is a hard failure.
//
// This replaces the version catalog the build scripts used to reference. There
// is no gradle/libs.versions.toml in this repository, and a missing catalog
// fails the build script itself ("Unresolved reference: libs") before any
// Kotlin is compiled.
plugins {
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
