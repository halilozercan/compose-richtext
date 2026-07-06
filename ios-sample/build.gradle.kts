plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    val iosMain by creating {
      dependsOn(commonMain.get())
    }

    val iosArm64Main by getting {
      dependsOn(iosMain)
    }

    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }

    iosMain.dependencies {
      implementation(project(":richtext-commonmark"))
      implementation(project(":richtext-ui-material3"))
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
    }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeRichText"
      isStatic = true
    }
  }
}
