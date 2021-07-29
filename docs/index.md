# Overview

[![Maven Central](https://img.shields.io/maven-central/v/com.halilibo.compose-richtext/richtext-ui.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.zachklipp.compose-richtext%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

Compose Richtext is a collection of Compose libraries for working with rich text formatting and
documents.

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

The `LIBRARY_ARTIFACT`s for each individual library can be found on their respective pages.
