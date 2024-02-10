plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.compose") version Compose.desktopVersion
  id("org.jetbrains.dokka")
}

repositories {
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

android {
  namespace = "com.halilibo.richtext.ui"
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(Compose.multiplatformUiUtil)
      }
    }
    val commonTest by getting

    val androidMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
    }
    val jvmMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
    }
  }
}
