plugins {
  id("richtext-android-library")
  id("org.jetbrains.dokka")
}

android {
  namespace = "com.zachklipp.richtext.ui.slideshow"
}

dependencies {
  implementation(Compose.foundation)
  implementation(Compose.material)
  implementation(Compose.tooling)
}
