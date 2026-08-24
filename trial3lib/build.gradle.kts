// ---------------------------------------------------------------------------
// Trial3: the design system as its own Android library module.
//
// Plugin ids are applied without versions: the root build.gradle.kts pins every
// version once, for both modules. That is deliberate -- AGP and the Kotlin
// compiler plugin are resolved once for the whole build, so app/ and trial3lib/
// cannot disagree about them, and no version catalog has to exist for the build
// scripts to compile.
//
// What is deliberately absent: androidx.compose.material3, androidx.compose.
// material, and any icon font. That absence is the whole point of this module,
// and it is enforced by a test in this project (NoMaterialDependencyTest) plus
// a grep in CI.
// ---------------------------------------------------------------------------
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "dev.trial3lib.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    sourceSets["main"].java.srcDirs("src/main/kotlin")
    sourceSets["test"].java.srcDirs("src/test/kotlin")

    testOptions {
        unitTests {
            // The tests in this module are pure arithmetic over colours and
            // fractions; Android calls return defaults instead of throwing.
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // api, not implementation: a consumer writing a screen needs Modifier,
    // Color and TextStyle in its own source, and having to declare Compose
    // twice is how two Compose versions end up in one build.
    api(platform("androidx.compose:compose-bom:2024.09.03"))
    api("androidx.compose.runtime:runtime")
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-graphics")
    api("androidx.compose.ui:ui-text")
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.animation:animation")

    // Previews only. compileOnly so the annotation does not travel into the
    // release APK of every consumer.
    compileOnly("androidx.compose.ui:ui-tooling-preview")

    testImplementation("junit:junit:4.13.2")
}
