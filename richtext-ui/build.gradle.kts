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
dependencies {
  implementation("androidx.lifecycle:lifecycle-runtime-compose-android:2.8.4")
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
      dependencies {
        // requires installing https://plugins.jetbrains.com/plugin/16541-compose-multiplatform-ide-support
        implementation(Compose.desktopPreview)
      }
    }
  }
}
