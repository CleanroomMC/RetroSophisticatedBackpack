package com.cleanroommc.retrosophisticatedbackpacks.inventory

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.items.ItemHandlerHelper
import kotlin.math.min

class BackpackItemStackHandler(size: Int, private val wrapper: BackpackWrapper) : ExposedItemStackHandler(size) {
    val memorizedSlotStack: NonNullList<ItemStack> = NonNullList.withSize(size, ItemStack.EMPTY)
    val memorizedSlotRespectNbtList: MutableList<Boolean> = MutableList(size) { false }
    val sortLockedSlots: MutableList<Boolean> = MutableList(size) { false }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
        if (memorizedSlotStack[slot].isEmpty) stack.item !is BackpackItem || wrapper.canNestBackpack()
        else if (memorizedSlotRespectNbtList[slot]) ItemStack.areItemStacksEqual(stack, memorizedSlotStack[slot])
        else stack.isItemEqualIgnoreDurability(memorizedSlotStack[slot])

    override fun getStackLimit(slotIndex: Int, stack: ItemStack): Int =
        stacks[slotIndex].maxStackSize * wrapper.getTotalStackMultiplier()

    /**
     * Prioritize insertion by tries inserting on memorized slot first.
     *
     * Only used by backpack tile entity for other block's insertion interaction, to prevent
     * gui-based interaction get unexpected insertion result.
     */
    fun prioritizedInsertion(slotIndex: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        var stack = insertItemToMemorySlots(stack, simulate)
        return insertItem(slotIndex, stack, simulate)
    }

    fun insertItemToMemorySlots(stack: ItemStack, simulate: Boolean): ItemStack {
        var stack = stack

        for ((slotIndex, memorizedStack) in memorizedSlotStack.withIndex()) {
            if (memorizedStack.isEmpty || !ItemStack.areItemsEqual(stack, memorizedStack))
                continue

            stack = insertItem(slotIndex, stack, simulate)

            if (stack.isEmpty)
                return stack
        }

        return stack
    }

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