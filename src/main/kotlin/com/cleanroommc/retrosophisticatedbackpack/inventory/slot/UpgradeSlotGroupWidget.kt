package com.cleanroommc.retrosophisticatedbackpack.inventory.slot

import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpack.Tags
import net.minecraft.util.ResourceLocation

class UpgradeSlotGroupWidget(private val slotSize: Int) : SlotGroupWidget() {
    companion object {
        private val UPPER_TAB_TEXTURE =
            UITexture.builder().location(ResourceLocation(Tags.MOD_ID, "gui/gui_controls.png")).imageSize(256, 256)
                .uv(0, 0, 25, 5).build()
        private val SLOT_SURROUNDING_TEXTURE =
            UITexture.builder().location(ResourceLocation(Tags.MOD_ID, "gui/gui_controls.png")).imageSize(256, 256)
                .uv(0, 5, 25, 18).build()
        private val LOWER_TAB_TEXTURE =
            UITexture.builder().location(ResourceLocation(Tags.MOD_ID, "gui/gui_controls.png")).imageSize(256, 256)
                .uv(0, 199, 25, 5).build()
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)
        var y = 5

        UPPER_TAB_TEXTURE.draw(context, 0, 0, 25, 5)

        for (i in 0 until slotSize) {
            SLOT_SURROUNDING_TEXTURE.draw(context, 0, y, 25, 18)
            y += 18
        }

        LOWER_TAB_TEXTURE.draw(context, 0, y, 25, 5)
    }
}