package com.example.proxy

import kotlinx.coroutines.flow.MutableSharedFlow

// Network Configuration
const val LOCAL_PORT_18899 = 18899
const val LOCAL_PORT_8899 = 8899
const val REMOTE_HOST = "www.myheatpump.com"
const val REMOTE_PORT = 18899 // Assuming remote port for out0 is also 18899
const val RECONNECT_DELAY_MS = 5000L
const val BUFFER_SIZE = 4096

// Shared flows for communication between in0 and inN clients
// Data from out0 (via in0) to all inN clients
val dataFromOut0ToInN = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 64)
// Data from any inN client to out0 (via in0)
val dataFromInNToOut0 = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 64)

// Connection counters (consider moving to a dedicated state holder if more complex state is needed)
var var_in0_connection_count = 0
var var_inN_connection_count = 0
