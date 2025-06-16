package com.example.proxy

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
// Removed some direct IO & Flow imports if they are only used in the Logic files now
// import io.ktor.utils.io.core.* // Only used in Logic files
import kotlinx.coroutines.*
// SharedFlows are in Constants.kt, so direct import here might not be needed unless used directly
// import kotlinx.coroutines.flow.MutableSharedFlow
// import kotlinx.coroutines.flow.asSharedFlow
// import kotlinx.coroutines.flow.collectLatest

// platformSpecificDelay is still needed here as it's used by functions in other files within this package/module
expect fun platformSpecificDelay(durationMillis: Long)

// Constants, shared flows, and connection counters are now in Constants.kt

// Dedicated scope for the application's lifecycle
private val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
// Connection counters are now in Constants.kt, accessed via their qualified name if needed,
// or directly if top-level in the same package.

fun main() {
    println("Starting proxy application...")
    println(" - Primary proxy (in0): localhost:$LOCAL_PORT_18899 <-> $REMOTE_HOST:$REMOTE_PORT")
    println(" - Fan-out listener (inN): localhost:$LOCAL_PORT_8899 (connects to primary's data flow)")

    // Single SelectorManager for the entire application
    val selectorManager = SelectorManager(Dispatchers.IO)

    runBlocking { // Use runBlocking here to keep the main function alive
        // Server for port 18899 (in0)
        applicationScope.launch {
            try {
                val serverSocket18899 = aSocket(selectorManager).tcp().bind(port = LOCAL_PORT_18899)
                println("Successfully listening on port $LOCAL_PORT_18899 for in0 connection")
                while (isActive) {
                    val in0Socket = serverSocket18899.accept()
                    val currentIn0Count = ++var_in0_connection_count // Using from Constants.kt
                    println("Accepted in0 connection (#$currentIn0Count) from ${in0Socket.remoteAddress}")
                    applicationScope.launch {
                        // Call refactored function from ProxyLogic18899.kt
                        handleIn0Connection(selectorManager, in0Socket, currentIn0Count)
                    }
                }
            } catch (e: CancellationException) {
                println("Server on port $LOCAL_PORT_18899 cancelled.")
                // throw e // Avoid rethrowing cancellation if it's meant to stop this server instance only
            } catch (e: Exception) {
                println("CRITICAL ERROR: Could not start or run server on port $LOCAL_PORT_18899: ${e.message}")
                applicationScope.cancel("Critical error in server 18899", e)
            } finally {
                println("Server loop on port $LOCAL_PORT_18899 stopped.")
            }
        }

        // Server for port 8899 (inN clients)
        applicationScope.launch {
            try {
                val serverSocket8899 = aSocket(selectorManager).tcp().bind(port = LOCAL_PORT_8899)
                println("Successfully listening on port $LOCAL_PORT_8899 for inN connections")
                while (isActive) {
                    val inNSocket = serverSocket8899.accept()
                    val currentInNCount = ++var_inN_connection_count // Using from Constants.kt
                    println("Accepted inN connection (#$currentInNCount) from ${inNSocket.remoteAddress}")
                    applicationScope.launch {
                        // Call refactored function from ProxyLogic8899.kt
                        handleInNConnection(inNSocket, currentInNCount)
                    }
                }
            } catch (e: CancellationException) {
                println("Server on port $LOCAL_PORT_8899 cancelled.")
                // throw e // Avoid rethrowing
            } catch (e: Exception) {
                println("CRITICAL ERROR: Could not start or run server on port $LOCAL_PORT_8899: ${e.message}")
                applicationScope.cancel("Critical error in server 8899", e)
            } finally {
                println("Server loop on port $LOCAL_PORT_8899 stopped.")
            }
        }

        try {
            applicationScope.coroutineContext[Job]?.join()
        } catch (e: CancellationException) {
            println("Application scope was cancelled. Main job joining was interrupted.")
        }
        println("Proxy application shutting down or all main server jobs completed.")
    }
    println("Proxy application main function finished.")
}
