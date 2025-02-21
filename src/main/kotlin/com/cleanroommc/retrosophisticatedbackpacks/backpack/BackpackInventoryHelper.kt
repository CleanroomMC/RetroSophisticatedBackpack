package com.cleanroommc.retrosophisticatedbackpacks.backpack

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.ISidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.wrapper.InvWrapper
import net.minecraftforge.items.wrapper.SidedInvWrapper

object BackpackInventoryHelper {
    fun attemptDepositOnTileEntity(wrapper: BackpackWrapper, destination: TileEntity, facing: EnumFacing): Boolean {
        val backpackInventory = wrapper.backpackItemStackHandler
        var transferred = false
        val destination = if (destination is ISidedInventory) SidedInvWrapper(destination, facing)
        else if (destination is IInventory) InvWrapper(destination)
        else if (destination is IItemHandler) destination
        else return false

        if (isFull(destination))
            return false

        for (i in 0 until backpackInventory.slots) {
            if (wrapper.canDeposit(i)) {
                val stack = wrapper.getStackInSlot(i)

                if (stack.isEmpty)
                    continue

                var copiedStack = stack.copy()
                copiedStack = ItemHandlerHelper.insertItemStacked(destination, copiedStack, false)

                if (!ItemStack.areItemStacksEqual(stack, copiedStack)) {
                    transferred = true
                    wrapper.setInventorySlotContents(i, copiedStack)
                }
            }
        }

        return transferred
    }

    private fun isFull(handler: IItemHandler): Boolean {
        for (i in 0 until handler.slots) {
            val stack = handler.getStackInSlot(i)

            if (stack.isEmpty || stack.count != handler.getSlotLimit(i)) {
                return false
            }
        }

        return true
    }

    private fun canInsertStackInSlot(destination: IItemHandler, stack: ItemStack, slotIndex: Int): Boolean =
        destination.isItemValid(slotIndex, stack)

    private fun canCombine(stack1: ItemStack, stack2: ItemStack): Boolean {
        return if (stack1.item !== stack2.item) false
        else if (stack1.metadata != stack2.metadata) false
        else if (stack1.count > stack1.maxStackSize) false
        else ItemStack.areItemStackTagsEqual(stack1, stack2)
    }
}