package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.network.PacketBuffer

class BackpackSlotSH(private val wrapper: BackpackWrapper, slot: ModularSlot) : ItemSlotSH(slot) {
    companion object {
        const val UPDATE_SET_MEMORY_STACK = 6
        const val UPDATE_UNSET_MEMORY_STACK = 7
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        when (id) {
            UPDATE_SET_MEMORY_STACK -> wrapper.setMemoryStack(slot.slotIndex)
            UPDATE_UNSET_MEMORY_STACK -> wrapper.unsetMemoryStack(slot.slotIndex)
            else -> super.readOnServer(id, buf)
        }
    }
}