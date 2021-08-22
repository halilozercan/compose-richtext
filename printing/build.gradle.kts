plugins {
  id("richtext-android-library")
  id("org.jetbrains.dokka")
}

dependencies {
  implementation(Compose.foundation)
  implementation(Compose.tooling)
  // For slot table analysis.
  implementation(Compose.toolingData)
  implementation(AndroidX.activity)

  // TODO Migrate off this.
  implementation(Compose.material)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + "-Xinline-classes"
  }
}
