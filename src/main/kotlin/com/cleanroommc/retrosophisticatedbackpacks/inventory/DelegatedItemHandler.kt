package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.wrapper.EmptyHandler

class DelegatedItemHandler(var delegated: () -> IItemHandler, var wrappedSlotAmount: Int) : IItemHandlerModifiable {


    override fun getSlots(): Int {
        val delegated = delegated()

        if (delegated != EmptyHandler.INSTANCE)
            check(delegated.slots == wrappedSlotAmount) {
                "Mismatched delegated item handler slot amount: assumed to have $wrappedSlotAmount but actually got ${delegated().slots}"
            }

        return wrappedSlotAmount
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