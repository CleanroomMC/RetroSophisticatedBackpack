package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.TankUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.NoBackgroundItemSlot
import net.minecraft.item.ItemStack

class TankUpgradeWidget(slotIndex: Int, wrapper: TankUpgradeWrapper, stack: ItemStack) :
    ExpandedUpgradeTabWidget<TankUpgradeWrapper>(slotIndex, wrapper, 3, stack, wrapper.settingsLangKey, width = 48) {
    init {
        size(48, 80)

        val slots = SlotGroupWidget().name("tank_$slotIndex").disableSortButtons()
        slots.size(42, 50).pos(3, 24)
        slots.child(NoBackgroundItemSlot(RSBTextures.EMPTY_TANK_INPUT_SLOT).syncHandler("tank_$slotIndex", 0).pos(0, 0))
        slots.child(NoBackgroundItemSlot(RSBTextures.EMPTY_TANK_OUTPUT_SLOT).syncHandler("tank_$slotIndex", 1).pos(21, 0))
        slots.child(NoBackgroundItemSlot().syncHandler("tank_$slotIndex", 2).pos(0, 32))
        slots.child(NoBackgroundItemSlot().syncHandler("tank_$slotIndex", 3).pos(21, 32))
        child(slots)
    }

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        super.drawBackground(context, widgetTheme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 3, 24, 18, 18, widgetTheme.theme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 24, 24, 18, 18, widgetTheme.theme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 3, 56, 18, 18, widgetTheme.theme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 24, 56, 18, 18, widgetTheme.theme)
        RSBTextures.TANK_ARROW.draw(context, 4, 45, 15, 8, widgetTheme.theme)
        RSBTextures.TANK_ARROW.draw(context, 25, 45, 15, 8, widgetTheme.theme)
    }
}
