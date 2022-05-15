plugins {
  id("com.android.application")
  kotlin("android")
}

android {
  compileSdk = AndroidConfiguration.compileSdk

  defaultConfig {
    minSdk = 21
    targetSdk = AndroidConfiguration.targetSdk
  }

  buildFeatures {
    compose = true
  }

  kotlinOptions { jvmTarget = "11" }

  composeOptions {
    kotlinCompilerExtensionVersion = Compose.version
  }
}

dependencies {
  implementation(project(":printing"))
  implementation(project(":richtext-commonmark"))
  implementation(project(":richtext-ui-material"))
  implementation(project(":slideshow"))
  implementation(AndroidX.appcompat)
  implementation(Compose.activity)
  implementation(Compose.foundation)
  implementation(Compose.icons)
  implementation(Compose.material)
  implementation(Compose.tooling)
}
