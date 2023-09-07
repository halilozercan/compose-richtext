plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.compose") version Compose.desktopVersion
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
        api(Commonmark.core)
        api(project(":richtext-ui"))
      }
    }
    val commonTest by getting

    val androidMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
      dependencies {
        implementation(Compose.coil)

        implementation(Commonmark.tables)
        implementation(Commonmark.strikethrough)
        implementation(Commonmark.autolink)
      }
    }

    val jvmMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(Network.okHttp)

        implementation(Commonmark.tables)
        implementation(Commonmark.strikethrough)
        implementation(Commonmark.autolink)
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
