Changelog
=========

1.0.0-alpha03
-------

This release removes the `printing` and `slideshow` modules to focus on the core Markdown and RichText functionalities. It also adds support for inline base64 images.

### Breaking Changes
- The `printing` and `slideshow` modules have been removed. If you were using them, you will need to find an alternative or use a previous version of the library.

### New Features
- **Inline Base64 Image Rendering**: Markdown images can now be rendered from inline base64-encoded data URIs.

### Updates & Maintenance
- **Dependencies Updated**:
    - Compose Multiplatform updated to `1.8.2`.
    - Commonmark updated to `0.25.0`.
    - Dokka updated to `2.0.0`.
- **Build & CI**:
    - Android Gradle Plugin and other dependencies have been updated.
    - CI now uses `actions/cache@v4`.
- **Sample App**:
    - The Android sample app has been updated to reflect the removal of the `printing` and `slideshow` modules.
    - Theme handling in the sample app has been simplified.

v0.11.0
------

_2022_02_09_

* Upgrade Coil to 2.0.0-alpha06 by @msfjarvis in https://github.com/halilozercan/compose-richtext/pull/72

## New Contributors
* @msfjarvis made their first contribution in https://github.com/halilozercan/compose-richtext/pull/72

**Full Changelog**: https://github.com/halilozercan/compose-richtext/compare/v0.10.0...v0.11.0

v0.10.0
------

_2021_12_05_

This release celebrates the release of Compose Multiplatform 1.0.0 🎉🥳

v0.9.0
------

_2021_11_20_

This release is mostly a version bump.
- Jetpack Compose: 1.1.0-beta03
- Jetbrains Compose: 1.0.0-beta5
- Kotlin: 1.5.31

Other changes:

* Fix link formatting in index page of docs by in https://github.com/halilozercan/compose-richtext/pull/60
* CodeBlock fixes in https://github.com/halilozercan/compose-richtext/pull/62
* Update CHANGELOG.md to include releases after the transfer in https://github.com/halilozercan/compose-richtext/pull/64
* Add info panels similar to bootstrap alerts #54 in https://github.com/halilozercan/compose-richtext/pull/63


**Full Changelog**: https://github.com/halilozercan/compose-richtext/compare/v0.8.1...v0.9.0

v0.8.1
------

_2021-9-11_

This release fixes JVM artifact issue #59

v0.8.0
------

_2021-9-8_

Compose Richtext goes KMP, opening RichText UI and its extensions to both Android and Desktop (#50)

Special thanks @zach-klippenstein @LouisCAD @russhwolf for their reviews and help.

* Richtext UI, Richtext UI Material, and RichText Commonmark are now KMP Compose libraries
* Slideshow, Printing remains Android only for the foreseeable future
* Updated docs
* A new CI compatible release configuration

v0.7.0
------

_2021-8-6_

* RichText UI no longer depends on Material (#45)
* A new artifact richtext-ui-material is published to easily integrate RichText for apps that use Material design.
* Upgraded compose to 1.0.1 and kotlin to 1.5.21

v0.6.0
------

_2021-7-29_

* **Compose 1.0.0 support** (#43)
* Upgrade to Gradle 7.0.2 (#40)
* Fix wrong word used. portrait -> landscape (#37 - thanks @LouisCAD)
* Repository has migrated from @zach-klippenstein to @halilozercan.
* Artifacts have moved from com.zachklipp.compose-richtext to com.halilibo.compose-richtext.
* Similarly, documentation is also now available at halilibo.com/compose-richtext

v0.5.0
------

_2021-5-18_

* **Compose Beta 7 support!** (#36)
* Fix several bugs in Table, RichTextStyle and improve InlineContent (#35 – thanks @halilozercan!)

v0.2.0
------

_2021-2-27_

* **Compose Beta 1 support!**
* Remove BulletList styling for different leading characters - Update markdown-demo.png to show new
  BulletList rendering (#28 – thanks @halilozercan!)

v0.1.0+alpha06
--------------

_2020-11-06_

* Initial release.

Thanks to @halilozercan for implementing Markdown support!