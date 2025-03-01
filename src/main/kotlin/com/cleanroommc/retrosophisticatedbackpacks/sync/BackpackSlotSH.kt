package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.network.PacketBuffer

class BackpackSlotSH(private val wrapper: BackpackWrapper, slot: ModularSlot) : ItemSlotSH(slot) {
    companion object {
        const val UPDATE_SET_MEMORY_STACK = 6
        const val UPDATE_UNSET_MEMORY_STACK = 7
        const val UPDATE_SET_SLOT_LOCK = 8
        const val UPDATE_UNSET_SLOT_LOCK = 9
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        when (id) {
            UPDATE_SET_MEMORY_STACK -> {
                val respectNBT = buf.readBoolean()

                wrapper.setMemoryStack(slot.slotIndex, respectNBT)
            }

            UPDATE_UNSET_MEMORY_STACK -> wrapper.unsetMemoryStack(slot.slotIndex)
            UPDATE_SET_SLOT_LOCK -> wrapper.setSlotLocked(slot.slotIndex, true)
            UPDATE_UNSET_SLOT_LOCK -> wrapper.setSlotLocked(slot.slotIndex, false)
            else -> super.readOnServer(id, buf)
        }
    }
}