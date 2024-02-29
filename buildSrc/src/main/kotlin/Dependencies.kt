object BuildPlugins {
  // keep in sync with buildSrc/build.gradle.kts
  val androidGradlePlugin = "com.android.tools.build:gradle:8.2.0"
}

object AndroidX {
  val activity = "androidx.activity:activity:1.5.0-rc01"
  val annotations = "androidx.annotation:annotation:1.1.0"
  val appcompat = "androidx.appcompat:appcompat:1.3.0"
  val material = "com.google.android.material:material:1.1.0"
}

object Network {
  val okHttp = "com.squareup.okhttp3:okhttp:4.9.0"
}

object Kotlin {
  // keep in sync with buildSrc/build.gradle.kts
  val version = "1.9.22"
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
  val version = "1.5.4"
  val compilerVersion = "1.5.8"
  val desktopVersion = "1.6.0"
  val activity = "androidx.activity:activity-compose:1.8.2"
  val annotatedText = "io.github.aghajari:AnnotatedText:1.0.3"
  val toolingData = "androidx.compose.ui:ui-tooling-data:$version"
  val multiplatformUiUtil = "org.jetbrains.compose.ui:ui-util:$desktopVersion"
  val coil = "io.coil-kt:coil-compose:2.5.0"
}

object Commonmark {
  private val version = "0.21.0"
  val core = "org.commonmark:commonmark:$version"
  val tables = "org.commonmark:commonmark-ext-gfm-tables:$version"
  val strikethrough = "org.commonmark:commonmark-ext-gfm-strikethrough:$version"
  val autolink = "org.commonmark:commonmark-ext-autolink:$version"
}

object AndroidConfiguration {
  val minSdk = 21
  val targetSdk = 34
  val compileSdk = targetSdk
}
