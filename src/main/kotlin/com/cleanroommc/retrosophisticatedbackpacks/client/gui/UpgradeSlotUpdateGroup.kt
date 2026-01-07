package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.CraftingSlotInfo
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularFilterSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.IndexedModularCraftingSlot
import com.cleanroommc.retrosophisticatedbackpacks.sync.DelegatedCraftingStackHandlerSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.DelegatedStackHandlerSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.FilterSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.FoodFilterSlotSH

class UpgradeSlotUpdateGroup(
    private val panel: BackpackPanel,
    private val wrapper: BackpackWrapper,
    private val slotIndex: Int
) {
    // Common Filters
    var commonFilterStackHandler = DelegatedStackHandlerSH(wrapper, slotIndex, 9)
    val commonFilterSlots: Array<ModularSlot>

    var advancedCommonFilterStackHandler = DelegatedStackHandlerSH(wrapper, slotIndex, 16)
    val advancedCommonFilterSlots: Array<ModularSlot>

    // Feeding filters
    val feedingFilterSlots: Array<ModularSlot>
    val advancedFeedingFilterSlots: Array<ModularSlot>

    // Crafting slots
    var craftingStackHandler = DelegatedCraftingStackHandlerSH(panel::getBackpackContainer, wrapper, slotIndex, 10)
    val craftingMatrixSlots: Array<ModularSlot>
    val craftingOutputSlot: ModularCraftingSlot

    val craftingInfo: CraftingSlotInfo

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

        syncManager.syncValue("crafting_delegation_$slotIndex", craftingStackHandler)
        craftingMatrixSlots = Array(9) {
            val slot = ModularSlot(craftingStackHandler.delegatedStackHandler, it)
            slot.slotGroup("crafting_matrix_$slotIndex")

            syncManager.syncValue(
                "crafting_matrix_$slotIndex",
                it,
                ItemSlotSH(slot)
            )

            slot
        }
        syncManager.registerSlotGroup(SlotGroup("crafting_matrix_$slotIndex", 3, true))



        craftingOutputSlot = {
            val slot = IndexedModularCraftingSlot(slotIndex, craftingStackHandler.delegatedStackHandler, 9)
            slot.slotGroup("crafting_result_$slotIndex")

            syncManager.syncValue(
                "crafting_result_$slotIndex",
                0,
                ItemSlotSH(slot)
            )

            slot
        }.invoke()
        craftingInfo = CraftingSlotInfo(craftingMatrixSlots, craftingOutputSlot)

        syncManager.registerSlotGroup(SlotGroup("crafting_result_$slotIndex", 1, false))

    }

    fun updateFilterDelegate(wrapper: IBasicFilterable) {
        commonFilterStackHandler.setDelegatedStackHandler(wrapper::filterItems)
        commonFilterStackHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE)
    }

    fun updateAdvancedFilterDelegate(wrapper: IAdvancedFilterable) {
        advancedCommonFilterStackHandler.setDelegatedStackHandler(wrapper::filterItems)
        advancedCommonFilterStackHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_FILTERABLE)
    }
    fun updateCraftingDelegate(wrapper: CraftingUpgradeWrapper){
        craftingStackHandler.setDelegatedStackHandler(wrapper::craftMatrix)
        craftingStackHandler.syncToServer(DelegatedStackHandlerSH.UPDATE_CRAFTING)
    }
}