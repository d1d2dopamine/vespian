// A Kotlin DSL build script resolves "java" to the JavaPluginExtension that the
// Android plugin brings with it, not to the java package root, so a qualified
// java.time.ZonedDateTime does not compile here -- the script fails to configure
// before any source file is looked at. Import the types instead.
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// -----------------------------------------------------------------------------
// Vespian's app module.
//
// Two things are different from the file currently in the repository, and both
// of them are why the build fails before a single line of Kotlin is compiled:
//
//   1. The old file is Ikna's, down to namespace = "dev.ikna". Every R.string
//      in dev/vespian/** then resolves against a package that Vespian's code
//      never imports, so R is unresolved in eight files.
//
//   2. The old file uses libs.* version-catalog accessors, and this repository
//      has no gradle/ directory at all. "Unresolved reference: libs" at
//      configuration time. Versions are written out literally below so the
//      build stops depending on a file that is not in the repo.
//
// Dependencies are exactly what dev/vespian/** imports -- nothing more. Ikna's
// file pulled in navigation-compose, lottie, datastore and serialization, none
// of which Vespian uses; the serialization plugin went with them, since no file
// in this app carries an @Serializable (Filter writes its own JSON by hand).
//
// Material 3 is gone as well. The interface is drawn by the :lattice module --
// Ikna's design system, extracted -- so this module has no Material artifact and
// no icon artifact on its classpath at all.
// -----------------------------------------------------------------------------
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// The keystore that is actually in this repository. The old file looked for
// ikna.keystore, did not find it, and quietly fell back to a per-machine debug
// key -- which is why a new build refuses to install over the previous one
// (INSTALL_FAILED_UPDATE_INCOMPATIBLE / "signatures do not match").
// The commit and the moment this APK was built. The about screen prints both and
// every backup carries them in its header, so they are not decoration: a bug
// report that names a commit can be reproduced. GITHUB_SHA is set by Actions; a
// local build says "local" rather than pretend to know. BUILD_AT is cut to the
// minute in UTC -- finer than that and two builds of one commit would miss the
// build cache for no one's benefit.
val BUILD_STAMP_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'")

val gitSha: String = (System.getenv("GITHUB_SHA") ?: "").take(7).ifEmpty { "local" }
val buildAt: String = System.getenv("BUILD_AT")
    ?: ZonedDateTime.now(ZoneOffset.UTC).format(BUILD_STAMP_FORMAT)

val keystoreFile = rootProject.file("app/vespian-debug.jks")
val keystorePassword = System.getenv("VESPIAN_KEYSTORE_PASSWORD") ?: "vespiandebug"
val keystoreAlias = System.getenv("VESPIAN_KEYSTORE_ALIAS") ?: "vespian"
val hasFixedKey = keystoreFile.exists()

android {
    namespace = "dev.vespian"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.vespian"
        minSdk = 29
        targetSdk = 35
        versionCode = (System.getenv("RUN_NUMBER") ?: "1").toInt()
        versionName = "0.1." + (System.getenv("RUN_NUMBER") ?: "1")

        ksp { arg("room.schemaLocation", "$projectDir/schemas") }

        // Vespian's own BuildConfig fields. They vanished with the build file
        // this module inherited: that file is Ikna's, and Ikna has no about
        // screen to print them, so nothing there declared them.
        buildConfigField("String", "GIT_SHA", "\"" + gitSha + "\"")
        buildConfigField("String", "BUILD_AT", "\"" + buildAt + "\"")
        // No resourceConfigurations here: AGP 8.9 deprecates it, its
        // replacement (androidResources.localeFilters) is new API, and the
        // only thing either one does is drop library translations from the
        // APK. The app ships en and ru either way -- res/xml/locales_config
        // is what the system language picker actually reads.
    }

    signingConfigs {
        create("fixed") {
            if (hasFixedKey) {
                storeFile = keystoreFile
                storePassword = keystorePassword
                keyAlias = keystoreAlias
                keyPassword = keystorePassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            if (hasFixedKey) signingConfig = signingConfigs.getByName("fixed")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasFixedKey) signingConfig = signingConfigs.getByName("fixed")
        }
    }

    buildFeatures {
        compose = true
        // AGP 8 generates BuildConfig only when asked. Nothing asked, and twelve
        // references across SettingsActivity and Backup went unresolved -- the
        // version name in the about screen, the version code, the commit, the
        // build time and the package name.
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }

    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    // Required: MainActivity, OnboardingActivity and SettingsActivity extend
    // AppCompatActivity and SettingsActivity uses AppCompatDelegate for
    // per-app locales. This dependency was missing entirely.
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // The design system, in place of Material 3. Nothing in this module imports
    // androidx.compose.material* any more -- the screens draw with dev.lattice.*,
    // and the compat package in :lattice answers the old Material names while
    // the last screens are rewritten. There is no icon artifact either: the
    // marks are drawn from lines in LatGlyph.
    implementation(project(":lattice"))

    // Health Connect. Also missing, while 16 files import it.
    implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    testImplementation("junit:junit:4.13.2")
}
