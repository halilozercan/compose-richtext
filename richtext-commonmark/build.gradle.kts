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
  namespace = "com.halilibo.richtext.commonmark"
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        api(project(":richtext-ui"))
        api(project(":richtext-markdown"))
      }
    }
    val commonTest by getting

    val androidMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
      dependencies {
        implementation(Commonmark.core)
        implementation(Commonmark.tables)
        implementation(Commonmark.strikethrough)
        implementation(Commonmark.autolink)
        implementation(Commonmark.imageAttributes)
      }
    }

    val jvmMain by getting {
      kotlin.srcDir("src/commonJvmAndroid/kotlin")
      dependencies {
        implementation(Commonmark.core)
        implementation(Commonmark.tables)
        implementation(Commonmark.strikethrough)
        implementation(Commonmark.autolink)
        implementation(Commonmark.imageAttributes)
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
