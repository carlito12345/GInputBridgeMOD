package com.salat.gbinder.receiver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 下载 HTTP 封面到本地文件(车机仪表盘需要本地 URI)
 * 使用 HttpURLConnection 直接下载, 不依赖 Coil
 */
object CoverDownloader {
    private const val TAG = "CoverDownloader"
    private const val TEMP_FOLDER = "gib_dash_cover"
    private const val FILE_TYPE = ".jpg"

    /**
     * 下载封面并返回本地 file:// URI; 失败返回 null
     */
    fun download(coverUrl: String, context: Context): String? {
        return try {
            val url = URL(coverUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }
            val input = conn.inputStream ?: return null
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            conn.disconnect()

            if (bitmap == null) {
                Log.w(TAG, "decode failed: $coverUrl")
                return null
            }

            val outputDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                TEMP_FOLDER
            ).apply { mkdirs() }
            val outputFile = File(outputDir, "${coverUrl.hashCode()}$FILE_TYPE")

            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            if (!bitmap.isRecycled) bitmap.recycle()

            Log.i(TAG, "downloaded: $coverUrl -> ${outputFile.absolutePath}")
            Uri.fromFile(outputFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "download failed: $coverUrl err=${e.message}")
            null
        }
    }

    /**
     * 保存播放器提供的封面 Bitmap 到本地文件
     */
    fun saveBitmap(bitmap: Bitmap, context: Context): String? {
        return try {
            val outputDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                TEMP_FOLDER
            ).apply { mkdirs() }
            val outputFile = File(outputDir, "bitmap_${System.currentTimeMillis()}$FILE_TYPE")

            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            Log.i(TAG, "bitmap saved: ${outputFile.absolutePath}")
            Uri.fromFile(outputFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "bitmap save failed: ${e.message}")
            null
        }
    }
}
