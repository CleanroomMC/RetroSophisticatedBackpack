package com.cleanroommc.retrosophisticatedbackpacks.value.sync

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.backpack.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper
import net.minecraft.network.PacketBuffer

/**
 * Used to synchronize upgrade item's capability, this is only fired from client to reflect client's action to server
 * side.
 */
class UpgradeSlotSH(slot: ModularSlot) : ItemSlotSH(slot) {
    companion object {
        const val UPDATE_UPGRADE_TOGGLE = 6
        const val UPDATE_PICKUP_UPGRADE_PICKUP_TYPE = 7
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        super.readOnServer(id, buf)

        when (id) {
            UPDATE_UPGRADE_TOGGLE -> updateToggleable(buf)
            UPDATE_PICKUP_UPGRADE_PICKUP_TYPE -> updatePickupUpgrade(buf)
        }
    }

    private fun updateToggleable(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.TOGGLEABLE_CAPABILITY, null) ?: return
        wrapper.toggle()
    }

    private fun updatePickupUpgrade(buf: PacketBuffer) {
        val wrapper = slot.stack.getCapability(Capabilities.PICKUP_UPGRADE_CAPABILITY, null) ?: return

        wrapper.filterType = buf.readEnumValue(PickupUpgradeWrapper.FilterType::class.java)
    }
}