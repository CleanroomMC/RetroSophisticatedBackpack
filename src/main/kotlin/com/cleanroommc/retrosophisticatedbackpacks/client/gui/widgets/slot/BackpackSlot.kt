package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.core.mixin.GuiAccessor
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class BackpackSlot(private val panel: BackpackPanel, private val wrapper: BackpackWrapper) : ItemSlot() {
    private val isInSettingMode: Boolean
        get() = panel.settingPanel.isPanelOpen
    private val stackMemorySetting: NonNullList<ItemStack>
        get() = wrapper.backpackItemStackHandler.memoryStack

    override fun onMousePressed(mouseButton: Int): Interactable.Result =
        if (isInSettingMode) {
            val isMemorySet = !stackMemorySetting[slot.slotIndex].isEmpty

            if (isMemorySet) {
                wrapper.unsetMemoryStack(slot.slotIndex)
                syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_MEMORY_STACK)
            } else {
                wrapper.setMemoryStack(slot.slotIndex)
                syncHandler.syncToServer(BackpackSlotSH.UPDATE_SET_MEMORY_STACK)
            }

            Interactable.Result.SUCCESS
        } else {
            super.onMousePressed(mouseButton)
        }

    override fun onMouseRelease(mouseButton: Int): Boolean =
        if (isInSettingMode) true
        else super.onMouseRelease(mouseButton)

    override fun onMouseDrag(mouseButton: Int, timeSinceClick: Long) {
        if (isInSettingMode)
            return

        super.onMouseDrag(mouseButton, timeSinceClick)
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        if (isInSettingMode) {
            drawSettingStack(context, widgetTheme)
        } else {
            drawNormalStack(context, widgetTheme)
        }
    }

    private fun drawSettingStack(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        val memoryStack = wrapper.backpackItemStackHandler.memoryStack[slot.slotIndex]
        val guiScreen = screen.screenWrapper.guiScreen
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender

        (guiScreen as GuiAccessor).zLevel = 100f
        renderItem.zLevel = 100f
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableLighting()

        if (slot.stack.isEmpty)
            renderItem.renderItemIntoGUI(memoryStack, 1, 1)
        else
            renderItem.renderItemIntoGUI(slot.stack, 1, 1)

        if (!memoryStack.isEmpty) {
            GlStateManager.depthFunc(516)
            Gui.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128))
            GlStateManager.depthFunc(515)
        }

        GlStateManager.enableLighting()
        RenderHelper.disableStandardItemLighting()

        (guiScreen as GuiAccessor).zLevel = 0f
        renderItem.zLevel = 0f
    }

    private fun drawNormalStack(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)

        val slot = slot as? ModularBackpackSlot ?: return
        val stack = slot.stack

        if (!stack.isEmpty)
            return

        val memoryStack = slot.getMemoryStack()

        if (memoryStack.isEmpty)
            return

        drawMemoryStack(memoryStack)
    }

    private fun drawMemoryStack(memoryStack: ItemStack) {
        val guiScreen = screen.screenWrapper.guiScreen
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender

        (guiScreen as GuiAccessor).zLevel = 100f
        renderItem.zLevel = 100f
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableLighting()

        renderItem.renderItemIntoGUI(memoryStack, 1, 1)
        GlStateManager.depthFunc(516)
        Gui.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128))
        GlStateManager.depthFunc(515)

        GlStateManager.enableLighting()
        RenderHelper.disableStandardItemLighting()

        (guiScreen as GuiAccessor).zLevel = 0f
        renderItem.zLevel = 0f
    }
}