package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.DelegatedItemHandler
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler

/**
 * Used to synchronize slot's delegated stack handler to server side, this is useful when the upgrade contains
 * item handler, and it's supposed to be able to dynamically introduced in container, which later could bind to certain
 * slots.
 */
class DelegatedStackHandlerSH(private val wrapper: BackpackWrapper, private val slotIndex: Int) : SyncHandler() {
    companion object {
        const val UPDATE_PICKUP_FILTER = 0
        const val UPDATE_ADVANCED_PICKUP_FILTER = 1
    }

    var delegatedStackHandler: DelegatedItemHandler = DelegatedItemHandler(EmptyHandler::INSTANCE)

    fun setDelegatedStackHandler(delegated: () -> IItemHandler) {
        delegatedStackHandler.delegated = delegated
    }

    override fun readOnClient(id: Int, buf: PacketBuffer) {
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        val stack = wrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)

        when (id) {
            UPDATE_PICKUP_FILTER -> {
                val wrapper = stack.getCapability(Capabilities.PICKUP_UPGRADE_CAPABILITY, null) ?: return

                setDelegatedStackHandler(wrapper::filterItems)
            }
            UPDATE_ADVANCED_PICKUP_FILTER -> {
                val wrapper = stack.getCapability(Capabilities.ADVANCED_PICKUP_UPGRADE_CAPABILITY, null) ?: return

                setDelegatedStackHandler(wrapper::filterItems)
            }
        }
    }
}