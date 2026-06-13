package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.UpOrDown
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedRefillUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.RefillUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting

class RefillUpgradeWidget(slotIndex: Int, wrapper: RefillUpgradeWrapper, stack: ItemStack) :
    BasicExpandedTabWidget<RefillUpgradeWrapper>(
        slotIndex,
        wrapper,
        stack,
        wrapper.settingsLangKey,
        coveredTabSize = refillCoveredTabSize(wrapper),
        width = refillTabWidth(wrapper),
        contentX = 3,
        contentY = 24,
        contentWidth = wrapper.slotsInRow * 18,
        contentPadding = 0,
        filterWidth = wrapper.slotsInRow * 18,
        showFilterButton = false
    ) {
    init {
        startingRow.height(0)
    }
}

class AdvancedRefillUpgradeWidget(slotIndex: Int, wrapper: AdvancedRefillUpgradeWrapper, stack: ItemStack) :
    BasicExpandedTabWidget<AdvancedRefillUpgradeWrapper>(
        slotIndex,
        wrapper,
        stack,
        wrapper.settingsLangKey,
        filterSyncKey = "adv_common_filter",
        coveredTabSize = refillCoveredTabSize(wrapper),
        width = refillTabWidth(wrapper),
        contentX = 3,
        contentY = 24,
        contentWidth = wrapper.slotsInRow * 18,
        contentPadding = 0,
        filterWidth = wrapper.slotsInRow * 18,
        showFilterButton = false,
        slotFactory = { filterSlot, syncHandler -> RefillTargetSlot(wrapper, filterSlot, syncHandler) }
    ) {
    init {
        startingRow.height(0)
    }
}

private fun refillTabWidth(wrapper: RefillUpgradeWrapper): Int =
    maxOf(75, 3 + wrapper.slotsInRow * 18 + 6)

private fun refillCoveredTabSize(wrapper: RefillUpgradeWrapper): Int {
    val slotsInRow = wrapper.slotsInRow.coerceAtLeast(1)
    val rows = (wrapper.filterItems.slots + slotsInRow - 1) / slotsInRow
    return ((24 + rows * 18 + 6 + 29) / 30).coerceAtLeast(3)
}

private class RefillTargetSlot(
    private val wrapper: AdvancedRefillUpgradeWrapper,
    private val filterSlot: Int,
    private val upgradeSyncHandler: () -> UpgradeSlotSH?
) : PhantomItemSlot() {
    override fun onMouseScroll(scrollDirection: UpOrDown, amount: Int): Boolean {
        if (slot.stack.isEmpty) {
            return super.onMouseScroll(scrollDirection, amount)
        }

        val targetSlot = if (scrollDirection.isUp) wrapper.getTargetSlot(filterSlot).next()
        else wrapper.getTargetSlot(filterSlot).previous()
        wrapper.setTargetSlot(filterSlot, targetSlot)
        upgradeSyncHandler()?.syncToServer(UpgradeSlotSH.UPDATE_REFILL_TARGET_SLOT) {
            it.writeInt(filterSlot)
            it.writeEnumValue(targetSlot)
        }
        markTooltipDirty()
        return true
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)
        if (!isSynced || slot.stack.isEmpty) {
            return
        }

        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        Minecraft.getMinecraft().fontRenderer.drawString(wrapper.getTargetSlot(filterSlot).acronym(), 10, 2, 0x55FF55)
        GlStateManager.enableDepth()
    }

    override fun buildTooltip(stack: ItemStack, tooltip: RichTooltip) {
        super.buildTooltip(stack, tooltip)
        if (stack.isEmpty) {
            return
        }

        val targetSlot = wrapper.getTargetSlot(filterSlot)
        val targetDescription = I18n.format(targetSlot.descriptionKey())
        tooltip.addLine(IKey.str(I18n.format("gui.refill_target_tooltip".asTranslationKey(), targetDescription)).style(targetSlot.descriptionColor()))
            .addLine(IKey.lang("gui.refill_scroll_tooltip".asTranslationKey()).style(TextFormatting.DARK_GRAY, TextFormatting.ITALIC))
    }
}
