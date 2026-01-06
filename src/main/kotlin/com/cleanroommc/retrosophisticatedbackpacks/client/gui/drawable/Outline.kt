package com.cleanroommc.retrosophisticatedbackpacks.client.gui.drawable

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.modularui.screen.viewport.GuiContext
import com.cleanroommc.modularui.theme.WidgetTheme

class Outline(val color: Int) : IDrawable {
    override fun draw(
        context: GuiContext,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        widgetTheme: WidgetTheme
    ) {
        GuiDraw.drawBorderInsideXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color)
    }
}