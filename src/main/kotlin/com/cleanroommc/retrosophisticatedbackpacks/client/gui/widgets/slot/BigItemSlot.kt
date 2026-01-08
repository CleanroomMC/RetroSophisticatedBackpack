package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

class BigItemSlot(): ItemSlot() {
    companion object {
        private val BIG_SLOT_TEXTURE = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls.png")
            .imageSize(256, 256)
            .xy(71, 216, 26, 26)
            .build()
    }

    override fun draw(
        context: ModularGuiContext?,
        widgetTheme: WidgetThemeEntry<*>?
    ) {
        context?.let {
            BIG_SLOT_TEXTURE.draw(context, -4, -4, 26, 26, widgetTheme.getThemeOrDefault())
        }
        super.draw(context, widgetTheme)
    }
}