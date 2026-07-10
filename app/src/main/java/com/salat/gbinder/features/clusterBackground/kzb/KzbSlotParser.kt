package com.salat.gbinder.features.clusterBackground.kzb

// Parses embedded PNG slots from a Kanzi KZBF archive without touching its directory
// Every image is stored as [uint32-LE length][PNG stream]; sizes differ per firmware, so
// nothing is hardcoded - the map is always rebuilt from the actual user file
object KzbSlotParser {

    private val KZB_MAGIC = byteArrayOf('K'.code.toByte(), 'Z'.code.toByte(), 'B'.code.toByte(), 'F'.code.toByte())
    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )
    private val IEND_TYPE = byteArrayOf('I'.code.toByte(), 'E'.code.toByte(), 'N'.code.toByte(), 'D'.code.toByte())

    fun hasKzbMagic(data: ByteArray): Boolean =
        data.size >= 4 && regionEquals(data, 0, KZB_MAGIC)

    // Returns validated slots in file order; a slot is accepted only when the uint32-LE
    // length prefix matches a real PNG stream that ends with a proper IEND chunk
    fun parse(data: ByteArray): List<KzbSlot> {
        val slots = ArrayList<KzbSlot>()
        var index = 0
        var search = 0
        while (true) {
            val pngStart = indexOf(data, PNG_SIGNATURE, search)
            if (pngStart < 0) break
            search = pngStart + PNG_SIGNATURE.size

            val lengthFieldOffset = pngStart - 4
            if (lengthFieldOffset < 0) continue

            val declaredLength = readUInt32Le(data, lengthFieldOffset)
            if (declaredLength <= 0 || pngStart.toLong() + declaredLength > data.size) continue

            // The declared slot must be a self-contained PNG ending in IEND
            val iendEnd = pngStart + declaredLength
            if (!endsWithIend(data, pngStart, iendEnd)) continue

            val ihdr = readIhdr(data, pngStart) ?: continue
            slots.add(
                KzbSlot(
                    index = index,
                    lengthFieldOffset = lengthFieldOffset,
                    dataOffset = pngStart,
                    length = declaredLength,
                    width = ihdr.first,
                    height = ihdr.second,
                    colorType = ihdr.third
                )
            )
            index++
            search = iendEnd
        }
        return slots
    }

    // IHDR is always the first chunk; width/height/colorType live at fixed offsets after the signature
    private fun readIhdr(data: ByteArray, pngStart: Int): Triple<Int, Int, Int>? {
        val ihdrTypeOffset = pngStart + 12
        if (ihdrTypeOffset + 11 > data.size) return null
        val isIhdr = data[ihdrTypeOffset] == 'I'.code.toByte() &&
                data[ihdrTypeOffset + 1] == 'H'.code.toByte() &&
                data[ihdrTypeOffset + 2] == 'D'.code.toByte() &&
                data[ihdrTypeOffset + 3] == 'R'.code.toByte()
        if (!isIhdr) return null
        val width = readUInt32Be(data, pngStart + 16)
        val height = readUInt32Be(data, pngStart + 20)
        val colorType = data[pngStart + 25].toInt() and 0xFF
        return Triple(width, height, colorType)
    }

    private fun endsWithIend(data: ByteArray, pngStart: Int, iendEnd: Int): Boolean {
        // IEND chunk occupies the final 12 bytes: length(4) + "IEND"(4) + crc(4)
        val typeOffset = iendEnd - 8
        return typeOffset >= pngStart && regionEquals(data, typeOffset, IEND_TYPE)
    }

    private fun readUInt32Le(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                ((data[offset + 2].toInt() and 0xFF) shl 16) or
                ((data[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun readUInt32Be(data: ByteArray, offset: Int): Int {
        return ((data[offset].toInt() and 0xFF) shl 24) or
                ((data[offset + 1].toInt() and 0xFF) shl 16) or
                ((data[offset + 2].toInt() and 0xFF) shl 8) or
                (data[offset + 3].toInt() and 0xFF)
    }

    private fun regionEquals(data: ByteArray, offset: Int, needle: ByteArray): Boolean {
        if (offset < 0 || offset + needle.size > data.size) return false
        for (i in needle.indices) {
            if (data[offset + i] != needle[i]) return false
        }
        return true
    }

    private fun indexOf(data: ByteArray, needle: ByteArray, from: Int): Int {
        if (needle.isEmpty()) return -1
        var i = maxOf(from, 0)
        val last = data.size - needle.size
        while (i <= last) {
            var matched = true
            for (j in needle.indices) {
                if (data[i + j] != needle[j]) {
                    matched = false
                    break
                }
            }
            if (matched) return i
            i++
        }
        return -1
    }
}
