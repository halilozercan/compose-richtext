plugins {
  id("com.android.library")
  kotlin("multiplatform")
  id("maven-publish")
  id("signing")
}

repositories {
  google()
  mavenCentral()
}

kotlin {
  jvm()
  android {
    publishLibraryVariants("release", "debug")
  }
  explicitApi()
}

android {
  compileSdk = 33
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  defaultConfig {
    minSdk = 21
    targetSdk = compileSdk
  }
}
