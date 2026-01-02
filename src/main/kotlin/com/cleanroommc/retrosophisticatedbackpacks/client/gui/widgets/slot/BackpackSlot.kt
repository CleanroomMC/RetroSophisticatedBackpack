package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiContainerAccessor
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor
import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.modularui.drawable.text.TextRenderer
import com.cleanroommc.modularui.screen.NEAAnimationHandler
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.utils.NumberFormat
import com.cleanroommc.modularui.utils.Platform
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.min

class BackpackSlot(private val panel: BackpackPanel, private val wrapper: BackpackWrapper) : ItemSlot() {
    companion object {
        private val textRenderer = TextRenderer()
        val DECIMAL_TWO: NumberFormat.Params = NumberFormat.AMOUNT_TEXT.copyToBuilder()
            .maxLength(2)
            .considerOnlyDecimalsForLength(true)
            .build()
        val DECIMAL_ONE: NumberFormat.Params = NumberFormat.AMOUNT_TEXT.copyToBuilder()
            .maxLength(1)
            .considerOnlyDecimalsForLength(true)
            .build()
    }


    private val isInSettingMode: Boolean
        get() = panel.settingPanel.isPanelOpen
    private val isInMemorySettingMode: Boolean
        get() = panel.isMemorySettingTabOpened
    private val isInSortSettingMode: Boolean
        get() = panel.isSortingSettingTabOpened

    override fun buildTooltip(stack: ItemStack, tooltip: RichTooltip) {
        val memorizedStack = wrapper.getMemorizedStack(slot.slotIndex)

        if (stack.isEmpty && memorizedStack.isEmpty)
            return

        val formattedCount: String
        val formattedStackLimit: String

        if (!stack.isEmpty) {
            super.buildTooltip(stack, tooltip)

            //NumberFormat.format(amount, NumberFormat.AMOUNT_TEXT);
            formattedCount = NumberFormat.format(stack.count.toDouble(), DECIMAL_TWO)
            formattedStackLimit = NumberFormat.format(slot.getItemStackLimit(stack).toDouble(), DECIMAL_TWO)
        } else {
            super.buildTooltip(memorizedStack, tooltip)
            formattedCount = "0"
            formattedStackLimit =
                NumberFormat.format(slot.getItemStackLimit(memorizedStack).toDouble(), DECIMAL_TWO)
        }

        tooltip.addLine(
            IKey.lang(
                "gui.stack_size_extra".asTranslationKey(),
                TextComponentString(formattedCount).setStyle(Style().setColor(TextFormatting.AQUA)).formattedText,
                TextComponentString(formattedStackLimit).setStyle(Style().setColor(TextFormatting.AQUA)).formattedText
            )
        )

        if (wrapper.isSlotMemorized(slot.slotIndex)) {
            tooltip.addLine(IKey.lang("gui.memorized_slot".asTranslationKey()).style(IKey.LIGHT_PURPLE))

            if (wrapper.isMemoryStackRespectNBT(slot.slotIndex)) {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.match_nbt".asTranslationKey()))
                        .style(TextFormatting.YELLOW)
                )
            } else {
                tooltip.addLine(
                    IKey.comp(IKey.str("- "), IKey.lang("gui.ignore_nbt".asTranslationKey()))
                        .style(TextFormatting.GRAY)
                )
            }
        }

