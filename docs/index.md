# Overview

[![Maven Central](https://img.shields.io/maven-central/v/com.halilibo.compose-richtext/richtext-ui.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.halilibo.compose-richtext%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

Compose Richtext is a collection of Compose libraries for working with rich text formatting and
documents. 

`richtext-ui`, `richtext-commonmark`, and `richtext-material-ui` are Kotlin Multiplatform(KMP) Compose Libraries.
All 3 of these modules can be used in Android and Desktop Compose apps. 

Each library is documented separately, see the navigation menu for the list. This site also includes
an API reference.

!!! warning
    This project is currently very experimental and mostly just a proof-of-concept at this point.
    There are no tests and some things might be broken or very non-performant.

    The API may also change between releases without deprecation cycles.

## Getting started

These libraries are published to Maven Central, so just add a Gradle dependency:

```groovy
dependencies {
  implementation "com.halilibo.compose-richtext:<LIBRARY-ARTIFACT>:${richtext_version}"
}
```

There is no difference for KMP artifacts. For instance, if you are adding `richtext-ui` to a Kotlin Multiplatform module

```kotlin
val commonMain by getting {
  dependencies {
    implementation("com.halilibo.compose-richtext:richtext-ui:${richtext_version}")
  }
}
```

### Android 

Desktop Compose and Jetpack Compose for Android use the same modules but they are published under separate 
artifacts. `org.jetbrains.compose` plugin replaces all `androidx.compose` dependencies with their `org.jetbrains.compose` counterparts.
If this is not done, gradle build fails with too many **Duplicate Class** errors.

On the other hand, applying a KMP plugin in a pure Android App might not be the desired approach. Adding the following
configuration to `build.gradle` file in an Android module that depends on any of `RichText` KMP libraries would solve the problem as well.

=== "Groovy"
    ```groovy
    configurations.all {
      resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        if (details.requested.group.contains('org.jetbrains.compose')) {
          def groupName = details.requested.group.replace("org.jetbrains.compose", "androidx.compose")
          details.useTarget(
              [group: groupName, name: details.requested.name, version: composeVersion] // compose version in your project
          )
        }
      }
    }
    ```

=== "Kotlin"
    ``` kotlin
    configurations.all {
      resolutionStrategy.eachDependency {
        if (requested.group.contains("org.jetbrains.compose")) {
          val groupName = requested.group.replace("org.jetbrains.compose", "androidx.compose")
          useTarget("$groupName:${requested.name}:${composeVersion}") // compose version in your project
        }
      }
    }
    ```

Most importantly, this problem seems to be temporary. Jetbrains team is actively working on it.

[A comment from Jetbrains in Compose Plugin docs.](https://github.com/JetBrains/compose-jb/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/ComposePlugin.kt#L79)

> It is temporarily solution until we will be publishing all MPP artifacts in Google Maven repository. Or align versions with androidx artifacts and point MPP-android artifacts to androidx artifacts (is it possible?)

### Library Artifacts

The `LIBRARY_ARTIFACT`s for each individual library can be found on their respective pages.

## Samples

Please check out [Android](https://github.com/halilozercan/compose-richtext/tree/main/android-sample) and [Desktop](https://github.com/halilozercan/compose-richtext/tree/main/desktop-sample)
projects to see various use cases of RichText in both platforms.