plugins {
  id("com.android.library")
  kotlin("android")
  id("maven-publish")
  id("signing")
}

kotlin {
  explicitApi()
}

android {
  compileSdk = AndroidConfiguration.compileSdk

  defaultConfig {
    minSdk = 21
    targetSdk = AndroidConfiguration.targetSdk
  }

  kotlinOptions { jvmTarget = "11" }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = Compose.version
  }
}
