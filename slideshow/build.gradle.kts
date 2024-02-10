plugins {
  id("richtext-android-library")
  id("org.jetbrains.dokka")
  id("org.jetbrains.compose") version Compose.desktopVersion
}

android {
  namespace = "com.zachklipp.richtext.ui.slideshow"
}

dependencies {
  implementation(compose.foundation)
  implementation(compose.material)
  implementation(compose.uiTooling)
}
