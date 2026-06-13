package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiAccessor
import com.cleanroommc.modularui.core.mixins.early.minecraft.GuiScreenAccessor
import com.cleanroommc.modularui.screen.NEAAnimationHandler
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.utils.NumberFormat
import com.cleanroommc.modularui.utils.Platform
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.handler.ClientGuiStashHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SStashToBackpackPacket
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.DyeColorUtils
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class BackpackSlot(private val panel: BackpackPanel, private val wrapper: BackpackWrapper) : NoBackgroundItemSlot() {
    companion object {
        val DECIMAL_TWO: NumberFormat.Params = NumberFormat.AMOUNT_TEXT.copyToBuilder()
            .maxLength(2)
            .considerOnlyDecimalsForLength(true)
            .build()
    }

    private val isInSettingMode: Boolean
        get() = panel.isSettingMode
    private val isInMemorySettingMode: Boolean
        get() = panel.isMemorySettingTabOpened
    private val isInSortSettingMode: Boolean
        get() = panel.isSortingSettingTabOpened
    private val isInItemDisplaySettingMode: Boolean
        get() = panel.isItemDisplaySettingTabOpened
    private val isBlockedByCapturedMob: Boolean
        get() = wrapper.isSlotBlockedByMobCatcher(slot.slotIndex)
    private val isHiddenBySearch: Boolean
        get() = !panel.isSearchSlotVisible(slot.slotIndex)

    override fun onUpdate() {
        super.onUpdate()
        updateSearchPosition()
    }

    override fun buildTooltip(stack: ItemStack, tooltip: RichTooltip) {
        if (isBlockedByCapturedMob || isHiddenBySearch)
            return

        val memorizedStack = wrapper.getMemorizedStack(slot.slotIndex)

        if (stack.isEmpty && memorizedStack.isEmpty)
            return

        val formattedCount: String
        val formattedStackLimit: String

        if (!stack.isEmpty) {
            super.buildTooltip(stack, tooltip)

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
        when {
            isBlockedByCapturedMob -> Interactable.Result.STOP
            isHiddenBySearch -> Interactable.Result.STOP
            isInItemDisplaySettingMode -> handleItemDisplaySlotClick(mouseButton)
            isInMemorySettingMode -> handleMemorySlotClick(mouseButton)
            isInSortSettingMode -> handleSortSlotClick(mouseButton)
            isInSettingMode -> Interactable.Result.STOP
            mouseButton == 1 && handleStashClick() -> Interactable.Result.SUCCESS
            else -> super.onMousePressed(mouseButton)
        }

    override fun onMouseRelease(mouseButton: Int): Boolean =
        if (isBlockedByCapturedMob || isHiddenBySearch || isInSettingMode) true
        else super.onMouseRelease(mouseButton)

    override fun onMouseDrag(mouseButton: Int, timeSinceClick: Long) {
        if (isBlockedByCapturedMob || isHiddenBySearch) {
            return
        }
        if (isInMemorySettingMode) {
            handleMemorySlotClick(mouseButton)
            return
        }
        if (isInSortSettingMode) {
            handleSortSlotClick(mouseButton)
            return
        }
        if (isInItemDisplaySettingMode) {
            handleItemDisplaySlotClick(mouseButton)
            return
        }
        if (isInSettingMode) {
            return
        }

        super.onMouseDrag(mouseButton, timeSinceClick)
    }

    private fun handleMemorySlotClick(mouseButton: Int): Interactable.Result {
        val isMemorySet = wrapper.isSlotMemorized(slot.slotIndex)

        return if (isMemorySet && mouseButton == 1) {
            wrapper.unsetMemoryStack(slot.slotIndex)
            syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_MEMORY_STACK)
            Utils.invalidateSortingContext()
            Interactable.Result.SUCCESS
        } else if (!isMemorySet && mouseButton == 0) {
            wrapper.setMemoryStack(slot.slotIndex, panel.shouldMemorizeRespectNBT)
            syncHandler.syncToServer(BackpackSlotSH.UPDATE_SET_MEMORY_STACK) {
                it.writeBoolean(panel.shouldMemorizeRespectNBT)
            }
            Utils.invalidateSortingContext()
            Interactable.Result.SUCCESS
        } else Interactable.Result.STOP
    }

    private fun handleItemDisplaySlotClick(mouseButton: Int): Interactable.Result {
        val slotIndex = slot.slotIndex
        return when {
            mouseButton == 0 && !wrapper.isItemDisplaySlotSelected(slotIndex) -> {
                wrapper.selectItemDisplaySlot(slotIndex)
                if (wrapper.isItemDisplaySlotSelected(slotIndex)) {
                    panel.setCurrentItemDisplaySelectedSlot(slotIndex)
                    panel.backpackSyncHandler.syncToServer(BackpackSH.UPDATE_ITEM_DISPLAY_SLOT) {
                        it.writeInt(slotIndex)
                        it.writeBoolean(true)
                    }
                    Interactable.Result.SUCCESS
                } else Interactable.Result.STOP
            }

            mouseButton == 1 && wrapper.isItemDisplaySlotSelected(slotIndex) -> {
                wrapper.unselectItemDisplaySlot(slotIndex)
                panel.setCurrentItemDisplaySelectedSlot(wrapper.getFirstItemDisplaySlot())
                panel.backpackSyncHandler.syncToServer(BackpackSH.UPDATE_ITEM_DISPLAY_SLOT) {
                    it.writeInt(slotIndex)
                    it.writeBoolean(false)
                }
                Interactable.Result.SUCCESS
            }

            else -> Interactable.Result.STOP
        }
    }

    private fun handleSortSlotClick(mouseButton: Int): Interactable.Result {
        val isSlotLocked = wrapper.isSlotLocked(slot.slotIndex)

        return if (isSlotLocked && mouseButton == 1) {
            wrapper.setSlotLocked(slot.slotIndex, false)
            syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_SLOT_LOCK)
            Utils.invalidateSortingContext()
            Interactable.Result.SUCCESS
        } else if (!isSlotLocked && mouseButton == 0) {
            wrapper.setSlotLocked(slot.slotIndex, true)
            syncHandler.syncToServer(BackpackSlotSH.UPDATE_SET_SLOT_LOCK)
            Utils.invalidateSortingContext()
            Interactable.Result.SUCCESS
        } else Interactable.Result.STOP
    }

    @SideOnly(Side.CLIENT)
    override fun draw(context: ModularGuiContext?, widgetThemeEntry: WidgetThemeEntry<*>?) {
        context?.let {
            updateSearchPosition()
            if (isBlockedByCapturedMob)
                return

            val widgetTheme = widgetThemeEntry?.theme ?: WidgetTheme.getDefault().theme

            if (isInSettingMode) {
                if (panel.isSlotSettingTabOpened)
                    drawSettingStack(context, widgetTheme)
                drawSettingOverlays(it, widgetTheme)
            } else {
                val slot = slot as? ModularBackpackSlot ?: return
                val memoryStack = slot.getMemoryStack()

                if (wrapper.isSlotLocked(slot.slotIndex))
                    drawLockedSlot(it, widgetTheme)

                if (isHiddenBySearch)
                    return

                super.draw(context, widgetThemeEntry)

                if (slot.stack.isEmpty && !memoryStack.isEmpty) {
                    drawMemoryStack(memoryStack, context, widgetTheme)
                    drawMemorizedSlotOverlay(context, widgetTheme)
                }
                drawStashSign()
            }
        }
    }

    private fun updateSearchPosition() {
        if (!isSynced)
            return

        val slotIndex = slot.slotIndex
        val x = panel.searchSlotX(slotIndex)
        val y = panel.searchSlotY(slotIndex)
        if (area.rx == x && area.ry == y)
            return

        area.rx = x
        area.ry = y
    }

    private fun handleStashClick(): Boolean {
        val player = Minecraft.getMinecraft().player ?: return false
        val carried = player.inventory.itemStack
        if (carried.isEmpty) {
            return false
        }
        val action = ClientGuiStashHandler.getStashActionForSlot(player, slot, carried) ?: return false
        NetworkHandler.INSTANCE.sendToServer(C2SStashToBackpackPacket(slot.slotNumber, action))
        return true
    }

    @SideOnly(Side.CLIENT)
    private fun drawStashSign() {
        val player = Minecraft.getMinecraft().player ?: return
        val carried = player.inventory.itemStack
        if (carried.isEmpty) {
            return
        }
        val (sign, result) = ClientGuiStashHandler.getStashResultForSlot(player, slot, carried) ?: return
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(
            sign,
            if (sign == "+") 10f else 1f,
            if (sign == "+") 8f else 0f,
            ClientGuiStashHandler.color(result)
        )
    }

    @SideOnly(Side.CLIENT)
    private fun drawSettingStack(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        val slot = slot as? ModularBackpackSlot ?: return
        val memoryStack = slot.getMemoryStack()
        val guiScreen = screen.screenWrapper.guiScreen
        check(guiScreen is GuiContainer) { "The gui must be an instance of GuiContainer if it contains slots!" }
        val guiContainer = guiScreen
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender

        // makes sure items of different layers don't interfere with each other visually
        val z = (context.currentDrawingZ + 100).toFloat()
        (guiScreen as GuiAccessor).zLevel = z
        renderItem.zLevel = z

        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableLighting()

        val useMemory = slot.stack.isEmpty
        val chosenStack = if (useMemory) memoryStack else slot.stack
        val itemStack = NEAAnimationHandler.injectVirtualStack(chosenStack, guiContainer, slot)

        Platform.setupDrawItem()

        if (!useMemory) {
            NEAAnimationHandler.injectHoverScale(guiContainer, slot)
        }

        renderItem.renderItemIntoGUI(itemStack, 1, 1)
        Platform.endDrawItem()

        if (!useMemory) {
            NEAAnimationHandler.endHoverScale()
        }

        RenderHelper.enableStandardItemLighting()

        GlStateManager.disableLighting()

        (guiScreen as GuiAccessor).zLevel = 0f
        renderItem.zLevel = 0f
    }

    @SideOnly(Side.CLIENT)
    private fun drawMemoryStack(memoryStack: ItemStack, context: ModularGuiContext, widgetTheme: WidgetTheme) {
        val guiScreen = screen.screenWrapper.guiScreen
        check(guiScreen is GuiContainer) { "The gui must be an instance of GuiContainer if it contains slots!" }
        val guiContainer = guiScreen
        val renderItem = (guiScreen as GuiScreenAccessor).itemRender

        // makes sure items of different layers don't interfere with each other visually
        val z = (context.currentDrawingZ + 100).toFloat()
        (guiScreen as GuiAccessor).zLevel = z
        renderItem.zLevel = z
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.disableLighting()

        val itemstack = NEAAnimationHandler.injectVirtualStack(memoryStack, guiContainer, slot)

        Platform.setupDrawItem()

        renderItem.renderItemIntoGUI(itemstack, 1, 1)
        Platform.endDrawItem()

        RenderHelper.enableStandardItemLighting()
        GlStateManager.disableLighting()

        (guiScreen as GuiAccessor).zLevel = 0f
        renderItem.zLevel = 0f
    }

    @SideOnly(Side.CLIENT)
    private fun drawSettingOverlays(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        if (wrapper.isSlotLocked(slot.slotIndex))
            drawLockedSlot(context, widgetTheme)
        if (wrapper.isSlotMemorized(slot.slotIndex))
            drawMemorizedSlotOverlay(context, widgetTheme)
        if (wrapper.isItemDisplaySlotSelected(slot.slotIndex))
            drawItemDisplayOverlay(context, widgetTheme)
    }

    @SideOnly(Side.CLIENT)
    private fun drawMemorizedSlotOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        RSBTextures.MEMORIZED_SLOT_OVERLAY.draw(context, 1, 1, 16, 16, widgetTheme)
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
    }

    @SideOnly(Side.CLIENT)
    private fun drawLockedSlot(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        RSBTextures.NO_SORT_ICON.draw(context, 1, 1, 16, 16, widgetTheme)
        GlStateManager.depthFunc(516)
        Gui.drawRect(1, 1, 17, 17, Color.argb(139, 139, 139, 128))
        GlStateManager.depthFunc(515)
    }

    @SideOnly(Side.CLIENT)
    private fun drawItemDisplayOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        val color = DyeColorUtils.colorValue(wrapper.itemDisplayColor)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        Gui.drawRect(1, 1, 17, 17, color and 0x00FFFFFF or (80 shl 24))
        if (panel.currentItemDisplaySelectedSlot == slot.slotIndex) {
            GlStateManager.colorMask(true, true, true, false)
            RSBTextures.SLOT_SELECTION.draw(context, -3, -3, 24, 24, widgetTheme)
            GlStateManager.colorMask(true, true, true, true)
        }
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
    }
}
