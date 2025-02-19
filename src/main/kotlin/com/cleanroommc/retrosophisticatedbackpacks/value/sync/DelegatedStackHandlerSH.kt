package com.cleanroommc.retrosophisticatedbackpacks.value.sync

import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.items.DelegatedItemHandler
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.wrapper.EmptyHandler

/**
 * Used to synchronize slot's delegated stack handler to server side, this is useful when the upgrade contains
 * item handler, and it's supposed to be able to dynamically introduced in container, which later could bind to certain
 * slots.
 */
class DelegatedStackHandlerSH(private val wrapper: BackpackWrapper) : SyncHandler() {
    var delegatedStackHandler: DelegatedItemHandler = DelegatedItemHandler(EmptyHandler::INSTANCE)

    fun syncToSlot(slotIndex: Int) {
        syncToServer(0) {
            it.writeInt(slotIndex)
        }
    }

    override fun readOnClient(id: Int, buf: PacketBuffer) {

    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        val slotIndex = buf.readInt()
        val wrapper = wrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
            .getCapability(Capabilities.PICKUP_UPGRADE_CAPABILITY, null) ?: return

        delegatedStackHandler.delegated = wrapper::filterItems
    }
}