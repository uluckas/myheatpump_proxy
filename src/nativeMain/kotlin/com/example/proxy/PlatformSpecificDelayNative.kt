package com.example.proxy

import kotlin.native.concurrent.Thread

actual fun platformSpecificDelay(durationMillis: Long) {
    Thread.sleep(durationMillis)
}
