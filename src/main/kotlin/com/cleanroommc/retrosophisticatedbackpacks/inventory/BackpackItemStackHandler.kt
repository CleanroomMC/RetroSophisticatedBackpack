package com.cleanroommc.retrosophisticatedbackpacks.inventory

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class BackpackItemStackHandler(size: Int, private val wrapper: BackpackWrapper) : ExposedItemStackHandler(size) {
    override fun getStackLimit(slotIndex: Int, stack: ItemStack): Int =
        stacks[slotIndex].maxStackSize * wrapper.getTotalStackMultiplier()

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (stack.isEmpty)
            return ItemStack.EMPTY

        validateSlotIndex(slot)

        val existing = stacks[slot]
        var limit = getStackLimit(slot, stack)

        if (!existing.isEmpty) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack

            limit -= existing.count
        }

        if (limit <= 0) return stack

        val reachedLimit = stack.count > limit

        if (!simulate) {
            if (existing.isEmpty) {
                stacks[slot] =
                    if (reachedLimit) ItemHandlerHelper.copyStackWithSize(stack, limit)
                    else stack
            } else {
                existing.grow(if (reachedLimit) limit else stack.count)
            }

            onContentsChanged(slot)
        }

        return if (reachedLimit) ItemHandlerHelper.copyStackWithSize(stack, stack.count - limit)
        else ItemStack.EMPTY
    }

    override fun extractItem(slotIndex: Int, amount: Int, simulate: Boolean): ItemStack {
        if (amount == 0)
            return ItemStack.EMPTY

        validateSlotIndex(slotIndex)

        val stack = stacks[slotIndex]

        if (stack.isEmpty)
            return ItemStack.EMPTY

        val slotMaxStackSize = stack.maxStackSize * wrapper.getTotalStackMultiplier()
        val toExtract = min(amount, slotMaxStackSize)

        if (stack.count <= toExtract) {
            if (!simulate) {
                stacks[slotIndex] = ItemStack.EMPTY
                onContentsChanged(slotIndex)
            }

            return stack
        } else {
            if (!simulate) {
                stacks[slotIndex] = ItemHandlerHelper.copyStackWithSize(stack, stack.count - toExtract)
                onContentsChanged(slotIndex)
            }

            return ItemHandlerHelper.copyStackWithSize(stack, toExtract)
        }
    }
}