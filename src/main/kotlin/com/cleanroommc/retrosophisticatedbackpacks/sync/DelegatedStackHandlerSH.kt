package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.screen.ModularContainer
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widgets.slot.InventoryCraftingWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.inventory.DelegatedItemHandler
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.IItemHandlerModifiable
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
        const val UPDATE_CRAFTING = 1
    }

    var delegatedStackHandler: DelegatedItemHandler = DelegatedItemHandler(EmptyHandler::INSTANCE, wrappedSlotAmount)

    open fun setDelegatedStackHandler(delegated: () -> IItemHandler) {
        delegatedStackHandler.delegated = delegated
    }

    protected fun getBackpackContainer(): BackpackContainer {
        check(isValid) {
            "Sync handler $this isn't ready to provide a container yet!"
        }
        val cont: ModularContainer = getSyncManager().container
        check(cont is BackpackContainer) {
            "Container $cont tied to Backpack is not a BackpackContainer!"
        }
        return cont
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
        }
    }
}