@file:Suppress("SameParameterValue")

package com.zachklipp.richtext.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceAround
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.ListType.Unordered
import com.halilibo.richtext.ui.material3.RichText
import com.zachklipp.richtext.ui.printing.Printable
import com.zachklipp.richtext.ui.printing.PrintableController
import com.zachklipp.richtext.ui.printing.hideWhenPrinting
import com.zachklipp.richtext.ui.printing.isBeingPrinted
import com.zachklipp.richtext.ui.printing.keepOnPageWithNext
import com.zachklipp.richtext.ui.printing.rememberPrintableController
import com.zachklipp.richtext.ui.printing.responsivePadding
import java.util.Locale
import kotlin.math.max

/** Dimension used for margins/spacing on "large" screens or paper. */
private val LargeGap = 96.dp

@Preview(showSystemUi = true)
@Composable private fun DocumentPhonePreview() {
  DocumentSample()
}

/**
 * Demonstrates a screen that is both displayable on device, and on print, and adjusts its
 * formatting for both.
 *
 * This sample doesn't actually use any of the formatting primitives defined in the richtext module,
 * it does everything from scratch. The sample primarily demonstrates using the [Printable]
 * composable for printing content from your UI.
 */
@Composable fun DocumentSample() {
  val printableController = rememberPrintableController()
  DocumentScreenContainer(printableController) {
    Column {
      ResponsiveSidebarLayout(
        body = { Title("Your Name") },
        columnSpacing = LargeGap,
        verticalSpacing = 16.dp,
        sidebar = {
          ContactInfo(
            "123 Your Street\n" +
                "Your City, ST 12345",
            "(123) 456-7890\n" +
                "no_reply@example.com"
          ) {
            val uriHandler = LocalUriHandler.current
            TextButton(onClick = { uriHandler.openUri("tel:1234567890") }) {
              Icon(Icons.Outlined.Phone, contentDescription = "Call")
            }
            TextButton(onClick = { uriHandler.openUri("mailto:no_reply@example.com") }) {
              Icon(Icons.Outlined.Email, contentDescription = "Email")
            }
          }
        }
      )
      Spacer(Modifier.size(12.dp))

      ResponsiveSidebarLayout(
        body = {
          Section("Experience") {
            LargeBlurb(
              "Company", "Location", "Job Title",
              "MONTH 20XX – PRESENT",
              "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh."
            )
            BulletBlurb(
              "Company", "Location", "Job Title",
              "MONTH 20XX – PRESENT",
              "Lorem ipsum dolor sit amet",
              "Consectetuer adipiscing elit",
              "Sed diam nonummy nibh."
            )
            LargeBlurb(
              "Company", "Location", "Job Title",
              "MONTH 20XX – PRESENT",
              "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh."
            )
          }

          Section("Education") {
            LargeBlurb(
              "School Name", "Location", "Degree",
              "MONTH 20XX – MONTH 20XX",
              "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh."
            )
          }
        },
        columnSpacing = LargeGap,
        sidebar = {
          Section("Skills", verticalArrangement = spacedBy(8.dp)) {
            Text("Lorem ipsum dolor sit amet.")
            Text("Consectetuer adipiscing elit.")
            Text("Sed diam nonummy nibh euismod tincidunt.")
            Text("Laoreet dolore magna aliquam erat volutpat.")
          }

          Section("Awards", verticalArrangement = spacedBy(8.dp)) {
            LinkBlurb(
              "Lorem ipsum dolor sit amet Consectetuer adipiscing elit, Sed diam nonummy",
              "https://example.com"
            )
          }
        }
      )

      val sourceUrl = "https://github.com/halilozercan/compose-richtext"
      Footer(
        if (isBeingPrinted) {
          "This document was built and printed with Compose. It is also available as an app."
        } else {
          "This document was built with Compose."
        } + "\nThe source code is available at $sourceUrl",
        sourceUrl,
        onPrintClicked = {
          printableController.print("Compose Document")
        }
      )
    }
  }
}

/**
 * Sets up a reasonable layout for printable document content, along with the [Printable] composable
 * that will define the printable area when the [printableController]'s [PrintableController.print]
 * method is called.
 */
@Composable fun DocumentScreenContainer(
  printableController: PrintableController,
  content: @Composable () -> Unit
) {
  Column(Modifier.verticalScroll(rememberScrollState())) {
    Printable(
      printableController,
      pageDpi = 96,
      modifier = Modifier.responsivePadding(
        600.dp to 32.dp,
        Dp.Infinity to LargeGap
      )
    ) {
      SampleTheme(
        colorScheme = lightColorScheme(
          primary = Color.Black,
          secondary = Color(0x20, 0x79, 0xc7)
        )
      ) {
        content()
      }
    }
  }
}

