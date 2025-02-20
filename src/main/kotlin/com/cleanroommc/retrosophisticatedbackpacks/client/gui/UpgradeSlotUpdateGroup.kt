package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.AdvancedPickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.inventory.slot.FilterSlot
import com.cleanroommc.retrosophisticatedbackpacks.sync.DelegatedStackHandlerSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.FilterSlotSH

class UpgradeSlotUpdateGroup(
    private val syncManager: PanelSyncManager,
    private val wrapper: BackpackWrapper,
    private val slotIndex: Int
) {
    var pickupFilterStackHandler = DelegatedStackHandlerSH(wrapper, slotIndex)
    val pickupFilterSlots: Array<ModularSlot>

    var advancedFilterStackHandler = DelegatedStackHandlerSH(wrapper, slotIndex)
    val advancedFilterSlots: Array<ModularSlot>

    init {
        syncManager.syncValue("pickup_filter_delegation_$slotIndex", pickupFilterStackHandler)

        pickupFilterSlots = Array(9) {
            val slot = FilterSlot(pickupFilterStackHandler.delegatedStackHandler, it)
            slot.slotGroup("pickup_filters_$slotIndex")

            syncManager.syncValue(
                "pickup_filter_$slotIndex",
                it,
                FilterSlotSH(slot)
            )

            slot
        }

        syncManager.registerSlotGroup(SlotGroup("pickup_filters_$slotIndex", 4, false))

        syncManager.syncValue("adv_pickup_filter_delegation_$slotIndex", advancedFilterStackHandler)

        advancedFilterSlots = Array(16) {
            val slot = FilterSlot(advancedFilterStackHandler.delegatedStackHandler, it)
            slot.slotGroup("adv_pickup_filters_$slotIndex")

            syncManager.syncValue(
                "adv_pickup_filter_$slotIndex",
                it,
                FilterSlotSH(slot)
            )

            slot
        }

        syncManager.registerSlotGroup(SlotGroup("adv_pickup_filters_$slotIndex", 4, false))
    }

    fun updatePickupFilterDelegate(pickupWrapper: PickupUpgradeWrapper) {
        pickupFilterStackHandler.setDelegatedStackHandler(pickupWrapper::filterItems)
        pickupFilterStackHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_PICKUP_FILTER)
    }

    fun updateAdvancedPickupFilterDelegate(pickupWrapper: AdvancedPickupUpgradeWrapper) {
        advancedFilterStackHandler.setDelegatedStackHandler(pickupWrapper::filterItems)
        advancedFilterStackHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_ADVANCED_PICKUP_FILTER)
    }
}