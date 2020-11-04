# Slideshow

A library for presenting simple Powerpoint-like slideshows from a phone (e.g. you can share your
phone screen to a Google Hangout and present that way). Slides can contain any composable content,
although a few pre-fab scaffolds are provided for common slide layouts.

![slideshow demo](img/slideshow-demo.gif)

## Gradle

```groovy
dependencies {
  implementation "com.zachklipp.compose-richtext:slideshow:${richtext_version}"
}
```

## Setting up a slideshow

There is a single, simple entry point to this library, that takes a vararg of composable functions
that define your slides:

```kotlin
Slideshow(
  { /* First slide. */ },
  { /* Second slide. */ },
  { /* etc… */ },
)
```

The `Slideshow` composable will automatically lock your phone to portrait and enter immersive
fullscreen while it's composed. You can tap anywhere on the left or right of the screen to navigate.
Currently the only supported slide transition is crossfade, but it shouldn't be hard to make the
library more pluggable and support more advanced transition libraries (like
[this one](https://github.com/zach-klippenstein/compose-backstack)).

## Creating slides

Individual slides are centered by default, but you can put whatever you want in them. The library
has a few scaffolds for common slide layouts that you might find useful.

### `TitleSlide`

Very simple: a title and a subtitle, centered.

```kotlin
Slideshow(
  {
    TitleSlide(
      title = { Text("Title") },
      subtitle = { Text("Subtitle") },
    )
  },
)
```

### `BodySlide`

The `BodySlide` composable gives you a top header, bottom footer, and middle body slot to put
stuff into.

```kotlin
Slideshow(
  { … },
  {
    BodySlide(
      header = { Text("Header") },
      footer = { Text("Footer") },
      body = {
        WebComponent(…)
        // or something
      },
    )
  },
)
```

Slide scaffolds like `BodySlide` and `TitleSlide`, as well as some other aspects of slideshow
formatting like background color, are controlled by passing a `SlideshowTheme` to the `Slideshow`
composable.

### Animating content on a single slide

A corporate presentation wouldn't be a presentation without obtuse visual effects. The
`NavigableContentContainer` composable is a flexible primitive for building such effects. It takes
a slot inside of which `NavigableContent` composables define blocks of content that will be
shown or hidden by slide navigation. Each `NavigableContent` block gets a `State<Boolean>`
indicating whether content should be shown or not, and is free to show or hide content however it
likes. For example, Compose comes with the `AnimatedVisibility` composable out of the box, which
plays very nicely with this API. See the `SlideshowSample` to see it in action.

```kotlin
NavigableContentContainer {
  Column {
    // Show this right away.
    Text("First paragraph")

    // Only show this after tapping to advance the show, then fade it in.
    NavigableContent { visible ->
      val opacity = animate(if (visible) 1f else 0f)
      Text("Second paragraph", Modifier.drawOpacity(opacity))
    }
  }
}
```

## Running the show

If you're in the middle of a presentation and lose your place, just drag up anywhere on the screen.
A slider and preview will pop up to let you scrub through the deck.

![slideshow scrubbing demo](img/slideshow-scrubbing-demo.gif)
