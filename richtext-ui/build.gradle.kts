plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.dokka")
}

kotlin {
  android {
    namespace = "com.halilibo.richtext.ui"
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
      }
    }
    val commonTest by getting

    val jvmAndroidMain by creating {
      dependsOn(commonMain)
    }

    val androidMain by getting {
      dependsOn(jvmAndroidMain)
    }
    val jvmMain by getting {
      dependsOn(jvmAndroidMain)
    }
  }
}
