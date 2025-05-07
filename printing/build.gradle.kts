plugins {
  id("richtext-android-library")
  id("org.jetbrains.dokka")
  id("org.jetbrains.compose") version Compose.desktopVersion
  id("org.jetbrains.kotlin.plugin.compose") version Kotlin.version
}

android {
  namespace = "com.zachklipp.richtext.ui.printing"
}

dependencies {
  implementation(compose.foundation)
  implementation(compose.uiTooling)
  // For slot table analysis.
  implementation(Compose.toolingData)
  implementation(Compose.activity)

  // TODO Migrate off this.
  implementation(compose.material)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xinline-classes"
  }
}
