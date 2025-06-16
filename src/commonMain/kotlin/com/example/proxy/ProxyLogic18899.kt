package com.example.proxy

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest

// This function will be called from main.kt
internal suspend fun handleIn0Connection(
    selectorManager: SelectorManager,
    in0Socket: Socket,
    connectionId: Int
) {
    println("handleIn0Connection (#$connectionId) started for ${in0Socket.remoteAddress}")
    coroutineScope {
        // MutableStateFlow to hold the current out0Socket, allowing safe access from collector coroutine
        // Initialized to null, indicating no connection yet.
        val currentOut0SocketState = MutableStateFlow<Socket?>(null)

        val in0ReadChannel = in0Socket.openReadChannel()
        val in0WriteChannel = in0Socket.openWriteChannel(autoFlush = false)

        // Job to collect data from inN clients and send to the current out0Socket
        val fromInNtoOut0Job = launch {
            collectAndForwardInNToOut0(currentOut0SocketState, connectionId)
        }

        // Main loop for managing connection to the remote host (out0)
        manageRemoteConnectionLoop(
            selectorManager,
            in0Socket,
            in0ReadChannel,
            in0WriteChannel,
            currentOut0SocketState, // Pass the state flow here
            connectionId
        )

        fromInNtoOut0Job.cancelAndJoin() // Ensure collector job is stopped when this handler exits
    }
    println("handleIn0Connection (#$connectionId) for ${in0Socket.remoteAddress} fully completed.")
    in0Socket.close()
}

private suspend fun CoroutineScope.collectAndForwardInNToOut0(
    out0SocketState: MutableStateFlow<Socket?>, // Changed to MutableStateFlow
    connectionId: Int
) {
    dataFromInNToOut0.asSharedFlow().collectLatest { byteArray ->
        out0SocketState.value?.let { socket -> // Access socket via state flow
            if (!socket.isClosed && isActive) {
                try {
                    println("[IN_N -> OUT0 ${socket.remoteAddress}] (#$connectionId) Forwarding ${byteArray.size} bytes")
                    // Open a new write channel for each write, Ktor sockets are not thread-safe for concurrent writes on same channel instance
                    val writeChannel = socket.openWriteChannel(autoFlush = false)
                    writeChannel.writeFully(byteArray)
                    writeChannel.flush()
                } catch (e: Exception) {
                    println("Error writing to OUT0 from IN_N for in0 (#$connectionId): ${e.message}")
                    if (e is CancellationException) throw e
                    // Optionally, could try to signal error to close out0Socket or handle it
                }
            }
        }
    }
}

private suspend fun CoroutineScope.manageRemoteConnectionLoop(
    selectorManager: SelectorManager,
    in0Socket: Socket,
    in0ReadChannel: ByteReadChannel,
    in0WriteChannel: ByteWriteChannel,
    out0SocketState: MutableStateFlow<Socket?>, // Changed to MutableStateFlow
    connectionId: Int
) {
    while (isActive && !in0Socket.isClosed) {
        var currentOut0Socket: Socket? = null // Temporary variable for the current connection attempt
        try {
            println("Connecting to $REMOTE_HOST:$REMOTE_PORT for in0 (#$connectionId) ${in0Socket.remoteAddress}...")
            currentOut0Socket = aSocket(selectorManager).tcp().connect(REMOTE_HOST, REMOTE_PORT)
            out0SocketState.value = currentOut0Socket // Update the state flow with the new socket
            println("Connected to $REMOTE_HOST:$REMOTE_PORT (out0 for in0 (#$connectionId) ${in0Socket.remoteAddress})")

            val out0ReadChannel = currentOut0Socket.openReadChannel()
            val out0WriteChannel = currentOut0Socket.openWriteChannel(autoFlush = false)

            // Launch forwarding jobs for the current out0 connection
            val in0ToOut0Job = launch {
                forwardIn0ToOut0(in0ReadChannel, out0WriteChannel, connectionId, in0Socket.remoteAddress, currentOut0Socket)
            }
            val out0ToIn0Job = launch {
                forwardOut0ToIn0AndFanOut(out0ReadChannel, in0WriteChannel, connectionId, in0Socket.remoteAddress, currentOut0Socket)
            }

            joinAll(in0ToOut0Job, out0ToIn0Job) // Wait for either forwarding job to complete

        } catch (e: CancellationException) {
            println("manageRemoteConnectionLoop for in0 (#$connectionId) cancelled.")
            throw e
        } catch (e: Exception) {
            println("Error in OUT0 connection cycle for in0 (#$connectionId) ${in0Socket.remoteAddress} to $REMOTE_HOST:$REMOTE_PORT: ${e.message}")
        } finally {
            currentOut0Socket?.close() // Close the specific socket for this attempt
            out0SocketState.value = null // Clear the state flow as this socket is now closed
            if (!isActive || in0Socket.isClosed) {
                println("manageRemoteConnectionLoop for in0 (#$connectionId) stopping permanently.")
                break
            }
            println("Disconnected from $REMOTE_HOST:$REMOTE_PORT (for in0 #$connectionId ${in0Socket.remoteAddress}). Reconnecting in ${RECONNECT_DELAY_MS / 1000}s...")
            platformSpecificDelay(RECONNECT_DELAY_MS)
        }
    }
}

