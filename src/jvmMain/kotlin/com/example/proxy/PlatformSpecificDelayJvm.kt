package com.example.proxy

actual fun platformSpecificDelay(durationMillis: Long) {
    Thread.sleep(durationMillis)
}
