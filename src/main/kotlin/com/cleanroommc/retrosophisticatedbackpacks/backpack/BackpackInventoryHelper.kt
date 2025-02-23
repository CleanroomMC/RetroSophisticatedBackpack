package com.cleanroommc.retrosophisticatedbackpacks.backpack

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.entity.Entity
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.ISidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.wrapper.InvWrapper
import net.minecraftforge.items.wrapper.SidedInvWrapper

object BackpackInventoryHelper {
    fun attemptDepositOnTileEntity(wrapper: BackpackWrapper, destination: TileEntity, facing: EnumFacing): Boolean {
        val destination = getHandler(destination, facing) ?: return false
        return attemptDepositOnItemHandler(wrapper, destination)
    }

    fun attemptDepositOnEntity(wrapper: BackpackWrapper, destination: Entity): Boolean {
        val destination = getHandler(destination, null) ?: return false
        return attemptDepositOnItemHandler(wrapper, destination)
    }

    fun attemptDepositOnItemHandler(wrapper: BackpackWrapper, destination: IItemHandler): Boolean {
        val backpackInventory = wrapper.backpackItemStackHandler
        var transferred = false

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
                    wrapper.extractItem(i, stack.count - copiedStack.count, false)
                }
            }
        }

        return transferred
    }

    fun attemptRestockFromTileEntity(wrapper: BackpackWrapper, source: TileEntity, facing: EnumFacing): Boolean {
        val source = getHandler(source, facing) ?: return false
        return attemptRestockFromItemHandler(wrapper, source)
    }

    fun attemptRestockFromEntity(wrapper: BackpackWrapper, source: Entity): Boolean {
        val source = getHandler(source, null) ?: return false
        return attemptRestockFromItemHandler(wrapper, source)
    }

    fun attemptRestockFromItemHandler(wrapper: BackpackWrapper, source: IItemHandler): Boolean {
        val backpackInventory = wrapper.backpackItemStackHandler
        var transferred = false

        if (source !is IItemHandlerModifiable)
            return false

        if (isFull(backpackInventory))
            return false

        for (i in 0 until source.slots) {
            var sourceStack = source.getStackInSlot(i)

            if (sourceStack.isEmpty)
                continue

            var copiedSourceStack = sourceStack.copy()

            if (wrapper.canRestock(copiedSourceStack)) {
                copiedSourceStack = ItemHandlerHelper.insertItemStacked(backpackInventory, copiedSourceStack, false)

                if (!ItemStack.areItemStacksEqual(sourceStack, copiedSourceStack)) {
                    transferred = true
                    source.setStackInSlot(i, copiedSourceStack)
                }
            }
        }

        return transferred
    }

    private fun getHandler(handler: Any, facing: EnumFacing?): IItemHandler? =
        if (handler is ISidedInventory) SidedInvWrapper(handler, facing)
        else if (handler is IInventory) InvWrapper(handler)
        else handler as? IItemHandler

    private fun isFull(handler: IItemHandler): Boolean {
        for (i in 0 until handler.slots) {
            val stack = handler.getStackInSlot(i)

            if (stack.isEmpty || stack.count != handler.getSlotLimit(i)) {
                return false
            }
        }

        return true
    }
}