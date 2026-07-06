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
        implementation(CommonmarkKmp.core)
        implementation(CommonmarkKmp.tables)
        implementation(CommonmarkKmp.strikethrough)
        implementation(CommonmarkKmp.autolink)
      }
    }
    val commonTest by getting

    val jvmAndroidMain by creating {
      dependsOn(commonMain)
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

    val iosMain by creating {
      dependsOn(commonMain)
    }
    val iosArm64Main by getting {
      dependsOn(iosMain)
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }

    val jvmTest by getting {
      dependsOn(jvmAndroidTest)
    }
  }
}
