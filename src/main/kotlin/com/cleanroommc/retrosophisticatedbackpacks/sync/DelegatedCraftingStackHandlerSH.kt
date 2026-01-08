package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.IndexedInventoryCraftingWrapper
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler

class DelegatedCraftingStackHandlerSH(
    private val containerProvider: () -> BackpackContainer,
    private val wrapper: BackpackWrapper,
    private val slotIndex: Int,
    wrappedSlotAmount: Int
) : DelegatedStackHandlerSH(wrapper, slotIndex, wrappedSlotAmount) {
    companion object {
        const val UPDATE_CRAFTING = 1
    }

    /**
     * Wrapper around this sync handler's delegated inventory, necessary for crafting interactions.
     * Delayed initialization due to the need for a Container.
     */
    private var inventoryCrafting: IndexedInventoryCraftingWrapper? = null

    override fun setDelegatedStackHandler(delegated: () -> IItemHandler) {
        super.setDelegatedStackHandler(delegated)
        updateInventoryCrafting()
    }

    fun updateInventoryCrafting() {
        if (null == inventoryCrafting) {
            inventoryCrafting =
                IndexedInventoryCraftingWrapper(slotIndex, containerProvider(), 3, 3, delegatedStackHandler, 0)
            // We need to link InventoryCraftingWrapper and ModularCraftingSlot instances.
            // The container has methods custom built to do this, regardless of insertion order!
            containerProvider().registerInventoryCrafting(slotIndex, inventoryCrafting!!)
        }
        // Sync the wrapper's set crafting destination
        val stack = wrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
        val wrapper: CraftingUpgradeWrapper? = stack.getCapability(Capabilities.CRAFTING_ITEM_HANDLER_CAPABILITY, null)
        wrapper?.let {
            inventoryCrafting!!.craftingDestination = wrapper.craftingDestination
        }

        // Notify the container to get the recipe's output
        inventoryCrafting!!.detectChanges()
        // Now, send the newly calculated output to client
        // Don't do anything if the connected inventory is a placeholder!
        if (isValid && !syncManager.isClient && delegatedStackHandler.delegated() !is EmptyHandler) {
            val result = delegatedStackHandler.getStackInSlot(9)
            syncToClient(UPDATE_CRAFTING) { buffer: PacketBuffer -> buffer.writeItemStack(result) }
        }
    }

    override fun readOnClient(id: Int, buf: PacketBuffer) {
        val stack = wrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
        when (id) {
            UPDATE_CRAFTING -> {
                val wrapper = stack.getCapability(Capabilities.CRAFTING_ITEM_HANDLER_CAPABILITY, null) ?: return
                wrapper.craftMatrix.setStackInSlot(9, buf.readItemStack())
            }
        }

    }

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        val stack = wrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)

        when (id) {
            UPDATE_FILTERABLE -> {
                val wrapper = stack.getCapability(Capabilities.BASIC_FILTERABLE_CAPABILITY, null) ?: return

                setDelegatedStackHandler(wrapper::filterItems)
            }

            UPDATE_CRAFTING -> {
                val wrapper = stack.getCapability(Capabilities.CRAFTING_ITEM_HANDLER_CAPABILITY, null) ?: return

                setDelegatedStackHandler(wrapper::craftMatrix)
            }
        }
    }
}