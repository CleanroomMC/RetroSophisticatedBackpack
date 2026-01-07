package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper.CraftingDestination
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade
import net.minecraft.network.PacketBuffer

/**
 * Used to synchronize upgrade item's capability, this is only fired from client to reflect client's action to server
 * side.
 */
class UpgradeSlotSH(slot: ModularSlot) : ItemSlotSH(slot) {
    companion object {
        const val UPDATE_UPGRADE_TAB_STATE = 6
        const val UPDATE_UPGRADE_TOGGLE = 7
        const val UPDATE_BASIC_FILTERABLE = 8
        const val UPDATE_ADVANCED_FILTERABLE = 9
        const val UPDATE_ADVANCED_FEEDING = 10
        const val UPDATE_FILTER_WAY = 11
        const val UPDATE_CRAFTING_SHIFT = 12
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        super.readOnServer(id, buf)

        when (id) {
            UPDATE_UPGRADE_TAB_STATE -> updateTabState(buf)
            UPDATE_UPGRADE_TOGGLE -> updateToggleable()
            UPDATE_BASIC_FILTERABLE -> updateBasicFilterable(buf)
            UPDATE_ADVANCED_FILTERABLE -> updateAdvancedFilterable(buf)
            UPDATE_ADVANCED_FEEDING -> updateAdvanceFeedingUpgrade(buf)
            UPDATE_FILTER_WAY -> updateFilterUpgrade(buf)
            UPDATE_CRAFTING_SHIFT -> updateCraftingUpgradeShift(buf)
        }
    }

    private fun updateTabState(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: return
        wrapper.isTabOpened = buf.readBoolean()
    }

    private fun updateToggleable() {
        val wrapper = slot.stack.getCapability(Capabilities.TOGGLEABLE_CAPABILITY, null) ?: return
        wrapper.toggle()
    }

    private fun updateBasicFilterable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.BASIC_FILTERABLE_CAPABILITY, null) ?: return

        wrapper.filterType = buf.readEnumValue(IBasicFilterable.FilterType::class.java)
    }

    private fun updateAdvancedFilterable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.ADVANCED_FILTERABLE_CAPABILITY, null) ?: return

        wrapper.filterType = buf.readEnumValue(IBasicFilterable.FilterType::class.java)
        wrapper.matchType = buf.readEnumValue(IAdvancedFilterable.MatchType::class.java)
        wrapper.ignoreDurability = buf.readBoolean()
        wrapper.ignoreNBT = buf.readBoolean()

        val size = buf.readInt()

        wrapper.oreDictEntries.clear()

        for (i in 0 until size) {
            wrapper.oreDictEntries.add(buf.readString(100))
        }
    }

    private fun updateAdvanceFeedingUpgrade(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.ADVANCED_FEEDING_UPGRADE_CAPABILITY, null) ?: return

        wrapper.hungerFeedingStrategy =
            buf.readEnumValue(AdvancedFeedingUpgradeWrapper.FeedingStrategy.Hunger::class.java)
        wrapper.healthFeedingStrategy =
            buf.readEnumValue(AdvancedFeedingUpgradeWrapper.FeedingStrategy.HEALTH::class.java)
    }

    private fun updateFilterUpgrade(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.IFILTER_UPGRADE_CAPABILITY, null) ?: return

        wrapper.filterWay = buf.readEnumValue(IFilterUpgrade.FilterWayType::class.java)
    }

    private fun updateCraftingUpgradeShift(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.CRAFTING_ITEM_HANDLER_CAPABILITY, null) ?: return

        wrapper.craftingDestination = buf.readEnumValue(CraftingDestination::class.java)
    }
}