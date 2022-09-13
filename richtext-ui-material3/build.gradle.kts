plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.compose") version "1.2.0-alpha01-dev764"
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

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.material3)

        api(project(":richtext-ui"))
      }
    }
    val commonTest by getting

    val androidMain by getting
    val jvmMain by getting
  }
}
