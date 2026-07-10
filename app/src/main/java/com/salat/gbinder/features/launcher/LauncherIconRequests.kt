package com.salat.gbinder.features.launcher

import android.content.Context
import android.net.Uri
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.salat.gbinder.entity.DisplayIconRef
import kotlinx.coroutines.Dispatchers

fun launcherIconRequest(
    context: Context,
    iconRef: DisplayIconRef?,
    customIcon: Uri?,
    pxSize: Int
): ImageRequest {
    val builder = ImageRequest.Builder(context)
        .size(pxSize, pxSize)
        .precision(Precision.EXACT)
        .allowHardware(false)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .dispatcher(Dispatchers.IO)

    if (customIcon != null) {
        // Stable keys let the prewarmed bitmap resolve synchronously on first composition
        val stableKey = "custom:$customIcon|w:$pxSize|h:$pxSize"
        builder.data(customIcon)
            .memoryCacheKey(stableKey)
            .placeholderMemoryCacheKey(stableKey)
    } else {
        val stableKey =
            "pkg:${iconRef?.packageName}|res:${iconRef?.resId}|dpi:${iconRef?.densityDpi}|vc:${iconRef?.versionCode}|w:$pxSize|h:$pxSize"
        builder.data(iconRef)
            .memoryCacheKey(stableKey)
            .placeholderMemoryCacheKey(stableKey)
    }

    return builder.build()
}
