plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.dokka")
}

kotlin {

  android {
    namespace = "com.halilibo.richtext.commonmark"
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        api(project(":richtext-ui"))
        api(project(":richtext-markdown"))
      }
    }
    val commonTest by getting

    val jvmAndroidMain by creating {
      dependsOn(commonMain)
      dependencies {
        implementation(Commonmark.core)
        implementation(Commonmark.tables)
        implementation(Commonmark.strikethrough)
        implementation(Commonmark.autolink)
      }
    }

    val jvmAndroidTest by creating {
      dependsOn(commonTest)
      dependencies {
        implementation(Kotlin.Test.jdk)
      }
    }

    val androidMain by getting {
      dependsOn(jvmAndroidMain)
    }

    val jvmMain by getting {
      dependsOn(jvmAndroidMain)
    }

    val jvmTest by getting {
      dependsOn(jvmAndroidTest)
    }
  }
}
