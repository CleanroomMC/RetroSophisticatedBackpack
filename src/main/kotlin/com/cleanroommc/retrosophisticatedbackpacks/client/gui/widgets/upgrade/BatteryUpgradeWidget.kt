package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.BatteryUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.NoBackgroundItemSlot
import net.minecraft.item.ItemStack

class BatteryUpgradeWidget(
    slotIndex: Int,
    wrapper: BatteryUpgradeWrapper,
    stack: ItemStack
) : ExpandedUpgradeTabWidget<BatteryUpgradeWrapper>(slotIndex, wrapper, 3, stack, wrapper.settingsLangKey, width = 48) {
    init {
        size(48, 48)
        val slots = SlotGroupWidget().name("battery_$slotIndex").disableSortButtons()
        slots.size(42, 18).pos(3, 24)
        slots.child(NoBackgroundItemSlot(RSBTextures.EMPTY_BATTERY_INPUT_SLOT).syncHandler("battery_$slotIndex", 0).pos(0, 0))
        slots.child(NoBackgroundItemSlot(RSBTextures.EMPTY_BATTERY_OUTPUT_SLOT).syncHandler("battery_$slotIndex", 1).pos(21, 0))
        child(slots)
    }

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        super.drawBackground(context, widgetTheme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 3, 24, 18, 18, widgetTheme.theme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 24, 24, 18, 18, widgetTheme.theme)
    }
}
