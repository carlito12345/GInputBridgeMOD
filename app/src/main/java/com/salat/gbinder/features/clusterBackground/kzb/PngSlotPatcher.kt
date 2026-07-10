package com.salat.gbinder.features.clusterBackground.kzb

import java.util.zip.CRC32

// Splices a new PNG into a single KZB slot without shifting any other byte
// The replacement is padded to the exact original slot length via an ancillary chunk, so the
// length prefix, all downstream offsets and the KZB directory stay byte-identical - the safest
// possible edit given the format keeps no absolute offsets
object PngSlotPatcher {

    // Minimum PNG chunk footprint: length(4) + type(4) + crc(4)
    private const val MIN_CHUNK_BYTES = 12

    // Private ancillary chunk - lowercase first letter marks it safe for decoders to ignore
    private val PAD_CHUNK_TYPE = byteArrayOf('g'.code.toByte(), 'b'.code.toByte(), 'P'.code.toByte(), 'd'.code.toByte())
    private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private val IEND_TYPE = byteArrayOf('I'.code.toByte(), 'E'.code.toByte(), 'N'.code.toByte(), 'D'.code.toByte())

    sealed interface Result {
        data class Success(val patchedFile: ByteArray) : Result
        // Encoded image does not fit the slot budget - user must simplify or pick a bigger slot
        data class TooLarge(val encodedSize: Int, val budget: Int) : Result
        data class Invalid(val reason: String) : Result
    }

    fun patch(originalFile: ByteArray, slot: KzbSlot, encodedPng: ByteArray): Result {
        if (slot.dataOffset + slot.length > originalFile.size) {
            return Result.Invalid("slot out of file bounds")
        }
        if (encodedPng.size < 8 || !startsWith(encodedPng, PNG_SIGNATURE)) {
            return Result.Invalid("prepared image is not a PNG")
        }

        val budget = slot.length - MIN_CHUNK_BYTES
        val gap = slot.length - encodedPng.size
        if (gap != 0 && encodedPng.size > budget) {
            return Result.TooLarge(encodedPng.size, budget)
        }

        val padded = if (gap == 0) encodedPng else padToExactSize(encodedPng, gap)
            ?: return Result.Invalid("could not build padded PNG")

        if (padded.size != slot.length) {
            return Result.Invalid("padded size mismatch: ${padded.size} != ${slot.length}")
        }
        if (!endsWithIend(padded)) {
            return Result.Invalid("padded PNG missing IEND")
        }

        // Only the slot bytes change; everything else is a verbatim copy of the original file
        val patchedFile = originalFile.copyOf()
        System.arraycopy(padded, 0, patchedFile, slot.dataOffset, slot.length)

        if (patchedFile.size != originalFile.size) {
            return Result.Invalid("file size changed")
        }
        return Result.Success(patchedFile)
    }

    // Inserts one padding chunk right before IEND so total size grows by exactly gap bytes
    private fun padToExactSize(png: ByteArray, gap: Int): ByteArray? {
        if (gap < MIN_CHUNK_BYTES) return null
        val iendTypeOffset = lastIndexOf(png, IEND_TYPE)
        if (iendTypeOffset < 4) return null
        val iendChunkStart = iendTypeOffset - 4

        val dataLen = gap - MIN_CHUNK_BYTES
        val padChunk = buildPadChunk(dataLen)

        val out = ByteArray(png.size + gap)
        System.arraycopy(png, 0, out, 0, iendChunkStart)
        System.arraycopy(padChunk, 0, out, iendChunkStart, padChunk.size)
        System.arraycopy(png, iendChunkStart, out, iendChunkStart + padChunk.size, png.size - iendChunkStart)
        return out
    }

    private fun buildPadChunk(dataLen: Int): ByteArray {
        val chunk = ByteArray(MIN_CHUNK_BYTES + dataLen)
        writeUInt32Be(chunk, 0, dataLen)
        System.arraycopy(PAD_CHUNK_TYPE, 0, chunk, 4, 4)
        // data stays zero-filled
        val crc = CRC32()
        crc.update(chunk, 4, 4 + dataLen)
        writeUInt32Be(chunk, 8 + dataLen, crc.value.toInt())
        return chunk
    }

    private fun endsWithIend(png: ByteArray): Boolean {
        if (png.size < 12) return false
        val typeOffset = png.size - 8
        return regionEquals(png, typeOffset, IEND_TYPE)
    }

    private fun startsWith(data: ByteArray, needle: ByteArray): Boolean = regionEquals(data, 0, needle)

    private fun regionEquals(data: ByteArray, offset: Int, needle: ByteArray): Boolean {
        if (offset < 0 || offset + needle.size > data.size) return false
        for (i in needle.indices) {
            if (data[offset + i] != needle[i]) return false
        }
        return true
    }

    private fun lastIndexOf(data: ByteArray, needle: ByteArray): Int {
        var i = data.size - needle.size
        while (i >= 0) {
            var matched = true
            for (j in needle.indices) {
                if (data[i + j] != needle[j]) {
                    matched = false
                    break
                }
            }
            if (matched) return i
            i--
        }
        return -1
    }

    private fun writeUInt32Be(data: ByteArray, offset: Int, value: Int) {
        data[offset] = ((value ushr 24) and 0xFF).toByte()
        data[offset + 1] = ((value ushr 16) and 0xFF).toByte()
        data[offset + 2] = ((value ushr 8) and 0xFF).toByte()
        data[offset + 3] = (value and 0xFF).toByte()
    }
}
