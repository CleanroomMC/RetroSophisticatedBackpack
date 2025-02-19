package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import net.minecraft.util.ResourceLocation

object RSBTextures {
    private val ICON_LOCATION = ResourceLocation(Tags.MOD_ID, "gui/icons")

    val CHECK_ICON = icon("check", 0, 0)
    val CROSS_ICON = icon("cross", 16, 0)

    val TOGGLE_DISABLE_ICON = icon("disable", 0, 128, 4, 10)
    val TOGGLE_ENABLE_ICON = icon("enable", 4, 128, 4, 10)

    private fun icon(name: String, x: Int, y: Int, w: Int = 16, h: Int = 16): UITexture =
        UITexture.builder()
            .location(ICON_LOCATION)
            .imageSize(256, 256)
            .uv(x, y, w, h)
            .name(name)
            .build()
}