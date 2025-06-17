package com.example.proxy

actual fun platformSpecificDelay(durationMillis: Long) {
    // For Node.js, a simple spin-wait or a more complex async delay can be used.
    // However, within runBlocking, true async delay is tricky.
    // For simplicity in this step, Thread.sleep equivalent is not directly available.
    // This will be a busy wait, not ideal for production Node.js.
    val start = js("Date.now()")
    while (js("Date.now()") - start < durationMillis) {
        // busy wait
    }
}
