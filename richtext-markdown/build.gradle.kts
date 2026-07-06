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
        implementation(Compose.coil)
        implementation(Compose.coilKtor)
        implementation(compose.runtime)
        implementation(compose.foundation)
        api(project(":richtext-ui"))
      }
    }
    val commonTest by getting

    val androidMain by getting {
      dependencies {
        implementation(Compose.coilKtorAndroid)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(Network.okHttp)
        implementation(Compose.coilKtorDesktop)
        implementation(desktopCoroutines)
      }
    }

    val iosMain by creating {
      dependencies {
        implementation(Compose.coikKtorIOS)
      }
      dependsOn(commonMain)
    }
    val iosArm64Main by getting {
      dependsOn(iosMain)
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }

    val jvmTest by getting {
      dependencies {
        implementation(Kotlin.Test.jdk)
      }
    }
  }
}