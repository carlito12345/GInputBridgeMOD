package com.salat.gbinder.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

fun isTrustedByCertificate(pkg: String, certs: Set<String>, context: Context): Boolean {
    val pm = context.packageManager

    val packageInfo = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageInfo(
                pkg,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(
                pkg,
                PackageManager.GET_SIGNATURES
            )
        }
    } catch (e: PackageManager.NameNotFoundException) {
        return false
    }

    val appInfo = packageInfo.applicationInfo ?: return false
    if (!appInfo.enabled) {
        return false
    }

    val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val signingInfo = packageInfo.signingInfo ?: return false
        if (signingInfo.hasMultipleSigners()) {
            signingInfo.apkContentsSigners
        } else {
            signingInfo.signingCertificateHistory
        }
    } else {
        @Suppress("DEPRECATION")
        packageInfo.signatures
    }

    return signatures?.any { signature ->
        sha256(signature.toByteArray()) in certs
    } ?: false
}

private fun sha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString(":") { "%02X".format(it) }
}
