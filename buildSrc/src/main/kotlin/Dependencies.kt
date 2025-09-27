object BuildPlugins {
  // keep in sync with buildSrc/build.gradle.kts
  val androidGradlePlugin = "com.android.tools.build:gradle:8.13.0"
}

object AndroidX {
  val appcompat = "androidx.appcompat:appcompat:1.7.1"
}

object Network {
  val okHttp = "com.squareup.okhttp3:okhttp:4.9.0"
}

object Kotlin {
  // keep in sync with buildSrc/build.gradle.kts
  val version = "2.2.20"
  val binaryCompatibilityValidatorPlugin = "org.jetbrains.kotlinx:binary-compatibility-validator:0.9.0"
  val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

  object Test {
    val common = "org.jetbrains.kotlin:kotlin-test-common"
    val annotations = "org.jetbrains.kotlin:kotlin-test-annotations-common"
    val jdk = "org.jetbrains.kotlin:kotlin-test-junit"
  }
}

val ktlint = "org.jlleitschuh.gradle:ktlint-gradle:10.0.0"

object Compose {
  val desktopVersion = "1.8.2"
  val activity = "androidx.activity:activity-compose:1.8.2"
  val toolingData = "androidx.compose.ui:ui-tooling-data:1.6.0"
  val coil = "io.coil-kt.coil3:coil-compose:3.3.0"
  val coilHttp = "io.coil-kt.coil3:coil-network-okhttp:3.3.0"
}

object Commonmark {
  private val version = "0.26.0"
  val core = "org.commonmark:commonmark:$version"
  val tables = "org.commonmark:commonmark-ext-gfm-tables:$version"
  val strikethrough = "org.commonmark:commonmark-ext-gfm-strikethrough:$version"
  val autolink = "org.commonmark:commonmark-ext-autolink:$version"
}

object AndroidConfiguration {
  val minSdk = 23
  val targetSdk = 36
  val compileSdk = targetSdk
}
