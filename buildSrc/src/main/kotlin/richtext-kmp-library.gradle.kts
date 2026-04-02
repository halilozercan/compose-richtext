import AndroidConfiguration.compileSdk
import AndroidConfiguration.minSdk
import AndroidConfiguration.targetSdk
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("multiplatform")
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.compose")
  id("com.vanniktech.maven.publish")
  id("org.jetbrains.dokka")
  signing
}

repositories {
  google()
  mavenCentral()
}

signing {
  val signingKey = System.getenv("GPG_PRIVATE_KEY")?.replace("\\n", "\n")
  val signingPassword = System.getenv("GPG_PRIVATE_PASSWORD")
  if (signingKey != null && signingPassword != null) {
    useInMemoryPgpKeys(signingKey, signingPassword)
  }
}

// Maven Central credentials are provided via ORG_GRADLE_PROJECT_mavenCentralUsername
// and ORG_GRADLE_PROJECT_mavenCentralPassword environment variables.
mavenPublishing {
  publishToMavenCentral()
  signAllPublications()
}

kotlin {
  jvm()
  explicitApi()

  android {
    compileSdk = 36

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }
}

