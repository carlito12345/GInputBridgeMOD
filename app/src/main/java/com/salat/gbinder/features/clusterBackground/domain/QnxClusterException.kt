package com.salat.gbinder.features.clusterBackground.domain

// Typed failure so the presentation layer can map each cause to localized copy
enum class QnxErrorCode {
    SHELL_UNAVAILABLE,
    SHARED_NOT_WRITABLE,
    QNX_UNREACHABLE,
    TARGET_MISSING,
    TRANSFER_FAILED,
    VERIFY_MISMATCH,
    REMOUNT_FAILED,
    NO_CHECKSUM_TOOL,
    IMAGE_TOO_LARGE,
    INVALID_FILE,
    UNKNOWN
}

class QnxClusterException(
    val code: QnxErrorCode,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
