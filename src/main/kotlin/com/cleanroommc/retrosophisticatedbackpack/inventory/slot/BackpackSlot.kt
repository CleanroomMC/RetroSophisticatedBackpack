package com.cleanroommc.retrosophisticatedbackpack.inventory.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackItemStackHandler
import net.minecraft.item.ItemStack

class BackpackSlot(
    private val stackMultiplier: () -> Int,
    itemHandler: BackpackItemStackHandler,
    private val index: Int
) :
    ModularSlot(itemHandler, index) {

    override fun getStack(): ItemStack {
        return super.getStack()
    }

    override fun putStack(stack: ItemStack) {
        super.putStack(stack)
    }

    override fun getItemStackLimit(stack: ItemStack): Int {
        return stack.maxStackSize * stackMultiplier()
    }

    override fun getSlotStackLimit(): Int {
        return 128
    }
}