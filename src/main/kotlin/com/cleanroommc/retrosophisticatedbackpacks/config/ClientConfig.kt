package com.cleanroommc.retrosophisticatedbackpacks.config

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import net.minecraftforge.common.config.Config

@Config(modid = Tags.MOD_ID, name = "${Tags.MOD_ID}_client")
object ClientConfig {
    @JvmField
    var enableAnimation = true
}