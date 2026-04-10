// App module build file
import java.util.Properties
import java.io.FileInputStream
import com.android.build.api.variant.ApkOutput
import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("net.canvoki.android-yaml-strings") version "1.0.0"
}

val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    FileInputStream(versionPropsFile).use { load(it) }
}

fun loadEnv() {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return
    envFile.forEachLine { line ->
        if (line.trim().isEmpty() || line.startsWith("#")) return@forEachLine
        val parts = line.split("=", limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim()
            val value = parts[1].trim()
            // Elimina cometes dobles o simples envolvents
            val cleanedValue = value.removeSurrounding("\"").removeSurrounding("'")
            project.ext.set(key, cleanedValue)
        }
    }
}

loadEnv()

android {
    signingConfigs {
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE").toString())
            storePassword = project.property("RELEASE_STORE_PASSWORD").toString()
            keyAlias = project.property("RELEASE_KEY_ALIAS").toString()
            keyPassword = project.property("RELEASE_KEY_PASSWORD").toString()
        }
    }

    namespace = "net.canvoki.vokibot"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.canvoki.vokibot"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = versionProps.getProperty("versionCode").toInt()
        versionName = versionProps.getProperty("versionName")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Rename apks adding appid and version
    applicationVariants.all {
        outputs.all {
            if (this is ApkVariantOutputImpl) {
                val version = mergedFlavor.versionName
                val buildType = buildType.name
                val appId = applicationId
                val flavorInfix = if (android.productFlavors.size > 1) "-$flavorName" else ""
                outputFileName = "${appId}${flavorInfix}-${version}-${buildType}.apk"
            }
        }
    }

    bundle {
        language {
            // Because user may change the language, all should be present
            enableSplit = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "freeness"

    productFlavors {
        create("floss") {
            dimension = "freeness"
            isDefault = true
        }
        create("nonfree") {
            dimension = "freeness"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xnested-type-aliases")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
        unitTests.all {
            it.jvmArgs(
                "-XX:+EnableDynamicAgentLoading",
                "--add-opens", "java.base/java.time=ALL-UNNAMED",
                "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
            )
        }
    }
    // Dependencies info is encrypted Google only to read block
    dependenciesInfo {
        includeInApk = false // APK
        includeInBundle = false // AAB
    }
}

tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
    testLogging {
        events("failed", "skipped")//, "standardOut", "standardError", "passed", "started"
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

dependencies {
    implementation(project(":shared"))

    // Platform BOM imports
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.kotlinx.coroutines.bom))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Tes
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
