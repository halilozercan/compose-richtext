object BuildPlugins {
  val androidGradlePlugin = "com.android.tools.build:gradle:7.2.2"
}

object AndroidX {
  val activity = "androidx.activity:activity:1.5.0-rc01"
  val annotations = "androidx.annotation:annotation:1.1.0"
  val appcompat = "androidx.appcompat:appcompat:1.3.0"
  val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
  val fragment = "androidx.fragment:fragment:1.2.2"
  val material = "com.google.android.material:material:1.1.0"
  val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
  val savedstate = "androidx.savedstate:savedstate-ktx:1.2.0-rc01"
  val transition = "androidx.transition:transition:1.3.1"
  val viewbinding = "androidx.databinding:viewbinding:3.6.1"
}

object Network {
  val okHttp = "com.squareup.okhttp3:okhttp:4.9.0"
}

object Kotlin {
  val version = "1.7.10"
  val binaryCompatibilityValidatorPlugin = "org.jetbrains.kotlinx:binary-compatibility-validator:0.9.0"
  val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

  object Test {
    val common = "org.jetbrains.kotlin:kotlin-test-common"
    val annotations = "org.jetbrains.kotlin:kotlin-test-annotations-common"
    val jdk = "org.jetbrains.kotlin:kotlin-test-junit"
    val mockito = "com.nhaarman:mockito-kotlin-kt1.1:1.6.0"
  }
}

val ktlint = "org.jlleitschuh.gradle:ktlint-gradle:10.0.0"

object Compose {
  val version = "1.2.1"
  val compilerVersion = "1.3.1"
  val desktopVersion = "1.2.0"
  val activity = "androidx.activity:activity-compose:1.6.0-rc01"
  val foundation = "androidx.compose.foundation:foundation:$version"
  val material = "androidx.compose.material:material:$version"
  val material3 = "androidx.compose.material3:material3:1.0.0-beta02"
  val icons = "androidx.compose.material:material-icons-extended:$version"
  val test = "androidx.ui:ui-test:$version"
  val tooling = "androidx.compose.ui:ui-tooling:$version"
  val toolingData = "androidx.compose.ui:ui-tooling-data:$version"
  val desktopPreview = "org.jetbrains.compose.ui:ui-tooling-preview-desktop:$desktopVersion"
  val multiplatformUiUtil = "org.jetbrains.compose.ui:ui-util:$desktopVersion"
  val coil = "io.coil-kt:coil-compose:2.2.1"
}

object Commonmark {
  private val version = "0.20.0"
  val core = "org.commonmark:commonmark:$version"
  val tables = "org.commonmark:commonmark-ext-gfm-tables:$version"
  val strikethrough = "org.commonmark:commonmark-ext-gfm-strikethrough:$version"
  val autolink = "org.commonmark:commonmark-ext-autolink:$version"
}

object AndroidConfiguration {
  val minSdk = 21
  val targetSdk = 33
  val compileSdk = targetSdk
}
