package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.IItemHandlerModifiable

class DelegatedItemHandler(var delegated: () -> IItemHandler, var wrappedSlotAmount: Int) : IItemHandlerModifiable {

    /**
     * Temporary flag to bypass ModularSlot's slot count requirement during construction, which would normally raise an error.
     * Only enabled while ModularSlot instances are being created.
     * @see com.cleanroommc.retrosophisticatedbackpacks.client.gui.UpgradeSlotUpdateGroup
     */
    var bypassSizeCheck = false


    override fun getSlots(): Int{
        val current = delegated().slots
        if(current != 0 && bypassSizeCheck){
            return wrappedSlotAmount
        }
        return current;
    }

    override fun getStackInSlot(slot: Int): ItemStack =
        delegated().getStackInSlot(slot)

    override fun insertItem(
        slot: Int,
        stack: ItemStack,
        simulate: Boolean
    ): ItemStack =
        delegated().insertItem(slot, stack, simulate)

    override fun extractItem(
        slot: Int,
        amount: Int,
        simulate: Boolean
    ): ItemStack =
        delegated().extractItem(slot, amount, simulate)

    override fun getSlotLimit(slot: Int): Int =
        delegated().getSlotLimit(slot)

    override fun setStackInSlot(slot: Int, stack: ItemStack) {
        val delegated = delegated()

        if (delegated is IItemHandlerModifiable)
            delegated.setStackInSlot(slot, stack)
    }
}