@Composable private fun Title(text: String) {
  Text(
    text,
    style = MaterialTheme.typography.headlineMedium,
    fontFamily = FontFamily.Serif,
    fontWeight = Bold
  )
}

@Composable private fun ContactInfo(
  address: String,
  contact: String,
  contactButtons: @Composable () -> Unit
) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = SpaceBetween) {
    Column {
      Text(address, fontSize = 13.sp)
      Spacer(Modifier.height(3.dp))
      Text(contact, fontSize = 13.sp, fontWeight = Bold)
    }

    // Buttons aren't clickable on paper.
    Column(Modifier.hideWhenPrinting()) {
      contactButtons()
    }
  }
}

/**
 * Defines a section with a header. A section contains a column of either [LargeBlurb]s or
 * other composables.
 */
// For Arrangement.Vertical.
@Composable private fun Section(
  title: String,
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  content: @Composable () -> Unit
) {
  val uppercaseTitle = remember(title) { title.uppercase(Locale.US) }

  Column(verticalArrangement = verticalArrangement) {
    Text(
      uppercaseTitle,
      color = Color(0x20, 0x79, 0xc7),
      fontWeight = Bold,
      style = MaterialTheme.typography.labelMedium,
      modifier = Modifier
        .keepOnPageWithNext()
        .padding(top = 32.dp, bottom = 8.dp)
    )
    ProvideTextStyle(
      value = TextStyle(fontFamily = FontFamily.Serif),
      content = content
    )
  }
}

/**
 * A paragraph describing a position, degree, or other single item on the document.
 */
@Composable private fun LargeBlurb(
  organization: String,
  location: String = "",
  title: String,
  duration: String = "",
  description: String
) {
  LargeBlurb(organization, location, title, duration) {
    Text(description)
  }
}

/**
 * A [LargeBlurb] whose description is a bullet list.
 */
@Composable private fun BulletBlurb(
  organization: String,
  location: String = "",
  title: String,
  duration: String = "",
  vararg descriptionBulletPoints: String
) {
  LargeBlurb(organization, location, title, duration) {
    // RichText works seamlessly with printing!
    RichText {
      FormattedList(Unordered, listOf(*descriptionBulletPoints)) {
        Text(it, fontSize = 13.sp)
      }
    }
  }
}

@Composable private fun LargeBlurb(
  organization: String,
  location: String = "",
  title: String,
  duration: String = "",
  description: @Composable () -> Unit
) {
  Column(Modifier.padding(top = 16.dp, bottom = 12.dp)) {
    Text(
      buildAnnotatedString {
        withStyle(SpanStyle(fontSize = 20.sp)) {
          withStyle(SpanStyle(fontWeight = Bold)) {
            append(organization.withHardSpaces())
            if (location.isNotBlank()) {
              append(",")
            }
          }

          if (location.isNotBlank()) {
            append(" ${location.withHardSpaces()}")
          }
          append(" — ")
          withStyle(SpanStyle(fontStyle = Italic)) {
            append(title.withHardSpaces())
          }
        }
      },
      modifier = Modifier.keepOnPageWithNext()
    )

    if (duration.isNotBlank()) {
      Text(
        modifier = Modifier
          .keepOnPageWithNext()
          .padding(top = 4.dp, bottom = 6.dp),
        fontFamily = FontFamily.SansSerif,
        color = Color.Gray,
        fontSize = 10.sp,
        lineHeight = 3.sp,
        text = duration
      )
    }

    description()
  }
}

/**
 * Text that is displayed as a clickable link on screen, and as the [text] with the [url] displayed
 * under it when printed.
 */
@Composable fun LinkBlurb(
  text: String,
  url: String
) {
  if (isBeingPrinted) {
    Column {
      Text(text)
      Text(
        url.substringAfter("://"),
        fontFamily = FontFamily.SansSerif,
        color = Color.Gray,
        fontSize = 12.sp,
        modifier = Modifier.padding(bottom = 8.dp)
      )
    }
  } else {
    val uriHandler = LocalUriHandler.current
    Text(
      text,
      textDecoration = Underline,
      modifier = Modifier.clickable(onClick = { uriHandler.openUri(url) })
    )
  }
}

