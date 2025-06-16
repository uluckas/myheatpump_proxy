package com.example.proxy

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest

// This function will be called from main.kt
internal suspend fun handleInNConnection(
    inNSocket: Socket,
    connectionId: Int
) {
    println("Handling inN connection (#$connectionId) from ${inNSocket.remoteAddress}")
    coroutineScope {
        val inNReadChannel = inNSocket.openReadChannel()
        val inNWriteChannel = inNSocket.openWriteChannel(autoFlush = false)

        // Job to forward data from shared broadcast (from out0 via in0) to this inN client
        val fromSharedToInNJobs = launch {
            forwardSharedToInN(inNWriteChannel, connectionId, inNSocket.remoteAddress, this@coroutineScope)
        }

        // Job to read data from this inN client and forward it to the shared flow for out0 (via in0)
        val fromInNToSharedJob = launch {
            forwardInNToShared(inNReadChannel, connectionId, inNSocket.remoteAddress, this@coroutineScope)
        }

        // Wait for both jobs, though cancellation of one by error will cancel the scope and thus the other.
        // joinAll(fromSharedToInNJobs, fromInNToSharedJob) // Not strictly needed due to scope cancellation
    }
    println("handleInNConnection (#$connectionId) for ${inNSocket.remoteAddress} completed.")
    inNSocket.close()
}

private suspend fun CoroutineScope.forwardSharedToInN(
    inNWriteChannel: ByteWriteChannel,
    connectionId: Int,
    inNRemoteAddress: NetworkAddress,
    parentScope: CoroutineScope // To cancel parent if a critical write error occurs
) {
    dataFromOut0ToInN.asSharedFlow().collectLatest { byteArray ->
        if (!inNWriteChannel.isClosedForWrite && isActive) {
            try {
                println("[IN0_BROADCAST -> IN_N (#$connectionId) $inNRemoteAddress] Forwarding ${byteArray.size} bytes")
                inNWriteChannel.writeFully(byteArray)
                inNWriteChannel.flush()
            } catch (e: Exception) {
                println("Error writing to IN_N (#$connectionId) $inNRemoteAddress: ${e.message}")
                parentScope.cancel("Write to inN (#$connectionId) failed", e) // Cancel the whole handleInNConnection
                if (e is CancellationException) throw e
            }
        } else {
            // Socket is closed or coroutine is no longer active, cancel collection.
            parentScope.cancel("inN (#$connectionId) $inNRemoteAddress socket closed or scope inactive for writing.")
        }
    }
}

private suspend fun CoroutineScope.forwardInNToShared(
    inNReadChannel: ByteReadChannel,
    connectionId: Int,
    inNRemoteAddress: NetworkAddress,
    parentScope: CoroutineScope // To cancel parent if a critical read error occurs
) {
    try {
        val buffer = ByteArray(BUFFER_SIZE)
        while (isActive && !inNReadChannel.isClosedForRead) {
            val bytesRead = inNReadChannel.readAvailable(buffer)
            if (bytesRead > 0) {
                val actualData = if (bytesRead == buffer.size) buffer else buffer.copyOfRange(0, bytesRead)
                println("[IN_N (#$connectionId) $inNRemoteAddress -> IN0_TARGET] Forwarding ${actualData.size} bytes")
                dataFromInNToOut0.emit(actualData)
            } else if (bytesRead == -1) {
                println("IN_N (#$connectionId) $inNRemoteAddress closed its read channel.")
                break
            }
        }
    } catch (e: ClosedReceiveChannelException) {
        println("IN_N (#$connectionId) $inNRemoteAddress connection closed by client.")
    } catch (e: Exception) {
        println("Error reading from IN_N (#$connectionId) $inNRemoteAddress: ${e.message}")
        if (e is CancellationException) throw e // Propagate if it's a cancellation
         parentScope.cancel("Error reading from IN_N (#$connectionId)", e) // Cancel parent on other errors
    } finally {
        println("IN_N (#$connectionId) $inNRemoteAddress forwarder (to shared) stopping.")
        // Ensure the parent scope is cancelled if this loop finishes (normally or by error)
        // to also stop the other forwarding job (fromSharedToInN).
        parentScope.cancel("inN (#$connectionId) $inNRemoteAddress read loop finished or errored")
    }
}
