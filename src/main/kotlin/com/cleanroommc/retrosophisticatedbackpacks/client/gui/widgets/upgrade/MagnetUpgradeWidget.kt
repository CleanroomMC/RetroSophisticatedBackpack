package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedMagnetUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.MagnetUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class MagnetUpgradeWidget(slotIndex: Int, wrapper: MagnetUpgradeWrapper, stack: ItemStack) :
    BasicExpandedTabWidget<MagnetUpgradeWrapper>(
        slotIndex,
        wrapper,
        stack,
        wrapper.settingsLangKey,
        coveredTabSize = filterTabSize(wrapper.filterItems.slots, wrapper.slotsInRow),
        width = filterTabWidth(wrapper.slotsInRow),
        contentX = 3,
        contentY = 24,
        contentWidth = wrapper.slotsInRow * 18,
        contentPadding = 0,
        filterWidth = wrapper.slotsInRow * 18
    ) {
    init {
        startingRow
            .height(20)
            .child(createPickupItemsButton(wrapper.pickupItems))
    }

    private fun createPickupItemsButton(pickupItems: Boolean): CyclicVariantButtonWidget =
        tabIconButton(PICKUP_ITEMS_VARIANTS, if (pickupItems) 0 else 1) {
            wrapper.pickupItems = !wrapper.pickupItems
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_MAGNET_PICKUP_ITEMS) {}
        }
}

class AdvancedMagnetUpgradeWidget(slotIndex: Int, wrapper: AdvancedMagnetUpgradeWrapper, stack: ItemStack) :
    AdvancedExpandedTabWidget<AdvancedMagnetUpgradeWrapper>(
        slotIndex,
        wrapper,
        stack,
        wrapper.settingsLangKey,
        coveredTabSize = filterTabSize(wrapper.filterItems.slots, wrapper.slotsInRow),
        width = filterTabWidth(wrapper.slotsInRow),
        contentX = 3,
        contentY = 24,
        contentWidth = wrapper.slotsInRow * 18,
        contentPadding = 0,
        filterWidth = wrapper.slotsInRow * 18
    ) {
    init {
        startingRow
            .height(20)
            .child(createPickupItemsButton(wrapper.pickupItems))
    }

    private fun createPickupItemsButton(pickupItems: Boolean): CyclicVariantButtonWidget =
        tabIconButton(PICKUP_ITEMS_VARIANTS, if (pickupItems) 0 else 1) {
            wrapper.pickupItems = !wrapper.pickupItems
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_MAGNET_PICKUP_ITEMS) {}
        }
}

private val PICKUP_ITEMS_VARIANTS = listOf(
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.pickup_items".asTranslationKey()), RSBTextures.MAGNET_PICKUP_ITEMS_ICON),
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.do_not_pickup_items".asTranslationKey()), RSBTextures.MAGNET_NO_PICKUP_ITEMS_ICON),
)
