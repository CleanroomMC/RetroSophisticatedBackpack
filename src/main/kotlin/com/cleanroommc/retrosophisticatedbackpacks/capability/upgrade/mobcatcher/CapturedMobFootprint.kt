package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher

data class CapturedMobFootprint(val width: Int, val height: Int) {
    val area: Int
        get() = width * height
}
