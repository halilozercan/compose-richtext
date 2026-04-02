package com.halilibo.richtext.ui.util

import java.util.UUID

internal actual fun randomUUID(): String {
  return UUID.randomUUID().toString()
}