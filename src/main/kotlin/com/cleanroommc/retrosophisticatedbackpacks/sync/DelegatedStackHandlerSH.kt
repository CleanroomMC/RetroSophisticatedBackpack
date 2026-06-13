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
open class DelegatedStackHandlerSH(
    private val wrapper: BackpackWrapper,
    private val slotIndex: Int,
    private val wrappedSlotAmount: Int
) : SyncHandler() {
    companion object {
        const val UPDATE_FILTERABLE = 0
        const val UPDATE_JUKEBOX = 1
        const val UPDATE_TANK = 2
        const val UPDATE_BATTERY = 3
        const val UPDATE_ANVIL = 4
    }

    var delegatedStackHandler: DelegatedItemHandler = DelegatedItemHandler(EmptyHandler::INSTANCE, wrappedSlotAmount)

    open fun setDelegatedStackHandler(delegated: () -> IItemHandler) {
        delegatedStackHandler.delegated = delegated
    }

    override fun readOnClient(id: Int, buf: PacketBuffer) {
    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        val stack = wrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)

        when (id) {
            UPDATE_FILTERABLE -> {
                val wrapper = stack.getCapability(Capabilities.BASIC_FILTERABLE_CAPABILITY, null) ?: return

                setDelegatedStackHandler(wrapper::filterItems)
            }

            UPDATE_JUKEBOX -> {
                val wrapper = stack.getCapability(Capabilities.IJUKEBOX_UPGRADE_CAPABILITY, null) ?: return

                setDelegatedStackHandler(wrapper::discInventory)
            }

            UPDATE_TANK -> {
                val wrapper = stack.getCapability(Capabilities.ITANK_UPGRADE_CAPABILITY, null) ?: return

                setDelegatedStackHandler(wrapper::getInventory)
            }

            UPDATE_BATTERY -> {
                val wrapper = stack.getCapability(Capabilities.IBATTERY_UPGRADE_CAPABILITY, null) ?: return
                setDelegatedStackHandler(wrapper::getInventory)
            }

            UPDATE_ANVIL -> {
                val wrapper = stack.getCapability(Capabilities.IANVIL_UPGRADE_CAPABILITY, null) ?: return
                setDelegatedStackHandler(wrapper::getInventory)
            }
        }
    }
}
