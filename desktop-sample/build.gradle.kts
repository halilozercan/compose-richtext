import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("jvm")
  id("org.jetbrains.compose") version "1.2.0-alpha01-dev683"
}

dependencies {
  implementation(project(":richtext-commonmark"))
  implementation(project(":richtext-ui-material"))
  implementation(compose.desktop.currentOs)
}

compose.desktop {
  application {
    mainClass = "com.halilibo.richtext.desktop.MainKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "jvm"
      packageVersion = "1.0.0"
    }
  }
}