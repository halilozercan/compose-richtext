object BuildPlugins {
  val androidGradlePlugin = "com.android.tools.build:gradle:7.0.1"
}

object AndroidX {
  val activity = "androidx.activity:activity:1.1.0"
  val annotations = "androidx.annotation:annotation:1.1.0"
  val appcompat = "androidx.appcompat:appcompat:1.3.0"
  val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
  val fragment = "androidx.fragment:fragment:1.2.2"
  val material = "com.google.android.material:material:1.1.0"
  val recyclerview = "androidx.recyclerview:recyclerview:1.1.0"
  val savedstate = "androidx.savedstate:savedstate:1.0.0"
  val transition = "androidx.transition:transition:1.3.1"
  val viewbinding = "androidx.databinding:viewbinding:3.6.1"
}

object Network {
  val okHttp = "com.squareup.okhttp3:okhttp:4.9.0"
}

object Kotlin {
  val version = "1.5.31"
  val binaryCompatibilityValidatorPlugin = "org.jetbrains.kotlinx:binary-compatibility-validator:0.5.0"
  val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
  val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
  val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"

  object Test {
    val common = "org.jetbrains.kotlin:kotlin-test-common"
    val annotations = "org.jetbrains.kotlin:kotlin-test-annotations-common"
    val jdk = "org.jetbrains.kotlin:kotlin-test-junit"
    val mockito = "com.nhaarman:mockito-kotlin-kt1.1:1.6.0"
  }
}

val ktlint = "org.jlleitschuh.gradle:ktlint-gradle:10.0.0"

object Compose {
  val version = "1.1.0-beta03"
  val activity = "androidx.activity:activity-compose:1.3.0"
  val foundation = "androidx.compose.foundation:foundation:$version"
  val layout = "androidx.compose.foundation:foundation-layout:$version"
  val material = "androidx.compose.material:material:$version"
  val icons = "androidx.compose.material:material-icons-extended:$version"
  val test = "androidx.ui:ui-test:$version"
  val tooling = "androidx.compose.ui:ui-tooling:$version"
  val toolingPreview = "androidx.compose.ui:ui-tooling-preview:$version"
  val toolingData = "androidx.compose.ui:ui-tooling-data:$version"
  val desktopPreview = "org.jetbrains.compose.ui:ui-tooling-preview-desktop:1.0.0-beta5"
  val coil = "io.coil-kt:coil-compose:1.4.0"
}

object Commonmark {
  private val version = "0.18.0"
  val core = "org.commonmark:commonmark:$version"
  val tables = "org.commonmark:commonmark-ext-gfm-tables:$version"
  val strikethrough = "org.commonmark:commonmark-ext-gfm-strikethrough:$version"
}

object AndroidConfiguration {
  val minSdk = 21
  val targetSdk = 31
  val compileSdk = targetSdk
}
