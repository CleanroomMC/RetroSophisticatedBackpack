package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class BackpackItemStackHandler(size: Int, private val stackMultiplier: () -> Int) : ExposedItemStackHandler(size) {
    override fun getStackLimit(slotIndex: Int, stack: ItemStack): Int {
        val stack = this.stacks[slotIndex]
        return stack.maxStackSize * stackMultiplier()
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (stack.isEmpty) return ItemStack.EMPTY

        validateSlotIndex(slot)

        val existing = this.stacks[slot]
        var limit = getStackLimit(slot, stack)

        if (!existing.isEmpty) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) return stack

            limit -= existing.count
        }

        if (limit <= 0) return stack

        val reachedLimit = stack.count > limit

        if (!simulate) {
            if (existing.isEmpty) {
                this.stacks[slot] = if (reachedLimit) ItemHandlerHelper.copyStackWithSize(stack, limit) else stack
            } else {
                existing.grow(if (reachedLimit) limit else stack.count)
            }
            onContentsChanged(slot)
        }

        return if (reachedLimit) ItemHandlerHelper.copyStackWithSize(
            stack,
            stack.count - limit
        ) else ItemStack.EMPTY
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