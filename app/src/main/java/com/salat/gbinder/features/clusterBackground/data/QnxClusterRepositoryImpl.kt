package com.salat.gbinder.features.clusterBackground.data

import android.util.Base64
import com.salat.gbinder.adb.domain.repository.AdbRepository
import com.salat.gbinder.features.clusterBackground.domain.QnxClusterException
import com.salat.gbinder.features.clusterBackground.domain.QnxClusterRepository
import com.salat.gbinder.features.clusterBackground.domain.QnxEnvProbe
import com.salat.gbinder.features.clusterBackground.domain.QnxErrorCode
import com.salat.gbinder.features.clusterBackground.domain.QnxProgress
import com.salat.gbinder.statekeeper.domain.repository.StateKeeperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

// Drives the cluster background swap over GInputBridge's existing privileged shell
// Design mirrors the proven community russification: files move through the NFS /shared bridge and
// the only QNX writes are remount and cp of an already-verified file, with a rollback copy kept next
// to the target and a checksum gate before any reboot
class QnxClusterRepositoryImpl(
    private val adb: AdbRepository,
    private val stateKeeper: StateKeeperRepository
) : QnxClusterRepository {

    private data class ShResult(val output: String, val exit: Int, val ok: Boolean)

    // Deep log for QNX cluster ops - surfaces the failing step in the app log stream
    private fun log(msg: String) = stateKeeper.sendLog("[QNX] $msg", true)

    override suspend fun probe(): QnxEnvProbe = withContext(Dispatchers.IO) {
        val shellReady = sh("echo ${MARK}RDY").output.contains("${MARK}RDY")
        log("probe: shellReady=$shellReady")
        if (!shellReady) {
            return@withContext QnxEnvProbe(
                androidShellReady = false,
                sharedWritable = false,
                qnxReachable = false,
                backgroundExists = false,
                backgroundSize = 0,
                checksumTool = null,
                appsDevice = null,
                details = "Android shell/ADB helper is not available"
            )
        }

        val sharedWritable = sh(
            "mkdir -p ${QnxPaths.ANDROID_SHARED_DIR}; " +
                    ": > ${QnxPaths.ANDROID_SHARED_DIR}.gibtest && rm -f ${QnxPaths.ANDROID_SHARED_DIR}.gibtest && echo ${MARK}W"
        ).output.contains("${MARK}W")
        log("probe: sharedWritable=$sharedWritable")

        // Single telnet session gathers everything - size, checksum tool and /apps mount device
        val (qnxReachable, infoRes) = qnx(
            "echo ${MARK}SZ",
            "wc -c < ${QnxPaths.TARGET} 2>/dev/null",
            "echo ${MARK}TOOL",
            "{ md5sum ${QnxPaths.TARGET} >/dev/null 2>&1 && echo md5sum; } || " +
                    "{ cksum ${QnxPaths.TARGET} >/dev/null 2>&1 && echo cksum; } || echo none",
            "echo ${MARK}MNT",
            "mount 2>/dev/null"
        )
        val info = infoRes.output
        log("probe: qnxReachable=$qnxReachable")
        if (!qnxReachable) {
            log("probe: qnx info tail=${info.takeLast(200)}")
            return@withContext QnxEnvProbe(
                androidShellReady = true,
                sharedWritable = sharedWritable,
                qnxReachable = false,
                backgroundExists = false,
                backgroundSize = 0,
                checksumTool = null,
                appsDevice = null,
                details = "QNX cluster is not reachable over telnet"
            )
        }

        // Clamp to a sane range - an implausible value means the read was polluted, not a real size
        val rawSize = firstLong(section(info, "${MARK}SZ", "${MARK}TOOL")) ?: 0L
        val size = if (rawSize in 1..MAX_BG_BYTES) rawSize else 0L
        val checksumTool = when (section(info, "${MARK}TOOL", "${MARK}MNT").trim()) {
            "md5sum" -> "md5sum"
            "cksum" -> "cksum"
            else -> null
        }
        val appsDevice = parseAppsDevice(section(info, "${MARK}MNT", null))

        QnxEnvProbe(
            androidShellReady = true,
            sharedWritable = sharedWritable,
            qnxReachable = true,
            backgroundExists = size > 0,
            backgroundSize = size,
            checksumTool = checksumTool,
            appsDevice = appsDevice,
            details = "size=$size checksum=${checksumTool ?: "none"} appsDev=${appsDevice ?: "unknown"}"
        ).also { log("probe: ${it.details}") }
    }

    private fun section(text: String, start: String, end: String?): String {
        val s = text.indexOf(start)
        if (s < 0) return ""
        val from = s + start.length
        val to = if (end == null) text.length else text.indexOf(end, from).let { if (it < 0) text.length else it }
        return text.substring(from, to)
    }

    private fun parseAppsDevice(mountText: String): String {
        val line = mountText.lineSequence().firstOrNull { it.contains("/apps") }
        val device = line?.split(Regex("\\s+"))?.firstOrNull { it.startsWith("/dev") }
        // Fall back to the reference-firmware device only when discovery finds nothing
        return device ?: QnxPaths.FALLBACK_APPS_DEVICE
    }

    override suspend fun backupToBytes(onProgress: QnxProgress): ByteArray = withContext(Dispatchers.IO) {
        log("backup: start")
        onProgress(0f, "probe")
        val probe = probe()
        log(
            "backup: probe shellReady=${probe.androidShellReady} sharedWritable=${probe.sharedWritable} " +
                    "qnxReachable=${probe.qnxReachable} backgroundExists=${probe.backgroundExists} size=${probe.backgroundSize}"
        )
        if (!probe.androidShellReady) {
            log("backup: fail SHELL_UNAVAILABLE")
            throw QnxClusterException(QnxErrorCode.SHELL_UNAVAILABLE, "shell unavailable")
        }
        if (!probe.sharedWritable) {
            log("backup: fail SHARED_NOT_WRITABLE")
            throw QnxClusterException(QnxErrorCode.SHARED_NOT_WRITABLE, "shared not writable")
        }
        if (!probe.qnxReachable) {
            log("backup: fail QNX_UNREACHABLE")
            throw QnxClusterException(QnxErrorCode.QNX_UNREACHABLE, "qnx unreachable")
        }
        if (!probe.backgroundExists) {
            log("backup: fail TARGET_MISSING")
            throw QnxClusterException(QnxErrorCode.TARGET_MISSING, "target missing")
        }

        // Fail early if the tmpfs-backed /shared cannot hold the archive - the community filled it
        ensureFreeSpace(QnxPaths.QNX_SHARED_BG, probe.backgroundSize, "/shared")

        // Stage into the shared bridge and capture the ORIGINAL checksum in the same session so the
        // backup can be verified against /apps, not against the transit copy of itself
        onProgress(0.05f, "copy to shared")
        log("backup: copy to shared start")
        val verifyOrig = if (probe.checksumTool == "md5sum") "md5sum ${QnxPaths.TARGET}" else "wc -c < ${QnxPaths.TARGET}"
        val (_, cp) = qnx(
            "cp ${QnxPaths.TARGET} ${QnxPaths.QNX_SHARED_BG}; RC=\$?",
            verifyOrig,
            "echo ${MARK}RC\$RC"
        )
        val cpRc = intAfter(cp.output, "${MARK}RC")
        log("backup: cp rc=$cpRc out=${cp.output.takeLast(200)}")
        if (cpRc != 0) {
            cleanupShared()
            throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "cp to /shared failed: ${cp.output.takeLast(200)}")
        }

        log("backup: download start size=${probe.backgroundSize}")
        val bytes = androidReadBytes(QnxPaths.ANDROID_SHARED_BG, probe.backgroundSize) { f ->
            onProgress(0.1f + 0.8f * f, "download")
        }

        // Verify the pulled backup against the cluster original, by size always and by md5 if available
        onProgress(0.92f, "verify")
        log("backup: verify downloaded=${bytes.size} expected=${probe.backgroundSize}")
        if (bytes.size.toLong() != probe.backgroundSize) {
            cleanupShared()
            throw QnxClusterException(QnxErrorCode.VERIFY_MISMATCH, "backup size ${bytes.size} != ${probe.backgroundSize}")
        }
        if (probe.checksumTool == "md5sum") {
            val originalMd5 = firstHex(cp.output)
            val matches = originalMd5 != null && originalMd5.equals(md5Hex(bytes), ignoreCase = true)
            log("backup: md5 original=$originalMd5 matches=$matches")
            if (!matches) {
                cleanupShared()
                throw QnxClusterException(QnxErrorCode.VERIFY_MISMATCH, "backup does not match cluster original")
            }
        }

        // Do not leave a 7 MB file in the tmpfs-backed /shared - it caused black covers before
        cleanupShared()
        onProgress(1f, "done")
        log("backup: done bytes=${bytes.size}")
        bytes
    }

    override suspend fun pushAndReboot(bytes: ByteArray, onProgress: QnxProgress) = withContext(Dispatchers.IO) {
        log("push: start bytes=${bytes.size}")
        if (!KzbMagic.hasMagic(bytes)) {
            log("push: fail INVALID_FILE")
            throw QnxClusterException(QnxErrorCode.INVALID_FILE, "not a KZBF file")
        }

        onProgress(0f, "probe")
        val probe = probe()
        log(
            "push: probe qnxReachable=${probe.qnxReachable} sharedWritable=${probe.sharedWritable} " +
                    "appsDevice=${probe.appsDevice}"
        )
        if (!probe.qnxReachable) {
            log("push: fail QNX_UNREACHABLE")
            throw QnxClusterException(QnxErrorCode.QNX_UNREACHABLE, "qnx unreachable")
        }
        if (!probe.sharedWritable) {
            log("push: fail SHARED_NOT_WRITABLE")
            throw QnxClusterException(QnxErrorCode.SHARED_NOT_WRITABLE, "shared not writable")
        }
        val device = probe.appsDevice ?: run {
            log("push: fail REMOUNT_FAILED apps mount device unknown")
            throw QnxClusterException(QnxErrorCode.REMOUNT_FAILED, "apps mount device unknown")
        }

        // Refuse before touching anything if the tmpfs bridge cannot hold the archive
        ensureFreeSpace(QnxPaths.QNX_SHARED_BG, bytes.size.toLong(), "/shared")

        // 1) Push bytes onto the Android side of the bridge and verify locally
        log("push: upload start")
        androidWriteBytes(QnxPaths.ANDROID_SHARED_BG, bytes) { f ->
            onProgress(0.05f + 0.5f * f, "upload")
        }
        onProgress(0.58f, "verify upload")
        val localMd5 = md5Hex(bytes)
        val sharedMd5 = androidMd5(QnxPaths.ANDROID_SHARED_BG)
        log("push: upload verify local=$localMd5 shared=$sharedMd5")
        if (sharedMd5 == null || !sharedMd5.equals(localMd5, ignoreCase = true)) {
            cleanupShared()
            throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "upload checksum mismatch")
        }

        // 2) The one destructive step - identical shape to the russification: remount rw, keep a
        // rollback copy, cp the ready file, then always remount ro even if the cp failed
        onProgress(0.65f, "install")
        log("push: install start device=$device")
        val verifyCmd = if (probe.checksumTool == "md5sum") {
            "md5sum ${QnxPaths.TARGET}"
        } else {
            "wc -c < ${QnxPaths.TARGET}"
        }
        // Only overwrite the target if the rollback copy was written first - guards against a full
        // /apps leaving both the target and its backup corrupt; remount ro always runs
        // Each compound stays on its own line to survive the QNX tty line limit - RC/BRC persist
        // across lines in the same telnet login shell, and remount ro always runs after the if
        val (_, res) = qnx(
            "mount -o remount,rw $device /apps",
            "cp ${QnxPaths.TARGET} ${QnxPaths.TARGET_BAK}; BRC=\$?",
            "if [ \$BRC = 0 ]; then cp ${QnxPaths.QNX_SHARED_BG} ${QnxPaths.TARGET}; RC=\$?; else RC=99; fi",
            "sync",
            "mount -o remount,ro $device /apps",
            verifyCmd,
            "echo ${MARK}RC\$RC"
        )
        log("push: install session out=${res.output.takeLast(200)}")
        if (!res.output.contains("${MARK}RC")) {
            cleanupShared()
            throw QnxClusterException(QnxErrorCode.REMOUNT_FAILED, "install session failed: ${res.output.takeLast(200)}")
        }
        val rc = intAfter(res.output, "${MARK}RC")
        log("push: install rc=$rc")
        if (rc == 99) {
            // Backup copy failed so the target was never touched - nothing to roll back
            cleanupShared()
            throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "backup copy failed, target untouched")
        }
        if (rc != 0) {
            log("push: install failed, rolling back")
            rollback(device)
            cleanupShared()
            throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "cp to /apps failed rc=$rc")
        }

        // 3) Gate the reboot on a matching target
        onProgress(0.9f, "verify target")
        val verified = if (probe.checksumTool == "md5sum") {
            val targetMd5 = firstHex(res.output)
            targetMd5 != null && targetMd5.equals(localMd5, ignoreCase = true)
        } else {
            val targetSize = firstLong(res.output)
            targetSize == bytes.size.toLong()
        }
        log("push: verify target verified=$verified")
        if (!verified) {
            log("push: target mismatch, rolling back")
            rollback(device)
            cleanupShared()
            throw QnxClusterException(QnxErrorCode.VERIFY_MISMATCH, "installed target does not match")
        }

        cleanupShared()
        onProgress(0.96f, "reboot")
        log("push: rebooting")
        // Reboot drops the session; its result is intentionally ignored
        qnx("shutdown -S reboot")
        onProgress(1f, "done")
        log("push: done")
    }

    private suspend fun rollback(device: String) {
        Timber.w("[QNX] rolling back cluster background from backup")
        qnx(
            "mount -o remount,rw $device /apps",
            "cp ${QnxPaths.TARGET_BAK} ${QnxPaths.TARGET}",
            "sync",
            "mount -o remount,ro $device /apps"
        )
    }

    private suspend fun cleanupShared() {
        sh("rm -f ${QnxPaths.ANDROID_SHARED_BG} ${QnxPaths.ANDROID_SHARED_B64}")
    }

    // --- Android-side file transfer over the persistent shell ---

    private suspend fun androidWriteBytes(path: String, bytes: ByteArray, onProgress: (Float) -> Unit) {
        val decoder = androidBase64Decoder()
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        if (!sh(": > $path").ok) {
            throw QnxClusterException(QnxErrorCode.SHARED_NOT_WRITABLE, "cannot open $path")
        }
        var offset = 0
        val total = b64.length
        // Chunk length is a multiple of 4, so each base64 chunk decodes cleanly on its own - this
        // decodes straight into the target and never stores a full base64 copy in the tmpfs bridge
        while (offset < total) {
            val end = minOf(offset + WRITE_B64_CHUNK, total)
            val chunk = b64.substring(offset, end)
            val r = sh("printf %s '$chunk' | $decoder >> $path")
            if (!r.ok) throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "chunk write failed")
            offset = end
            onProgress(offset.toFloat() / total)
        }
    }

    // Picks the base64 decoder present on this Android build
    private suspend fun androidBase64Decoder(): String {
        if (sh("printf %s 'aGk=' | base64 -d").output.contains("hi")) return "base64 -d"
        if (sh("printf %s 'aGk=' | toybox base64 -d").output.contains("hi")) return "toybox base64 -d"
        return "base64 -d"
    }

    // Pre-flight space guard - best effort, skips silently if df output cannot be parsed since the
    // post-write checksum verify is the real gate
    private suspend fun ensureFreeSpace(pathOnFs: String, needBytes: Long, label: String) {
        val free = qnxFreeBytes(pathOnFs) ?: return
        if (free < needBytes) {
            throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "not enough space on $label: $free < $needBytes")
        }
    }

    private suspend fun qnxFreeBytes(path: String): Long? {
        val out = qnx("df $path 2>/dev/null").second.output
        val line = out.lineSequence().lastOrNull { it.isNotBlank() && !it.contains("Filesystem") } ?: return null
        val cols = line.trim().split(Regex("\\s+"))
        // Robust to busybox wrapping long device names: Available is the column before the Use% one
        val usePctIdx = cols.indexOfFirst { it.matches(Regex("\\d+%")) }
        val availKb = if (usePctIdx >= 1) cols[usePctIdx - 1].toLongOrNull() else cols.getOrNull(3)?.toLongOrNull()
        return availKb?.let { it * 1024 }
    }

    private suspend fun androidReadBytes(path: String, size: Long, onProgress: (Float) -> Unit): ByteArray {
        // Guard against a mis-parsed size driving an unbounded read or an Int overflow
        if (size <= 0 || size > MAX_BG_BYTES) {
            throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "implausible size $size")
        }
        val out = ByteArrayOutputStream(size.toInt().coerceAtLeast(16))
        val chunks = ((size + READ_CHUNK_BYTES - 1) / READ_CHUNK_BYTES).toInt().coerceAtLeast(1)
        for (i in 0 until chunks) {
            val r = sh("dd if=$path bs=$READ_CHUNK_BYTES skip=$i count=1 2>/dev/null | base64")
            val clean = r.output.filterNot { it.isWhitespace() }
            if (clean.isEmpty() && out.size().toLong() < size) {
                throw QnxClusterException(QnxErrorCode.TRANSFER_FAILED, "empty chunk $i")
            }
            out.write(Base64.decode(clean, Base64.NO_WRAP))
            onProgress((i + 1).toFloat() / chunks)
        }
        val bytes = out.toByteArray()
        return if (bytes.size.toLong() > size) bytes.copyOf(size.toInt()) else bytes
    }

    private suspend fun androidMd5(path: String): String? {
        val r = sh("md5sum $path 2>/dev/null || toybox md5sum $path")
        return firstHex(r.output)
    }

    // --- Shell plumbing ---

    // Runs one command on the Android shell and returns its output plus real exit code, isolated
    // from banner noise by unique start/end markers
    private suspend fun sh(cmd: String): ShResult {
        val id = MARK + System.nanoTime().toString()
        // Telnet transport is an echoing PTY and its single-line echo strip fails on long or wrapped
        // commands - emit markers via a shell var so the echoed command shows only ${G} not the id
        val wrapped = "G=$id; echo \${G}S; ( $cmd ) 2>&1; echo \${G}E\$?"
        val raw = adb.execute(wrapped)
        return parseMarked(raw, "${id}S", "${id}E")
    }

    // Runs QNX command lines over telnet, trying su first (the community scripts elevated before
    // opening telnet) and falling back to a direct telnet session. Returns the reached flag so the
    // probe can tell a real session apart from command echo
    private suspend fun qnx(vararg lines: String): Pair<Boolean, ShResult> {
        val elevated = qnxVia(elevate = true, lines = lines)
        if (elevated.first) return elevated
        log("qnx: su session not reached, retrying direct telnet")
        return qnxVia(elevate = false, lines = lines)
    }

    // Opens a telnet session to the cluster and runs each line as its own short command, mirroring
    // the community scripts - su first (or direct telnet on retry), the login timing known to work,
    // stty -echo to keep the QNX command echo out of the body, and markers via a shell var so any
    // residual echo never matches the parsed marker. Returns whether the session reached the cluster
    private suspend fun qnxVia(elevate: Boolean, lines: Array<out String>): Pair<Boolean, ShResult> {
        val id = QMARK + System.nanoTime().toString()
        val launcher = if (elevate) "su" else "busybox telnet ${QnxPaths.HOST} 2>/dev/null"
        val block = buildString {
            append("{ ")
            // su reads this block as a root shell so telnet is launched from inside it - direct mode
            // already has telnet as the pipe target and only waits for it to connect
            if (elevate) append("echo 'busybox telnet ${QnxPaths.HOST}'; ")
            append("sleep $TELNET_CONNECT_S; ")
            append("echo '${QnxPaths.USER}'; sleep $LOGIN_STEP_S; ")
            append("echo '${QnxPaths.PASSWORD}'; sleep $LOGIN_STEP_S; ")
            // Disable QNX command echo and fold stderr into the body before the markers
            append("echo 'stty -echo 2>/dev/null'; ")
            append("echo 'exec 2>&1'; ")
            append("echo 'Q=$id'; ")
            append("echo 'echo \${Q}S'; ")
            for (line in lines) append("echo '$line'; ")
            append("echo 'echo \${Q}E\$?'; ")
            append("sleep $DRAIN_S; echo exit; ")
            if (elevate) append("echo exit; ")
            append("}")
        }
        val res = sh("$block | $launcher")
        val reached = res.output.contains("${id}S")
        log("qnx via '${if (elevate) "su" else "telnet"}' reached=$reached")
        if (!reached) log("qnx tail=${res.output.takeLast(200)}")
        return reached to parseMarked(res.output, "${id}S", "${id}E")
    }

    private fun parseMarked(raw: String, startMarker: String, endMarker: String): ShResult {
        val s = raw.indexOf(startMarker)
        val e = raw.lastIndexOf(endMarker)
        if (s < 0 || e < 0 || e < s) return ShResult(raw, -1, false)
        val body = raw.substring(s + startMarker.length, e).trim()
        val exit = raw.substring(e + endMarker.length).trimStart().takeWhile { it.isDigit() }.toIntOrNull() ?: -1
        return ShResult(body, exit, exit == 0)
    }

    private fun md5Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("MD5").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun firstHex(text: String): String? {
        val match = Regex("\\b[a-fA-F0-9]{32}\\b").find(text)
        return match?.value
    }

    // First line that is purely a number - skips echoed command lines that merely contain digits
    private fun firstLong(text: String): Long? =
        text.lineSequence().mapNotNull { it.trim().toLongOrNull() }.firstOrNull()

    private fun intAfter(text: String, marker: String): Int {
        val idx = text.lastIndexOf(marker)
        if (idx < 0) return -1
        return text.substring(idx + marker.length).trimStart().takeWhile { it.isDigit() }.toIntOrNull() ?: -1
    }

    private object KzbMagic {
        fun hasMagic(bytes: ByteArray): Boolean =
            bytes.size >= 4 && bytes[0] == 'K'.code.toByte() && bytes[1] == 'Z'.code.toByte() &&
                    bytes[2] == 'B'.code.toByte() && bytes[3] == 'F'.code.toByte()
    }

    private companion object {
        const val MARK = "__GIBQ_"
        const val QMARK = "__QNXQ_"
        const val WRITE_B64_CHUNK = 16384
        const val READ_CHUNK_BYTES = 262144

        // A background.kzb is a few MB - cap parsed sizes so a mis-read can never drive a runaway read
        const val MAX_BG_BYTES = 64L * 1024 * 1024

        // Telnet login timing copied from the community scripts that are known to work on this cluster
        const val TELNET_CONNECT_S = 3
        const val LOGIN_STEP_S = 2
        const val DRAIN_S = 2
    }
}