        if (wrapper.isSlotLocked(slot.slotIndex)) {
            tooltip.addLine(IKey.lang("gui.no_sorting_slot".asTranslationKey()).style(TextFormatting.DARK_RED))
        }
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result =
        if (isInMemorySettingMode) {
            val isMemorySet = wrapper.isSlotMemorized(slot.slotIndex)

            if (isMemorySet && mouseButton == 1) {
                wrapper.unsetMemoryStack(slot.slotIndex)
                syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_MEMORY_STACK)
                Interactable.Result.SUCCESS
            } else if (!isMemorySet && mouseButton == 0) {
                wrapper.setMemoryStack(slot.slotIndex, panel.shouldMemorizeRespectNBT)
                syncHandler.syncToServer(BackpackSlotSH.UPDATE_SET_MEMORY_STACK) {
                    it.writeBoolean(panel.shouldMemorizeRespectNBT)
                }
                Interactable.Result.SUCCESS
            } else Interactable.Result.STOP
        } else if (isInSortSettingMode) {
            val isSlotLocked = wrapper.isSlotLocked(slot.slotIndex)

            if (isSlotLocked && mouseButton == 1) {
                wrapper.setSlotLocked(slot.slotIndex, false)
                syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_SLOT_LOCK)
                Interactable.Result.SUCCESS
            } else if (!isSlotLocked && mouseButton == 0) {
                wrapper.setSlotLocked(slot.slotIndex, true)
                syncHandler.syncToServer(BackpackSlotSH.UPDATE_SET_SLOT_LOCK)
                Interactable.Result.SUCCESS
            } else Interactable.Result.STOP
        } else if (isInSettingMode) {
            Interactable.Result.STOP
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

    @SideOnly(Side.CLIENT)
    override fun draw(context: ModularGuiContext?, widgetThemeEntry: WidgetThemeEntry<*>?) {
        context?.let {
            val widgetTheme = widgetThemeEntry?.theme ?: WidgetTheme.getDefault().theme
            if (wrapper.isSlotLocked(slot.slotIndex))
                drawLockedSlot(it, widgetTheme)


            if (isInSettingMode) drawSettingStack(context, widgetTheme)
            else {
                val slot = slot as? ModularBackpackSlot ?: return
                val memoryStack = slot.getMemoryStack()

                super.draw(context, widgetThemeEntry)

                if (slot.stack.isEmpty && !memoryStack.isEmpty)
                    drawMemoryStack(memoryStack, context, widgetTheme)

            }//drawNormalStack(context, widgetTheme)
        }
    }

    @SideOnly(Side.CLIENT)
    private fun drawSettingStack(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        val slot = slot as? ModularBackpackSlot ?: return
        val memoryStack = slot.getMemoryStack()
        val guiScreen = screen.screenWrapper.guiScreen
        check(guiScreen is GuiContainer) { "The gui must be an instance of GuiContainer if it contains slots!" }
        val guiContainer = guiScreen as GuiContainer
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender

        // makes sure items of different layers don't interfere with each other visually
        val z = (context.currentDrawingZ + 100).toFloat()
        (guiScreen as GuiAccessor).zLevel = z
        renderItem.zLevel = z

        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableLighting()
        val useMemory = slot.stack == null || slot.stack.isEmpty()
        val chosenstack = if(useMemory) memoryStack else slot.stack

        val itemstack = NEAAnimationHandler.injectVirtualStack(chosenstack, guiContainer, slot)

        Platform.setupDrawItem()
        if(!useMemory) NEAAnimationHandler.injectHoverScale(guiContainer, slot)
        renderItem.renderItemIntoGUI(itemstack, 1, 1)
        Platform.endDrawItem()
        if(!useMemory) NEAAnimationHandler.endHoverScale()

        if (!memoryStack.isEmpty) {
            GlStateManager.depthFunc(516)
            Gui.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128))
            GlStateManager.depthFunc(515)
        }
        RenderHelper.enableStandardItemLighting()

        //GlStateManager.enableLighting()
        //RenderHelper.disableStandardItemLighting()

        (guiScreen as GuiAccessor).zLevel = 0f
        renderItem.zLevel = 0f
    }

    @SideOnly(Side.CLIENT)
    private fun drawMemoryStack(memoryStack: ItemStack, context: ModularGuiContext, widgetTheme: WidgetTheme) {
        val memoryStack = wrapper.backpackItemStackHandler.memorizedSlotStack[slot.slotIndex]
        val guiScreen = screen.screenWrapper.guiScreen
        check(guiScreen is GuiContainer) { "The gui must be an instance of GuiContainer if it contains slots!" }
        val guiContainer = guiScreen as GuiContainer
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender

        // makes sure items of different layers don't interfere with each other visually
        val z = (context.currentDrawingZ + 100).toFloat()
        (guiScreen as GuiAccessor).zLevel = z
        renderItem.zLevel = z
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableLighting()

        val itemstack = NEAAnimationHandler.injectVirtualStack(memoryStack, guiContainer, slot)

        Platform.setupDrawItem()
        //val itemScale = NEAAnimationHandler.injectHoverScale(guiContainer, slotIn)
        renderItem.renderItemIntoGUI(itemstack, 1, 1)
        Platform.endDrawItem()
        //NEAAnimationHandler.endHoverScale()

        if (!memoryStack.isEmpty) {
            GlStateManager.depthFunc(516)
            Gui.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128))
            GlStateManager.depthFunc(515)
        }
        RenderHelper.enableStandardItemLighting()

        //GlStateManager.enableLighting()
        //RenderHelper.disableStandardItemLighting()

        (guiScreen as GuiAccessor).zLevel = 0f
        renderItem.zLevel = 0f
    }

    @SideOnly(Side.CLIENT)
    private fun drawLockedSlot(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        RSBTextures.NO_SORT_ICON.draw(context, 1, 1, 16, 16, widgetTheme)
        GlStateManager.depthFunc(516)
        Gui.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128))
        GlStateManager.depthFunc(515)
    }

}