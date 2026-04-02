plugins {
  id("richtext-kmp-library")
  id("org.jetbrains.dokka")
}

kotlin {
  android {
    namespace = "com.halilibo.richtext.ui.material"
  }
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
        api(project(":richtext-ui"))
      }
    }
    val commonTest by getting

    val androidMain by getting
    val jvmMain by getting
  }
}
