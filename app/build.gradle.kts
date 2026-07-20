import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val githubRunNumber =
    System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

val versionBase = "1.3"

val automaticVersionName =
    "$versionBase.$githubRunNumber"

val automaticBuildDate =
    ZonedDateTime.now(ZoneId.of("Europe/Zurich"))
        .format(
            DateTimeFormatter.ofPattern(
                "MMMM yyyy",
                Locale.GERMANY
            )
        )

plugins {
    id("com.android.application")
}

android {
    namespace = "ch.florian.tagescode"
    compileSdk = 35

    defaultConfig {
        applicationId = "ch.florian.tagescode"
        minSdk = 26
        targetSdk = 35

        versionCode = githubRunNumber
        versionName = automaticVersionName

        buildConfigField(
            "String",
            "BUILD_NUMBER",
            "\"$githubRunNumber\""
        )

        buildConfigField(
            "String",
            "BUILD_DATE",
            "\"$automaticBuildDate\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("androidx.documentfile:documentfile:1.1.0")
}
