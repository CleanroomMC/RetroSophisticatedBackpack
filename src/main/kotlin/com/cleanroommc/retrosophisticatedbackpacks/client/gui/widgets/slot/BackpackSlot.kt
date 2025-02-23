package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.core.mixin.GuiAccessor
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper

class BackpackSlot() : ItemSlot() {
    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)

        val slot = slot as? ModularBackpackSlot ?: return
        val stack = slot.stack

        if (!stack.isEmpty)
            return

        val memoryStack = slot.getMemoryStack()

        if (memoryStack.isEmpty)
            return

        val guiScreen = screen.screenWrapper.guiScreen
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender

        (guiScreen as GuiAccessor).zLevel = 100f
        renderItem.zLevel = 100f
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableLighting()

        renderItem.renderItemIntoGUI(memoryStack, 1, 1)
        GlStateManager.depthFunc(516)
        Gui.drawRect(1, 1, 17, 17, 822083583)
        GlStateManager.depthFunc(515)

        GlStateManager.enableLighting()
        RenderHelper.disableStandardItemLighting()

        (guiScreen as GuiAccessor).zLevel = 0f
        renderItem.zLevel = 0f
    }
}