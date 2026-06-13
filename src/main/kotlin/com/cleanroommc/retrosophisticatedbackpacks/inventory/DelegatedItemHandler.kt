package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.IItemHandlerModifiable

class DelegatedItemHandler(var delegated: () -> IItemHandler, var wrappedSlotAmount: Int) : IItemHandlerModifiable {


    override fun getSlots(): Int = wrappedSlotAmount

    override fun getStackInSlot(slot: Int): ItemStack =
        delegated().let { if (slot in 0 until it.slots) it.getStackInSlot(slot) else ItemStack.EMPTY }

    override fun insertItem(
        slot: Int,
        stack: ItemStack,
        simulate: Boolean
    ): ItemStack =
        delegated().let { if (slot in 0 until it.slots) it.insertItem(slot, stack, simulate) else stack }

    override fun extractItem(
        slot: Int,
        amount: Int,
        simulate: Boolean
    ): ItemStack =
        delegated().let { if (slot in 0 until it.slots) it.extractItem(slot, amount, simulate) else ItemStack.EMPTY }

    override fun getSlotLimit(slot: Int): Int =
        delegated().let { if (slot in 0 until it.slots) it.getSlotLimit(slot) else 0 }

    override fun setStackInSlot(slot: Int, stack: ItemStack) {
        val delegated = delegated()

        if (delegated is IItemHandlerModifiable && slot in 0 until delegated.slots)
            delegated.setStackInSlot(slot, stack)
    }
}
