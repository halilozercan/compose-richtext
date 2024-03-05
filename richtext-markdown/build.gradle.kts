plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.compose") version Compose.desktopVersion
  id("org.jetbrains.kotlin.plugin.compose") version Kotlin.version
  id("org.jetbrains.dokka")
}

repositories {
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

android {
  namespace = "com.halilibo.richtext.markdown"
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        api(project(":richtext-ui"))
      }
    }
    val commonTest by getting

    val androidMain by getting {
      dependencies {
        implementation(Compose.coil)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(Network.okHttp)
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(Kotlin.Test.jdk)
      }
    }
  }
}
