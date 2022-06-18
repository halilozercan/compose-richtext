plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.compose") version "1.1.1"
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
        api(project(":richtext-ui"))
      }
    }
    val commonTest by getting

    val androidMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
      dependencies {
        implementation(Compose.coil)

        implementation(Commonmark.core)
        implementation(Commonmark.tables)
        implementation(Commonmark.strikethrough)
      }
    }

    val jvmMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(Network.okHttp)

        implementation(Commonmark.core)
        implementation(Commonmark.tables)
        implementation(Commonmark.strikethrough)
      }
    }

    val jvmTest by getting {
      kotlin.srcDir("src/commonJvmAndroidTest/kotlin")
      dependencies {
        implementation(Kotlin.Test.jdk)
      }
    }
  }
}
