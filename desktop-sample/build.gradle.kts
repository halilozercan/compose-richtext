import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose") version "1.0.0-alpha3"
}

kotlin {
  jvm()
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(project(":richtext-commonmark"))
        implementation(project(":richtext-ui-material"))
        implementation(compose.desktop.currentOs)
      }
    }
    val jvmTest by getting
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "jvm"
      packageVersion = "1.0.0"
    }
  }
}