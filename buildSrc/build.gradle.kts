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
  implementation("com.android.tools.build:gradle:7.0.1")
  // keep in sync with Dependencies.Kotlin.gradlePlugin
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
  implementation(kotlin("script-runtime"))
}