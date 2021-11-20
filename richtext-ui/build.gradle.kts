plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.compose") version "1.0.0-beta5"
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
      }
    }
    val commonTest by getting

    val androidMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
    }
    val jvmMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
      dependencies {
        // requires installing https://plugins.jetbrains.com/plugin/16541-compose-multiplatform-ide-support
        implementation(Compose.desktopPreview)
      }
    }
  }
}
