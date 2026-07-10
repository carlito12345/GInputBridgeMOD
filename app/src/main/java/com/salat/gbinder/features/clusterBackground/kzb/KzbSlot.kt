package com.salat.gbinder.features.clusterBackground.kzb

// One embedded PNG inside a Kanzi background.kzb archive
// Layout in file: [uint32-LE length][raw PNG bytes]
data class KzbSlot(
    val index: Int,
    val lengthFieldOffset: Int,
    val dataOffset: Int,
    val length: Int,
    val width: Int,
    val height: Int,
    val colorType: Int
) {
    // PNG color type 6 carries an alpha channel, type 2 is opaque RGB
    val hasAlpha: Boolean get() = colorType == 6
}
