package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

class TransferButtonWidget(private val matchedIcon: IDrawable, private val allIcon: IDrawable) :
    ButtonWidget<TransferButtonWidget>() {
    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)

        if (Interactable.hasShiftDown()) {
            allIcon.drawAtZero(context, area, widgetTheme.getThemeOrDefault())
        } else {
            matchedIcon.drawAtZero(context, area, widgetTheme.getThemeOrDefault())
        }
    }
}