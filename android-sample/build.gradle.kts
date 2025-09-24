import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.application")
  kotlin("android")
  id("org.jetbrains.compose") version Compose.desktopVersion
  id("org.jetbrains.kotlin.plugin.compose") version Kotlin.version
}

android {
  namespace = "com.zachklipp.richtext.sample"
  compileSdk = AndroidConfiguration.compileSdk

  defaultConfig {
    minSdk = AndroidConfiguration.minSdk
    targetSdk = AndroidConfiguration.targetSdk
  }

  buildFeatures {
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_11
  }
}

dependencies {
  implementation(project(":richtext-commonmark"))
  implementation(project(":richtext-ui-material3"))
  implementation(AndroidX.appcompat)
  implementation(Compose.activity)
  implementation(compose.foundation)
  implementation(compose.materialIconsExtended)
  implementation(compose.material3)
  implementation(compose.uiTooling)
}
