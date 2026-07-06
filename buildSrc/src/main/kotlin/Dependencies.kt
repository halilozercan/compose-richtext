object BuildPlugins {
  // keep in sync with buildSrc/build.gradle.kts
  val androidGradlePlugin = "com.android.tools.build:gradle:9.1.0"
}

object AndroidX {
  val appcompat = "androidx.appcompat:appcompat:1.7.1"
}

object Network {
  val okHttp = "com.squareup.okhttp3:okhttp:4.9.0"
}

object Kotlin {
  // keep in sync with buildSrc/build.gradle.kts
  val version = "2.3.10"
  val binaryCompatibilityValidatorPlugin = "org.jetbrains.kotlinx:binary-compatibility-validator:0.9.0"
  val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

  val composeCompilerPlugin = "org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:$version"

  object Test {
    val common = "org.jetbrains.kotlin:kotlin-test-common"
    val annotations = "org.jetbrains.kotlin:kotlin-test-annotations-common"
    val jdk = "org.jetbrains.kotlin:kotlin-test-junit"
  }
}

val ktlint = "org.jlleitschuh.gradle:ktlint-gradle:10.0.0"
val desktopCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2"

object Compose {
  val desktopVersion = "1.11.0-beta01"

  val jetbrainsComposePlugin = "org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:$desktopVersion"
  val activity = "androidx.activity:activity-compose:1.8.2"
  val toolingData = "androidx.compose.ui:ui-tooling-data:1.6.0"
  val coil = "io.coil-kt.coil3:coil-compose:3.3.0"
  val coilKtor = "io.coil-kt.coil3:coil-network-ktor3:3.3.0"
  val coilKtorAndroid = "io.ktor:ktor-client-okhttp:3.3.0"
  val coikKtorIOS = "io.ktor:ktor-client-darwin:3.3.0"
  val coilKtorDesktop = "io.ktor:ktor-client-java:3.3.0"
}

object CommonmarkKmp {
  private val version = "0.0.2"
  val core = "io.github.feiyin0719:commonmark:$version"
  val tables = "io.github.feiyin0719:commonmark-ext-gfm-tables:$version"
  val strikethrough = "io.github.feiyin0719:commonmark-ext-gfm-strikethrough:$version"
  val autolink = "io.github.feiyin0719:commonmark-ext-autolink:$version"
}

object AndroidConfiguration {
  val minSdk = 24
  val targetSdk = 36
  val compileSdk = targetSdk
}