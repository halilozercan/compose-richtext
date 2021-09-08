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

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = Compose.version
  }

  lint {
    disable("ComposableModifierFactory", "ModifierFactoryExtensionFunction", "ModifierFactoryReturnType", "ModifierFactoryUnreferencedReceiver")
  }
}
