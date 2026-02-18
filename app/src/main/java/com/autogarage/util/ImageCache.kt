package com.autogarage.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCache @Inject constructor(
    private val context: Context
) {
    // ✅ OPTIMIZATION: Cache decoded bitmaps
    private val bitmapCache = object : LruCache<String, Bitmap>(
        // Use 1/8th of available memory for bitmap cache
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    suspend fun loadImage(path: String, maxWidth: Int = 1024): Bitmap? {
        // Check cache first
        bitmapCache.get(path)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                // ✅ OPTIMIZATION: Decode with inSampleSize for memory efficiency
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(path, options)

                // Calculate sample size
                options.inSampleSize = calculateInSampleSize(options, maxWidth)
                options.inJustDecodeBounds = false

                val bitmap = BitmapFactory.decodeFile(path, options)

                // Cache the bitmap
                bitmap?.let { bitmapCache.put(path, it) }

                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int
    ): Int {
        val width = options.outWidth
        var inSampleSize = 1

        if (width > reqWidth) {
            val halfWidth = width / 2
            while (halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun clearCache() {
        bitmapCache.evictAll()
    }
}
