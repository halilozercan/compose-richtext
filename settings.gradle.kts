pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
  }
}

include(":richtext-ui")
include(":richtext-ui-material")
include(":richtext-ui-material3")
include(":richtext-commonmark")
include(":richtext-markdown")
include(":android-sample")
include(":desktop-sample")
rootProject.name = "compose-richtext"
