package com.salat.gbinder.features.geelyLauncher

import android.content.Context
import androidx.core.net.toUri
import timber.log.Timber

private const val GEELY_APPSTORE_APP_INFO_URI =
    "content://com.geely.appstore.AppstoreProvider/appInfo"

fun isGeelyAppStoreProviderRegistered(context: Context): Boolean {
    val uri = GEELY_APPSTORE_APP_INFO_URI.toUri()
    return try {
        context.contentResolver.acquireContentProviderClient(uri)?.use {
            true
        } ?: false
    } catch (e: Exception) {
        Timber.e(e)
        false
    }
}