private suspend fun CoroutineScope.forwardIn0ToOut0(
    in0ReadChannel: ByteReadChannel,
    out0WriteChannel: ByteWriteChannel,
    connectionId: Int,
    in0RemoteAddress: NetworkAddress,
    out0Socket: Socket // Pass the specific socket for this job
) {
    try {
        val buffer = ByteArray(BUFFER_SIZE)
        while (isActive && !in0ReadChannel.isClosedForRead && !out0Socket.isClosed) {
            val bytesRead = in0ReadChannel.readAvailable(buffer)
            if (bytesRead > 0) {
                val actualData = if (bytesRead == buffer.size) buffer else buffer.copyOfRange(0, bytesRead)
                println("[IN0 (#$connectionId) $in0RemoteAddress -> OUT0 ${out0Socket.remoteAddress}] Forwarding ${actualData.size} bytes")
                out0WriteChannel.writeFully(actualData)
                out0WriteChannel.flush()
            } else if (bytesRead == -1) {
                println("IN0 (#$connectionId) $in0RemoteAddress closed its read channel.")
                break
            }
        }
    } catch (e: ClosedReceiveChannelException) {
        println("IN0 (#$connectionId) $in0RemoteAddress connection closed by client.")
    } catch (e: Exception) {
        println("Error IN0->OUT0 for in0 (#$connectionId) $in0RemoteAddress: ${e.message}")
        if (e is CancellationException) throw e
    } finally {
        println("IN0->OUT0 forwarder for in0 (#$connectionId) $in0RemoteAddress stopping.")
        // Don't close out0Socket here, it's managed by manageRemoteConnectionLoop
    }
}

private suspend fun CoroutineScope.forwardOut0ToIn0AndFanOut(
    out0ReadChannel: ByteReadChannel,
    in0WriteChannel: ByteWriteChannel,
    connectionId: Int,
    in0RemoteAddress: NetworkAddress,
    out0Socket: Socket // Pass the specific socket for this job
) {
    try {
        val buffer = ByteArray(BUFFER_SIZE)
        while (isActive && !out0ReadChannel.isClosedForRead && !in0WriteChannel.isClosedForWrite) {
            val bytesRead = out0ReadChannel.readAvailable(buffer)
            if (bytesRead > 0) {
                val actualData = if (bytesRead == buffer.size) buffer else buffer.copyOfRange(0, bytesRead)
                // Send to in0
                println("[OUT0 ${out0Socket.remoteAddress} -> IN0 (#$connectionId) $in0RemoteAddress] Forwarding ${actualData.size} bytes")
                in0WriteChannel.writeFully(actualData)
                in0WriteChannel.flush()
                // Broadcast to inN clients
                println("[OUT0 ${out0Socket.remoteAddress} -> IN_N] (#$connectionId) Broadcasting ${actualData.size} bytes")
                dataFromOut0ToInN.emit(actualData)
            } else if (bytesRead == -1) {
                println("OUT0 ${out0Socket.remoteAddress} (for in0 #$connectionId) closed its read channel.")
                break
            }
        }
    } catch (e: ClosedReceiveChannelException) {
        println("OUT0 connection (for in0 #$connectionId) $in0RemoteAddress closed by remote: $REMOTE_HOST:$REMOTE_PORT")
    } catch (e: Exception) {
        println("Error OUT0->IN0/IN_N for in0 (#$connectionId) $in0RemoteAddress: ${e.message}")
        if (e is CancellationException) throw e
    } finally {
        println("OUT0->IN0/IN_N forwarder for in0 (#$connectionId) $in0RemoteAddress stopping.")
        // Don't close out0Socket here, it's managed by manageRemoteConnectionLoop
    }
}
