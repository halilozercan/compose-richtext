import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("jvm")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
  implementation(project(":richtext-commonmark"))
  implementation(project(":richtext-ui-material"))
  implementation(compose.desktop.currentOs)
  implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
}

compose.desktop {
  application {
    mainClass = "com.halilibo.richtext.desktop.MarkdownSampleAppKt"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "jvm"
      packageVersion = "1.0.0"
    }
  }
}