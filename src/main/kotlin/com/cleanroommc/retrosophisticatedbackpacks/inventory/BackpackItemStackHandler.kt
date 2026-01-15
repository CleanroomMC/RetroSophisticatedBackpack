package com.cleanroommc.retrosophisticatedbackpacks.inventory

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IVoidUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
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
        if (Config.blacklistedItems.contains(stack.item.registryName?.toString())) false
        else if (memorizedSlotStack[slot].isEmpty) stack.item !is BackpackItem || wrapper.canNestBackpack()
        else if (memorizedSlotRespectNbtList[slot]) ItemStack.areItemStacksEqual(stack, memorizedSlotStack[slot])
        else stack.isItemEqualIgnoreDurability(memorizedSlotStack[slot])

    override fun getStackLimit(slotIndex: Int, stack: ItemStack): Int =
        stacks[slotIndex].maxStackSize * wrapper.getTotalStackMultiplier()

    private fun insertItemToMemorySlots(stack: ItemStack, simulate: Boolean): ItemStack {
        val shouldVoidIfOverflow = wrapper.canVoid(
            stack,
            IVoidUpgrade.TransferSource.UPGRADE_OR_WORLD_INTERACTION,
            IVoidUpgrade.VoidType.OVERFLOW
        )
        var stack = stack

        if (stack.isEmpty)
            return ItemStack.EMPTY

        for ((slotIndex, memorizedStack) in memorizedSlotStack.withIndex()) {
            if (memorizedStack.isEmpty || !ItemStack.areItemsEqual(stack, memorizedStack))
                continue

            val beforeSize = stack.count
            stack = insertItem(slotIndex, stack, simulate)

            if (shouldVoidIfOverflow && stack.count < beforeSize)
                return ItemStack.EMPTY

            if (stack.isEmpty)
                return stack
        }

        return stack
    }

    private fun insertItemRespectOverflowVoiding(stack: ItemStack, simulate: Boolean): ItemStack {
        if (!wrapper.canVoid(
                stack,
                IVoidUpgrade.TransferSource.UPGRADE_OR_WORLD_INTERACTION,
                IVoidUpgrade.VoidType.OVERFLOW
            )
        )
            return stack

        var firstEmptySlotIndex: Int? = null

        if (stack.isEmpty)
            return ItemStack.EMPTY

        for (slotIndex in 0..<slots) {
            val slotStack = getStackInSlot(slotIndex)

            if (ItemHandlerHelper.canItemStacksStack(slotStack, stack)) {
                if (slotStack.count < getStackLimit(slotIndex, slotStack))
                    super.insertItem(slotIndex, stack, simulate)
                return ItemStack.EMPTY
            } else if (slotStack.isEmpty && firstEmptySlotIndex == null)
                firstEmptySlotIndex = slotIndex
        }

        if (firstEmptySlotIndex != null)
            return super.insertItem(firstEmptySlotIndex, stack, simulate)

        return ItemStack.EMPTY
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (stack.isEmpty)
            return ItemStack.EMPTY

        if (wrapper.canVoid(stack, IVoidUpgrade.TransferSource.UPGRADE_OR_WORLD_INTERACTION, IVoidUpgrade.VoidType.ANY))
            return ItemStack.EMPTY

        var stack = insertItemToMemorySlots(stack, simulate)
        stack = insertItemRespectOverflowVoiding(stack, simulate)
        return super.insertItem(slot, stack, simulate)
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
