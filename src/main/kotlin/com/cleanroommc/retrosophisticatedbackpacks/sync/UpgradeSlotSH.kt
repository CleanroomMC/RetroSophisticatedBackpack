package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvanceFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import net.minecraft.network.PacketBuffer

/**
 * Used to synchronize upgrade item's capability, this is only fired from client to reflect client's action to server
 * side.
 */
class UpgradeSlotSH(slot: ModularSlot) : ItemSlotSH(slot) {
    companion object {
        const val UPDATE_UPGRADE_TOGGLE = 6
        const val UPDATE_BASIC_FILTERABLE = 7
        const val UPDATE_ADVANCED_FILTERABLE = 8
        const val UPDATE_ADVANCED_FEEDING = 9
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        super.readOnServer(id, buf)

        when (id) {
            UPDATE_UPGRADE_TOGGLE -> updateToggleable(buf)
            UPDATE_BASIC_FILTERABLE -> updateBasicFilterable(buf)
            UPDATE_ADVANCED_FILTERABLE -> updateAdvancedFilterable(buf)
            UPDATE_ADVANCED_FEEDING -> updateAdvanceFeedingUpgrade(buf)
        }
    }

    private fun updateToggleable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.TOGGLEABLE_CAPABILITY, null) ?: return
        wrapper.toggle()
    }

    private fun updateBasicFilterable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.BASIC_FILTERABLE_CAPABILITY, null) ?: return

        wrapper.filterType = buf.readEnumValue(IBasicFilterable.FilterType::class.java)
    }

    private fun updateAdvancedFilterable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.ADVANCE_FILTERABLE_CAPABILITY, null) ?: return

        wrapper.filterType = buf.readEnumValue(IBasicFilterable.FilterType::class.java)
        wrapper.matchType = buf.readEnumValue(IAdvanceFilterable.MatchType::class.java)
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
}