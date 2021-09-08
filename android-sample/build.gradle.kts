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

  composeOptions {
    kotlinCompilerExtensionVersion = Compose.version
  }

  lint {
    disable("ComposableModifierFactory", "ModifierFactoryExtensionFunction", "ModifierFactoryReturnType", "ModifierFactoryUnreferencedReceiver")
  }
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (requested.group.contains("org.jetbrains.compose")) {
      val groupName = requested.group.replace("org.jetbrains.compose", "androidx.compose")
      useTarget(
          mapOf("group" to groupName, "name" to requested.name, "version" to Compose.version)
      )
    }
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
