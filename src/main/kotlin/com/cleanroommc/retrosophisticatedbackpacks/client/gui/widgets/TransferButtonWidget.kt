package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widgets.ButtonWidget

class TransferButtonWidget(private val matchedIcon: IDrawable, private val allIcon: IDrawable) :
    ButtonWidget<TransferButtonWidget>() {
    override fun drawOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawOverlay(context, widgetTheme)

        if (Interactable.hasShiftDown()) {
            allIcon.drawAtZero(context, area, widgetTheme)
        } else {
            matchedIcon.drawAtZero(context, area, widgetTheme)
        }
    }
}