package com.salat.gbinder

import com.salat.gbinder.entity.KeyBindPattern

object DebugKeyBindHarness {
    const val STUB_KEY_CODE = 1456

    val shortClickTestPattern: KeyBindPattern
        get() = KeyBindPattern.ShortClick(STUB_KEY_CODE)
}
