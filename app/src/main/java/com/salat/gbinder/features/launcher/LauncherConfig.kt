package com.salat.gbinder.features.launcher

import android.util.DisplayMetrics

const val DEFAULT_LAUNCHER_ICON_DENSITY = DisplayMetrics.DENSITY_XHIGH
const val DEFAULT_LAUNCHER_ICON_OUT_SPACE = 48
const val DEFAULT_LAUNCHER_ICON_INNER_SPACE = 48
const val DEFAULT_LAUNCHER_ICON_SIZE = 86
const val DEFAULT_LAUNCHER_ICON_TEXT_SIZE = 15
const val DEFAULT_LAUNCHER_ICON_TEXT_PADDING = 10
const val DEFAULT_LAUNCHER_ICON_ROUND = 18 // 8 = like system
const val DEFAULT_LAUNCHER_ICON_IS_ROUND = true
const val DEFAULT_LAUNCHER_WINDOW_HORIZONTAL_SPACE = 64
const val DEFAULT_LAUNCHER_WINDOW_VERTICAL_SPACE = 72
const val DEFAULT_LAUNCHER_WINDOW_ALPHA = .97f
const val DEFAULT_LAUNCHER_SHORTCUT_SIZE = 32
const val DEFAULT_LAUNCHER_DIVIDER_SIZE = 22
const val DEFAULT_AUTO_LIGHT_THEME_START = 7
const val DEFAULT_AUTO_LIGHT_THEME_END = 20

const val LAUNCHER_TOOLBAR_HEIGHT = 60
const val DISABLED_APP_TRANSPARENCY = .6f

val OVERLAY_RESTRICTED_PKGS by lazy {
    setOf(
        "com.android.settings",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        // "com.android.systemui",
    )
}

val NAVI_PKGS by lazy {
    setOf(
        "ru.yandex.yandexnavi",
        "ru.yandex.yandexmaps",
        "ru.dublgis.dgismobile",
        "com.navitel",
        "com.mapswithme.maps.pro",
        "com.cityguide.probki.net",
        "ru.probki.net",
        "com.garmin.android.apps.gmobilext",
        "com.shturmann.navigator",
        "com.locator.navitel",
        "com.google.android.apps.maps",
        "com.waze",
        "com.here.app.maps",
        "com.sygic.aura",
        "com.tomtom.gplay.navapp",
        "net.osmand",
        "com.mapquest.android.ace",
        "com.telenav.app.android",
        "com.glympse.android.glympse",
        "com.tripit",
        "com.sygic.truck",
        "com.here.wego.enterprise",
        "com.mapfactor.navigator",
        "com.karta.gps.navigation",
        "com.polestar.navigation",
        "com.magiclane.navigation"
    )
}
