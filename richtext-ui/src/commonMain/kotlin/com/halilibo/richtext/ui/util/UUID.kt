package com.halilibo.richtext.ui.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal fun randomUUID(): String = Uuid.random().toString()