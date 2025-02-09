package com.cleanroommc.retrosophisticatedbackpack.inventory.widget

import com.cleanroommc.modularui.drawable.AdaptableUITexture
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.retrosophisticatedbackpack.Tags

sealed class ExpandedTabWidget : ParentWidget<ExpandedTabWidget>() {
    companion object {
        val TAB_TEXTURE = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(128, 0, 128, 256)
            .adaptable(4)
            .tiled()
            .build() as AdaptableUITexture
    }
}