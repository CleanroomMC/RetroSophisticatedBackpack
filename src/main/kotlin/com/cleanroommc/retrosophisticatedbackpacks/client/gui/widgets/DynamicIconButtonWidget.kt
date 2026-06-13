package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.client.renderer.GlStateManager

class DynamicIconButtonWidget(
    private val icon: () -> IDrawable,
    private val iconOffset: Int = 1,
    private val iconSize: Int = 16
) : ButtonWidget<DynamicIconButtonWidget>() {
    override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        val theme = widgetTheme.getThemeOrDefault()
        if (isHovering) RSBTextures.STANDARD_BUTTON_HOVERED.draw(context, 0, 0, 18, 18, theme)
        else RSBTextures.STANDARD_BUTTON.draw(context, 0, 0, 18, 18, theme)
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        icon().draw(context, iconOffset, iconOffset, iconSize, iconSize, widgetTheme.getThemeOrDefault())
        GlStateManager.color(1f, 1f, 1f, 1f)
    }
}