@Composable private fun Footer(
  text: String,
  uri: String,
  onPrintClicked: () -> Unit
) {
  val uriHandler = LocalUriHandler.current

  Row(
    modifier = Modifier
      .padding(top = 16.dp)
      .fillMaxWidth()
      .wrapContentWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = SpaceAround
  ) {
    Text(
      buildAnnotatedString {
        val displayUri = uri.indexOf("://").takeIf { it > -1 }
          ?.let { uri.substring(it + 3) }
          ?: uri
        val displayText = text.replace(uri, displayUri)

        append(displayText)

        // Make the URI look clickable if it appears in the display text, and we're in interactive
        // mode.
        if (!isBeingPrinted) {
          val uriIndex = displayText.indexOf(displayUri)
          if (uriIndex > -1) {
            addStyle(
              SpanStyle(textDecoration = Underline),
              uriIndex, uriIndex + displayUri.length
            )
          }
        }
      },
      modifier = Modifier
        .clickable(onClick = { uriHandler.openUri(uri) })
        .weight(1f)
        .padding(vertical = 16.dp),
      textAlign = TextAlign.Center,
      fontSize = 9.sp,
      color = Color.Gray
    )
    Spacer(
      Modifier
        .size(4.dp)
        .hideWhenPrinting())
    OutlinedButton(
      modifier = Modifier.hideWhenPrinting(),
      onClick = onPrintClicked
    ) {
      Icon(Icons.Outlined.Print, contentDescription = "Print", Modifier.size(16.dp))
    }
  }
}

/**
 * Wide-narrow column layout for wide displays, falls back to stacking vertically if the smaller
 * column is less than [minColWidth]. Kind of like a weighted `FlowRow`.
 *
 * @param columnSpacing The amount of space between the columns, when side-by-side.
 * @param verticalSpacing The amount of space between the body and sidebare when stacking
 * vertically.
 * @param minColWidth The minimum width a column will be before collapsing to vertical.
 */
@Composable private fun ResponsiveSidebarLayout(
  bodyWidthFraction: Float = 2f / 3f,
  columnSpacing: Dp = 0.dp,
  verticalSpacing: Dp = 0.dp,
  minColWidth: Dp = 150.dp,
  body: @Composable ColumnScope.() -> Unit,
  sidebar: @Composable ColumnScope.() -> Unit,
) {
  Layout(
    content = {
      Column(content = body)
      Column(content = sidebar)
    }
  ) { measurables, constraints ->
    val widthWithoutSpace =
      constraints.constrainWidth(constraints.maxWidth - columnSpacing.roundToPx())
    val bodySideBySideWidth = (widthWithoutSpace * bodyWidthFraction)
    val sidebarSideBySideWidth = (widthWithoutSpace * (1 - bodyWidthFraction))
    val isSideBySide = bodySideBySideWidth >= minColWidth.toPx() &&
        sidebarSideBySideWidth >= minColWidth.toPx()

    // TODO Handle height constraints, handle non-zero min width.
    val bodyConstraints = if (isSideBySide) {
      // Remove the sidebar width.
      constraints.offset(horizontal = -(columnSpacing.roundToPx() + sidebarSideBySideWidth.toInt()))
    } else {
      constraints.offset(vertical = -(verticalSpacing / 2).roundToPx())
    }
    val sidebarConstraints = if (isSideBySide) {
      // Remove the body width.
      constraints.offset(horizontal = -(columnSpacing.roundToPx() + bodySideBySideWidth.toInt()))
    } else {
      constraints.offset(vertical = -(verticalSpacing / 2).roundToPx())
    }

    val (bodyMeasurable, sidebarMeasurable) = measurables
    val bodyPlaceable = bodyMeasurable.measure(bodyConstraints)
    val sidebarPlaceable = sidebarMeasurable.measure(sidebarConstraints)

    val width = constraints.constrainWidth(
      if (isSideBySide) {
        // Force full width.
        bodySideBySideWidth.toInt() + columnSpacing.roundToPx() + sidebarSideBySideWidth.toInt()
      } else {
        max(bodyPlaceable.width, sidebarPlaceable.width)
      }
    )
    val height = constraints.constrainHeight(
      if (isSideBySide) {
        max(bodyPlaceable.height, sidebarPlaceable.height)
      } else {
        bodyPlaceable.height + sidebarPlaceable.height
      }
    )

    layout(width, height) {
      bodyPlaceable.placeRelative(0, 0)
      if (isSideBySide) {
        sidebarPlaceable.place(bodySideBySideWidth.toInt() + columnSpacing.roundToPx(), 0)
      } else {
        sidebarPlaceable.place(0, bodyPlaceable.height + verticalSpacing.roundToPx())
      }
    }
  }
}

private fun String.withHardSpaces(): String = replace(' ', '\u00a0')
