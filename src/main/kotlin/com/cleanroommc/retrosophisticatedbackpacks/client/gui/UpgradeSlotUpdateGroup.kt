package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularFilterSlot
import com.cleanroommc.retrosophisticatedbackpacks.sync.DelegatedStackHandlerSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.FilterSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.FoodFilterSlotSH

class UpgradeSlotUpdateGroup(
    private val panel: BackpackPanel,
    private val wrapper: BackpackWrapper,
    private val slotIndex: Int
) {
    // Common Filters
    var commonFilterStackHandler = DelegatedStackHandlerSH(wrapper, slotIndex)
    val commonFilterSlots: Array<ModularSlot>

    var advancedCommonFilterStackHandler = DelegatedStackHandlerSH(wrapper, slotIndex)
    val advancedCommonFilterSlots: Array<ModularSlot>

    // Feeding filters
    val feedingFilterSlots: Array<ModularSlot>
    val advancedFeedingFilterSlots: Array<ModularSlot>

    init {
        val syncManager = panel.syncManager

        syncManager.syncValue("common_filter_delegation_$slotIndex", commonFilterStackHandler)

        commonFilterSlots = Array(9) {
            val slot = ModularFilterSlot(commonFilterStackHandler.delegatedStackHandler, it)
            slot.slotGroup("common_filters_$slotIndex")

            syncManager.syncValue(
                "common_filter_$slotIndex",
                it,
                FilterSlotSH(slot)
            )

            slot
        }

        syncManager.registerSlotGroup(SlotGroup("common_filters_$slotIndex", 4, false))

        syncManager.syncValue("adv_common_filter_delegation_$slotIndex", advancedCommonFilterStackHandler)

        advancedCommonFilterSlots = Array(16) {
            val slot = ModularFilterSlot(advancedCommonFilterStackHandler.delegatedStackHandler, it)
            slot.slotGroup("adv_common_filters_$slotIndex")

            syncManager.syncValue(
                "adv_common_filter_$slotIndex",
                it,
                FilterSlotSH(slot)
            )

            slot
        }

        syncManager.registerSlotGroup(SlotGroup("adv_common_filters_$slotIndex", 4, false))

        // Feeding Filter Slots
        feedingFilterSlots = Array(9) {
            val slot = ModularFilterSlot(commonFilterStackHandler.delegatedStackHandler, it)
            slot.slotGroup("feeding_filters_$slotIndex")

            syncManager.syncValue(
                "feeding_filter_$slotIndex",
                it,
                FoodFilterSlotSH(slot)
            )

            slot
        }

        syncManager.registerSlotGroup(SlotGroup("feeding_filters_$slotIndex", 4, false))

        advancedFeedingFilterSlots = Array(16) {
            val slot = ModularFilterSlot(advancedCommonFilterStackHandler.delegatedStackHandler, it)
            slot.slotGroup("adv_feeding_filters_$slotIndex")

            syncManager.syncValue(
                "adv_feeding_filter_$slotIndex",
                it,
                FoodFilterSlotSH(slot)
            )

            slot
        }

        syncManager.registerSlotGroup(SlotGroup("adv_feeding_filters_$slotIndex", 4, false))
    }

    fun updateFilterDelegate(wrapper: IBasicFilterable) {
        commonFilterStackHandler.setDelegatedStackHandler(wrapper::filterItems)
        commonFilterStackHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE)
    }

    fun updateAdvancedFilterDelegate(wrapper: IAdvancedFilterable) {
        advancedCommonFilterStackHandler.setDelegatedStackHandler(wrapper::filterItems)
        advancedCommonFilterStackHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE)
    }
}