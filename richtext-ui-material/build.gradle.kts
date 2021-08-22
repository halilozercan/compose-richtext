plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.compose") version "1.0.0-alpha3"
  id("org.jetbrains.dokka")
}

repositories {
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(project(":richtext-ui"))
      }
    }
    val commonTest by getting

    val androidMain by getting
    val jvmMain by getting
  }
}
