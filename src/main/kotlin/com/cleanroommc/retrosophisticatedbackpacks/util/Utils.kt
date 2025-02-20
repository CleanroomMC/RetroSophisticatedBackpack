package com.cleanroommc.retrosophisticatedbackpacks.util

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import kotlin.math.absoluteValue
import kotlin.math.sign

object Utils {
    fun String.asTranslationKey(): String =
        "${Tags.MOD_ID}.${this}"

    fun Int.ceilDiv(other: Int): Int =
        this.floorDiv(other) + this.rem(other).sign.absoluteValue
}