plugins {
  id("richtext-android-library")
  id("org.jetbrains.dokka")
  id("org.jetbrains.compose") version Compose.desktopVersion
  id("org.jetbrains.kotlin.plugin.compose") version Kotlin.version
}

android {
  namespace = "com.zachklipp.richtext.ui.slideshow"
}

dependencies {
  implementation(compose.foundation)
  implementation(compose.material)
  implementation(compose.uiTooling)
}
