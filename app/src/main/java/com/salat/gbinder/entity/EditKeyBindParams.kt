package com.salat.gbinder.entity

import androidx.compose.runtime.Immutable

@Immutable
data class EditKeyBindParams(
    val bindName: String,
    val config: KeyBindConfig,
    val pattern: KeyBindPattern,
    val initialSection: EditKeyBindSection? = null
)

enum class EditKeyBindSection {
    KEYS,
    PARAMS
}
