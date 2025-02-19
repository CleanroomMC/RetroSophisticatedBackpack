package com.cleanroommc.retrosophisticatedbackpacks.backpack

import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class BackpackItemStackHandler(size: Int, private val stackMultiplier: () -> Int) : ExposedItemStackHandler(size) {
    override fun getStackLimit(slotIndex: Int, stack: ItemStack): Int {
        val stack = this.stacks[slotIndex]
        return stack.maxStackSize * stackMultiplier()
    }

    override fun extractItem(slotIndex: Int, amount: Int, simulate: Boolean): ItemStack {
        if (amount == 0) return ItemStack.EMPTY

        validateSlotIndex(slotIndex)

        val stack = this.stacks[slotIndex]

        if (stack.isEmpty) return ItemStack.EMPTY

        val slotMaxStackSize = stack.maxStackSize * stackMultiplier()
        val toExtract = min(amount, slotMaxStackSize)

        if (stack.count <= toExtract) {
            if (!simulate) {
                this.stacks[slotIndex] = ItemStack.EMPTY
                onContentsChanged(slotIndex)
            }
            return stack
        } else {
            if (!simulate) {
                this.stacks[slotIndex] = ItemHandlerHelper.copyStackWithSize(stack, stack.count - toExtract)
                onContentsChanged(slotIndex)
            }

            return ItemHandlerHelper.copyStackWithSize(stack, toExtract)
        }
    }
}