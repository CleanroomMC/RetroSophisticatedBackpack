package com.cleanroommc.retrosophisticatedbackpack.inventory

import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.retrosophisticatedbackpack.Tags

object TabTextures {
    private fun builder(): UITexture.Builder =
        UITexture.builder().location(Tags.MOD_ID, "gui/gui_controls.png").imageSize(256, 256)
}