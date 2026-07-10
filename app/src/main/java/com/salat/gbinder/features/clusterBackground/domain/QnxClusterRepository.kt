package com.salat.gbinder.features.clusterBackground.domain

// Environment gate result - every destructive action must be blocked until this passes
data class QnxEnvProbe(
    val androidShellReady: Boolean,
    val sharedWritable: Boolean,
    val qnxReachable: Boolean,
    val backgroundExists: Boolean,
    val backgroundSize: Long,
    val checksumTool: String?,
    val appsDevice: String?,
    val details: String
) {
    // Read is safe once we can reach QNX and see the file over the writable bridge
    val canBackup: Boolean
        get() = androidShellReady && sharedWritable && qnxReachable && backgroundExists && backgroundSize > 0

    // Write additionally needs the /apps mount device to remount read-write
    val canWrite: Boolean
        get() = canBackup && appsDevice != null
}

// Progress callback: fraction in 0..1 and a short human-readable step label
typealias QnxProgress = (Float, String) -> Unit

interface QnxClusterRepository {

    suspend fun probe(): QnxEnvProbe

    // Pulls the current background.kzb out of QNX and returns its verified bytes
    suspend fun backupToBytes(onProgress: QnxProgress): ByteArray

    // Writes a prepared background.kzb into QNX with backup and checksum verification, then reboots
    // the cluster only when the on-device copy matches - used by both restore and install
    suspend fun pushAndReboot(bytes: ByteArray, onProgress: QnxProgress)
}
