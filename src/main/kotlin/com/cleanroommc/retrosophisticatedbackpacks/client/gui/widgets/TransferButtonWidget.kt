package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.client.renderer.GlStateManager

class TransferButtonWidget(private val matchedIcon: IDrawable, private val allIcon: IDrawable) :
    ButtonWidget<TransferButtonWidget>() {
    override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        val theme = widgetTheme.getThemeOrDefault()
        if (isHovering) RSBTextures.SMALL_BUTTON_HOVERED.draw(context, 0, 0, 12, 12, theme)
        else RSBTextures.SMALL_BUTTON.draw(context, 0, 0, 12, 12, theme)
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        val icon = if (Interactable.hasShiftDown()) allIcon else matchedIcon
        icon.draw(context, 0, 0, 12, 12, widgetTheme.getThemeOrDefault())
        GlStateManager.color(1f, 1f, 1f, 1f)
    }
}
