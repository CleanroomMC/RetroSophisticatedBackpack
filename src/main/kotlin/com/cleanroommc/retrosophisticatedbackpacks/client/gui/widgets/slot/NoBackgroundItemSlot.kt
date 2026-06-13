package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

open class NoBackgroundItemSlot(private val emptyOverlay: IDrawable? = null) : ItemSlot() {
    init {
        background(IDrawable.EMPTY)
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)
        if (emptyOverlay != null && isSynced && slot.stack.isEmpty) {
            emptyOverlay.draw(context, 1, 1, 16, 16, widgetTheme.getThemeOrDefault())
        }
    }
}
