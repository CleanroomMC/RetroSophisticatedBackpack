package com.cleanroommc.retrosophisticatedbackpacks.util

import com.cleanroommc.retrosophisticatedbackpacks.mixin.EnumDyeColorAccessor
import net.minecraft.item.EnumDyeColor

object DyeColorUtils {
    private val fallbackColors = intArrayOf(
        16383998, 16351261, 13061821, 3847130,
        16701501, 8439583, 15961002, 4673362,
        10329495, 1481884, 8991416, 3949738,
        8606770, 6192150, 11546150, 1908001
    )

    fun colorValue(color: EnumDyeColor): Int =
        ((color as Any) as? EnumDyeColorAccessor)?.`rsb$getColorValue`()
            ?: fallbackColors.getOrElse(color.ordinal) { 0xFF0000 }
}
