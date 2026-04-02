plugins {
  id("richtext-kmp-library")
}

kotlin {
  android {
    namespace = "com.halilibo.richtext.markdown"
  }
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
        implementation(Compose.coilHttp)
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
