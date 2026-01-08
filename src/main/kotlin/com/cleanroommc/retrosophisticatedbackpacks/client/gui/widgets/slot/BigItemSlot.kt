package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures.BIG_SLOT_TEXTURE
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

class BigItemSlot() : ItemSlot() {

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