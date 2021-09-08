pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
  }
}

include(":printing")
include(":richtext-ui")
include(":richtext-ui-material")
include(":richtext-commonmark")
include(":android-sample")
include(":desktop-sample")
include(":slideshow")
rootProject.name = "compose-richtext"
