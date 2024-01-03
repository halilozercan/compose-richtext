repositories {
  google()
  mavenCentral()
}

plugins {
  `kotlin-dsl`
  `kotlin-dsl-precompiled-script-plugins`
}

dependencies {
  // keep in sync with Dependencies.BuildPlugins.androidGradlePlugin
  implementation("com.android.tools.build:gradle:8.2.0")
  // keep in sync with Dependencies.Kotlin.gradlePlugin
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
  implementation(kotlin("script-runtime"))
}