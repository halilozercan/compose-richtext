// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.dokka")
}

repositories {
  mavenCentral()
}

dokka {
  dokkaPublications.configureEach {
    outputDirectory.set(rootProject.file("docs/api"))
  }
}

dependencies {
  dokka(project(":richtext-ui"))
  dokka(project(":richtext-ui-material"))
  dokka(project(":richtext-ui-material3"))
  dokka(project(":richtext-markdown"))
  dokka(project(":richtext-commonmark"))
}

// See https://stackoverflow.com/questions/25324880/detect-ide-environment-with-gradle
fun isRunningFromIde(): Boolean {
  return project.properties["android.injected.invoked.from.ide"] == "true"
}

subprojects {
  repositories {
    google()
    mavenCentral()
  }

  tasks.withType<KotlinCompile>().all {
    compilerOptions {
//       TODO(stable); Disable warnings as errors until we get to 1.0.0
//       Allow warnings when running from IDE, makes it easier to experiment.
//      if (!isRunningFromIde()) {
//        allWarningsAsErrors = true
//      }

      freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xexpect-actual-classes")
    }
  }

  // taken from https://github.com/google/accompanist/blob/main/build.gradle
  afterEvaluate {
    if (tasks.findByName("dokkaHtmlPartial") == null) {
      // If dokka isn't enabled on this module, skip
      return@afterEvaluate
    }
  }
}

//disable until the library reaches 1.0.0-beta01
//apply plugin: 'binary-compatibility-validator'
//apiValidation {
//  // Ignore all sample projects, since they're not part of our API.
//  // Only leaf project name is valid configuration, and every project must be individually ignored.
//  // See https://github.com/Kotlin/binary-compatibility-validator/issues/3
//  ignoredProjects += project('sample').name
//  ignoredProjects += project('desktop').name
//  ignoredProjects += project('richtext-ui-kmm').name
//  ignoredProjects += project('richtext-commonmark-kmm').name
//}
