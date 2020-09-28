package com.zachklipp.richtext.sample

import androidx.compose.foundation.Icon
import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandlerAmbient
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.annotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zachklipp.richtext.ui.printing.isBeingPrinted
import com.zachklipp.richtext.ui.printing.rememberPrintableController

@Composable fun Resume() {
  val printableController = rememberPrintableController()
  val openUri = UriHandlerAmbient.current::openUri

  DocumentScreenContainer(printableController) {
    Column {
      ResponsiveSidebarLayout(
        body = { Title("Zach Klippenstein") },
        columnSpacing = LargeGap,
        verticalSpacing = 16.dp,
        sidebar = {
          ContactInfo(
            "San Francisco, CA",
            "(415) 881-0288\n" +
                "zach.klippenstein@gmail.com\n" +
                "zachklipp.com"
          ) {
            TextButton(onClick = { openUri("mailto:zach.klippenstein@gmail.com") }) {
              Icon(Icons.Outlined.Email)
            }
            TextButton(onClick = { openUri("http://zachklipp.com") }) {
              Icon(Icons.Outlined.Language)
            }
          }
        }
      )
    }
    Spacer(Modifier.size(12.dp))

    ResponsiveSidebarLayout(
      body = {
        Section("Experience") {
          BulletBlurb(
            "Square", "San Francisco, CA", "Software Engineer",
            "NOVEMBER 2014 – PRESENT",
            "As a platform developer, built mobile infrastructure such as Workflow (our " +
                "declarative, reactive, composable state machine library), and an analytics " +
                "logging library for an internal analytics service.",
            "Produced written training content for technologies like Workflow, Kotlin, " +
                "RxJava, coroutines.",
            "Facilitated biweekly lightning talks as well annual training courses and " +
                "developer summits, recruiting and coordinating speakers, planning " +
                "curriculums, recording and editing talks, and supervising workshops.",
            "Drove and helped support internal adoption of new technologies like Kotlin, " +
                "coroutines, and Compose.",
            "Helped open source a number of projects (Workflow, Cycler, Anvil, Radiography) " +
                "and contributed to those and others (Okio, Phrase, Kotlin coroutines, " +
                "Compose).",
            "As a feature developer, built a number of customer-facing features for Point of " +
                "Sale apps, including receipt rendering and printing, paper tip entry, and " +
                "card-on-file.",
            "Worked directly with third-party merchants to test beta features, gather " +
                "product requirements, and troubleshoot hardware and software issues on-site.",
            "Managed Play Store releases and triaged production crashes and bug reports.",
          )
          BulletBlurb(
            "Square", "San Francisco, CA", "Tech Lead Manager",
            "JUNE 2016 – MARCH 2017",
            "Managed a small team of 3-4 Android engineers.",
            "Completed introductory manager training programs.",
            "Performed semi-annual 360° reviews, and drove the successful promotion of one of my " +
                "reports.",
            "Helped an engineer transfer from a QA testing role into a full engineering role by " +
                "creating transfer criteria and coaching them through the process.",
            "Actively recruited for my team and sister teams, researching and reaching out to " +
                "potential candidates.",
            "Facilitated an early exploration into adopting build system alternatives to gradle, " +
                "resulting in eventually migrating to Buck for a couple years.",
          )
          BulletBlurb(
            "Amazon", "Seattle, WA", "SDE 1",
            "JUNE 2013 – NOVEMBER 2014",
            "Led design of technical specification for new external API.",
            "Integrated Amazon’s payment platform with two payment processing companies.",
            "Performed operational maintenance on services owned by my team as part of a 24-hour " +
                "on-call rotation.",
            "Worked with technical and business contacts from numerous external companies on " +
                "projects and operational emergencies.",
            "Ported legacy service monitor tool to run on AWS SQS/SNS.",
          )
        }

        Section("Education") {
          LargeBlurb(
            organization = "University of Manitoba",
            location = "Winnipeg, MB, Canada",
            title = "Bachelor of Computer Science",
            duration = "SEPTEMBER 2007 – JUNE 2013",
            "Graduated with honors."
          )
        }
      },
      columnSpacing = LargeGap,
      sidebar = {
        Section("Public Speaking", verticalArrangement = Arrangement.spacedBy(8.dp)) {
          LinkBlurb(
            "Safer Kotlin w/ Structured Concurrency",
            "https://youtu.be/OqOGze29xPQ"
          )
          LinkBlurb(
            "RxJava.map(Flow) - Using Flow in an RxJava codebase",
            "https://youtu.be/ut0aZWAUSVo"
          )
          LinkBlurb(
            "Droidcon 2019: Square Workflow",
            "https://www.droidcon.com/media-detail?video=362741019"
          )
          LinkBlurb(
            "Android at Scale panel discussion with Pinterest, Square, Twitter, & Uber",
            "https://youtu.be/Ps8YNET84lU"
          )
        }

        Section("Open Source Work", verticalArrangement = Arrangement.spacedBy(8.dp)) {
          LinkBlurb(
            "Radiography (Square)",
            "https://github.com/square/radiography"
          )
          LinkBlurb(
            "Workflow (Square)",
            "http://github.com/square/workflow-kotlin"
          )
          LinkBlurb(
            "Compose Backstack (Personal)",
            "https://github.com/zach-klippenstein/compose-backstack"
          )
          LinkBlurb(
            "Compose RichText (Personal)",
            "https://github.com/zach-klippenstein/compose-richtext"
          )
        }

        if (isBeingPrinted) {
          Section("One more thing…", verticalArrangement = spacedBy(8.dp)) {
            ProvideTextStyle(TextStyle(fontSize = 13.sp)) {
              Text(annotatedString {
                append("This document is also an app. Clone the ")
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                  append("zachklipp/resume")
                }
                append(" branch in the ")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                  append("github.com/zach-klippenstein/compose-richtext")
                }
                append(" repository, and run the following command to install the app:")
              })
              Text(
                "./gradlew :sample:installDebug",
                fontFamily = FontFamily.Monospace
              )
              Text("See the single commit on that branch for the source code.")
            }
          }
        }
      }
    )

    val sourceUrl = "https://github.com/zach-klippenstein/compose-richtext"
    Footer(
      if (isBeingPrinted) {
        "This document was built and printed with Compose. It is also available as an app."
      } else {
        "This document was built with Compose."
      } + "\nThe source code is available at $sourceUrl",
      sourceUrl,
      onPrintClicked = {
        printableController.print("Zach Klippenstein Resume 2020")
      }
    )
  }
}