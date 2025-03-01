package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.core.mixin.GuiAccessor
import com.cleanroommc.modularui.core.mixin.GuiContainerAccessor
import com.cleanroommc.modularui.core.mixin.GuiScreenAccessor
import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.modularui.drawable.text.TextRenderer
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.utils.Alignment
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.utils.NumberFormat
import com.cleanroommc.modularui.widgets.ItemSlot
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
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import kotlin.math.min

class BackpackSlot(private val panel: BackpackPanel, private val wrapper: BackpackWrapper) : ItemSlot() {
    companion object {
        private val textRenderer = TextRenderer()
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
            formattedCount = NumberFormat.formatWithMaxDecimals(stack.count.toDouble(), 2)
            formattedStackLimit = NumberFormat.formatWithMaxDecimals(slot.getItemStackLimit(stack).toDouble(), 2)
        } else {
            super.buildTooltip(memorizedStack, tooltip)
            formattedCount = "0"
            formattedStackLimit =
                NumberFormat.formatWithMaxDecimals(slot.getItemStackLimit(memorizedStack).toDouble(), 2)
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

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        if (wrapper.isSlotLocked(slot.slotIndex))
            drawLockedSlot(context, widgetTheme)

        if (isInSettingMode) drawSettingStack(context, widgetTheme)
        else drawNormalStack(context, widgetTheme)
    }

    private fun drawSettingStack(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        val memoryStack = wrapper.backpackItemStackHandler.memorizedSlotStack[slot.slotIndex]
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
        val slot = slot as? ModularBackpackSlot ?: return
        val memoryStack = slot.getMemoryStack()

        superDraw()

        if (slot.stack.isEmpty && !memoryStack.isEmpty)
            drawMemoryStack(memoryStack, context, widgetTheme)
    }

    private fun drawMemoryStack(memoryStack: ItemStack, context: ModularGuiContext, widgetTheme: WidgetTheme) {
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

    private fun drawLockedSlot(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        RSBTextures.NO_SORT_ICON.draw(context, 1, 1, 16, 16, widgetTheme)
        GlStateManager.depthFunc(516)
        Gui.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128))
        GlStateManager.depthFunc(515)
    }

    private fun superDraw() {
        RenderHelper.enableGUIStandardItemLighting()
        drawSlot(slot)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.disableLighting()
        if (isHovering) {
            GlStateManager.colorMask(true, true, true, false)
            GuiDraw.drawRect(1f, 1f, 16f, 16f, getSlotHoverColor())
            GlStateManager.colorMask(true, true, true, true)
        }
    }

    private fun drawSlot(slotIn: Slot) {
        val guiScreen = screen.screenWrapper.guiScreen
        check(guiScreen is GuiContainer) { "The gui must be an instance of GuiContainer if it contains slots!" }
        val acc = guiScreen as GuiContainerAccessor
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender
        var itemstack: ItemStack = slotIn.stack
        var flag = false
        val flag1 = slotIn === acc.clickedSlot && !acc.draggedStack.isEmpty && !acc.isRightMouseClick
        val itemstack1 = guiScreen.mc.player.inventory.itemStack
        var amount = -1
        var format: String? = null

        if (slotIn === acc.clickedSlot && !acc.draggedStack
                .isEmpty && acc.isRightMouseClick && !itemstack.isEmpty
        ) {
            itemstack = itemstack.copy()
            itemstack.setCount(itemstack.count / 2)
        } else if (acc.dragSplitting && acc.dragSplittingSlots.contains(slotIn) && !itemstack1.isEmpty) {
            if (acc.dragSplittingSlots.size == 1) {
                return
            }

            if (Container.canAddItemToSlot(slotIn, itemstack1, true) && screen.getContainer()
                    .canDragIntoSlot(slotIn)
            ) {
                itemstack = itemstack1.copy()
                flag = true
                Container.computeStackSize(
                    acc.dragSplittingSlots,
                    acc.dragSplittingLimit,
                    itemstack,
                    if (slotIn.stack.isEmpty) 0 else slotIn.stack.count
                )
                val k =
                    min(itemstack.maxStackSize.toDouble(), slotIn.getItemStackLimit(itemstack).toDouble()).toInt()

                if (itemstack.count > k) {
                    amount = k
                    format = TextFormatting.YELLOW.toString()
                    itemstack.setCount(k)
                }
            } else {
                acc.dragSplittingSlots.remove(slotIn)
                acc.invokeUpdateDragSplitting()
            }
        }

        (guiScreen as GuiAccessor).zLevel = 100f
        renderItem.zLevel = 100.0f

        if (!flag1) {
            if (flag) {
                GuiDraw.drawRect(1f, 1f, 16f, 16f, -2130706433)
            }

            if (!itemstack.isEmpty) {
                GlStateManager.enableDepth()
                // render the item itself
                renderItem.renderItemAndEffectIntoGUI(guiScreen.mc.player, itemstack, 1, 1)
                if (amount < 0) {
                    amount = itemstack.count
                }
                // render the amount overlay
                if (amount > 1 || format != null) {
                    var amountText = NumberFormat.formatWithMaxDecimals(amount.toDouble(), 1)
                    if (format != null) {
                        amountText = format + amountText
                    }
                    var scale = 1f
                    if (amountText.length == 3) {
                        scale = 0.8f
                    } else if (amountText.length == 4) {
                        scale = 0.6f
                    } else if (amountText.length > 4) {
                        scale = 0.5f
                    }
                    textRenderer.setShadow(true)
                    textRenderer.setScale(scale)
                    textRenderer.setColor(Color.WHITE.main)
                    textRenderer.setAlignment(
                        Alignment.BottomRight,
                        (area.width - 1).toFloat(),
                        (area.height - 1).toFloat()
                    )
                    textRenderer.setPos(1, 1)
                    GlStateManager.disableLighting()
                    GlStateManager.disableDepth()
                    GlStateManager.disableBlend()
                    textRenderer.draw(amountText)
                    GlStateManager.enableLighting()
                    GlStateManager.enableDepth()
                    GlStateManager.enableBlend()
                }

                val cachedCount = itemstack.getCount()
                itemstack.setCount(1) // required to not render the amount overlay
                // render other overlays like durability bar
                renderItem.renderItemOverlayIntoGUI(
                    (guiScreen as GuiScreenAccessor).getFontRenderer(),
                    itemstack,
                    1,
                    1,
                    null
                )
                itemstack.setCount(cachedCount)
                GlStateManager.disableDepth()
            }
        }

        (guiScreen as GuiAccessor).setZLevel(0f)
        renderItem.zLevel = 0f
    }
}