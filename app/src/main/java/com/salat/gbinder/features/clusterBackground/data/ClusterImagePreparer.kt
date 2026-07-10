package com.salat.gbinder.features.clusterBackground.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import com.salat.gbinder.features.clusterBackground.kzb.KzbSlot
import java.io.ByteArrayOutputStream

// Turns a user image into a PNG that exactly matches a slot's dimensions and color type
// Color type is preserved so the Kanzi texture keeps the format it expects - opaque RGB for type 2,
// RGBA for type 6 - which maximizes the chance the patched archive still loads
object ClusterImagePreparer {

    fun encodeForSlot(source: ByteArray, slot: KzbSlot): ByteArray {
        val decoded = BitmapFactory.decodeByteArray(source, 0, source.size)
            ?: throw IllegalArgumentException("cannot decode source image")
        val budget = slot.length - PNG_CHUNK_OVERHEAD
        try {
            val fitted = centerCropScale(decoded, slot.width, slot.height)
            try {
                var factor = 1f
                var best: ByteArray? = null
                // PNG is lossless, so to fit the byte slot we lower detail by downscaling then
                // upscaling back to the slot size - keeps output dimensions, shrinks the encoded
                // size, and never allocates anything larger than the slot so it cannot OOM
                repeat(MAX_REDUCTION_STEPS) {
                    val candidate = if (factor <= 1f) fitted else reduceDetail(fitted, factor)
                    val png = encodeWithColorType(candidate, slot.hasAlpha)
                    if (candidate !== fitted) candidate.recycle()
                    best = png
                    if (png.size <= budget) return png
                    factor *= REDUCTION_STEP
                }
                return best ?: throw IllegalStateException("encode produced no output")
            } finally {
                fitted.recycle()
            }
        } finally {
            if (!decoded.isRecycled) decoded.recycle()
        }
    }

    private fun encodeWithColorType(bitmap: Bitmap, wantAlpha: Boolean): ByteArray {
        val prepared = applyColorType(bitmap, wantAlpha)
        val out = ByteArrayOutputStream()
        prepared.compress(Bitmap.CompressFormat.PNG, 100, out)
        if (prepared !== bitmap) prepared.recycle()
        return out.toByteArray()
    }

    // Blur by round-tripping through a smaller bitmap - the higher the factor, the less detail and
    // the smaller the resulting PNG
    private fun reduceDetail(src: Bitmap, factor: Float): Bitmap {
        val dw = (src.width / factor).toInt().coerceAtLeast(8)
        val dh = (src.height / factor).toInt().coerceAtLeast(8)
        val small = Bitmap.createScaledBitmap(src, dw, dh, true)
        val back = Bitmap.createScaledBitmap(small, src.width, src.height, true)
        if (small !== back) small.recycle()
        return back
    }

    // Cover-fit: scale to fill the target and crop the overflow, keeping the center
    private fun centerCropScale(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val result = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val scale = maxOf(targetW.toFloat() / src.width, targetH.toFloat() / src.height)
        val scaledW = src.width * scale
        val scaledH = src.height * scale
        val cropW = (targetW / scale).toInt().coerceIn(1, src.width)
        val cropH = (targetH / scale).toInt().coerceIn(1, src.height)
        val cropLeft = ((src.width - cropW) / 2).coerceAtLeast(0)
        val cropTop = ((src.height - cropH) / 2).coerceAtLeast(0)
        val srcRect = Rect(cropLeft, cropTop, cropLeft + cropW, cropTop + cropH)
        val dstRect = Rect(0, 0, targetW, targetH)
        canvas.drawBitmap(src, srcRect, dstRect, null)
        return result
    }

    private fun applyColorType(bitmap: Bitmap, wantAlpha: Boolean): Bitmap {
        if (wantAlpha) {
            // Force an alpha channel so the encoder emits color type 6
            if (!bitmap.hasAlpha()) bitmap.setHasAlpha(true)
            return bitmap
        }
        // Flatten onto black and drop alpha so the encoder emits opaque color type 2
        val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(opaque)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        opaque.setHasAlpha(false)
        return opaque
    }

    // Minimum PNG chunk footprint reserved for the size-padding chunk in PngSlotPatcher
    private const val PNG_CHUNK_OVERHEAD = 12
    private const val MAX_REDUCTION_STEPS = 9
    private const val REDUCTION_STEP = 1.6f
}
