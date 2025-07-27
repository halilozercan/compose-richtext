import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("jvm")
  id("org.jetbrains.compose") version Compose.desktopVersion
  id("org.jetbrains.kotlin.plugin.compose") version Kotlin.version
}

dependencies {
  implementation(project(":richtext-commonmark"))
  implementation(project(":richtext-ui-material"))
  implementation(compose.desktop.currentOs)
  implementation(compose.materialIconsExtended)
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