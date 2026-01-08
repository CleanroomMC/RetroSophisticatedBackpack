package com.cleanroommc.retrosophisticatedbackpacks.backpack

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
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
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper
import net.minecraftforge.items.wrapper.SidedInvWrapper
import net.minecraftforge.oredict.OreDictionary
import kotlin.math.min

object BackpackInventoryHelper {
    fun sortInventory(wrapper: BackpackWrapper) {
        fun compareLists(list1: List<String>, list2: List<String>): Int {
            for (i in 0 until min(list1.size, list2.size)) {
                val item1 = list1[i]
                val item2 = list2[i]
                val comparedValue = item1.compareTo(item2)

                if (comparedValue != 0)
                    return comparedValue
            }

            return list1.size.compareTo(list2.size)
        }

        // Merges all slots first
        for (i in 0 until wrapper.backpackInventorySize() - 1) {
            if (wrapper.isSlotLocked(i))
                continue

            var isMemorizedSlot = wrapper.isSlotMemorized(i)
            val baseStack = wrapper.getStackInSlot(i)
            val maxSize = baseStack.maxStackSize * wrapper.getTotalStackMultiplier()

            for (j in i + 1 until wrapper.backpackInventorySize()) {
                if (isMemorizedSlot != wrapper.isSlotMemorized(j) || wrapper.isSlotLocked(j))
                    continue

                val stack = wrapper.getStackInSlot(j)

                if (!ItemHandlerHelper.canItemStacksStack(baseStack, stack))
                    continue

                val diff = min(stack.count, maxSize - baseStack.count)

                if (diff > 0) {
                    baseStack.grow(diff)
                    stack.shrink(diff)
                    continue
                } else if (diff == 0) break
            }
        }

        val inPlaceStacks = mutableListOf<Pair<ItemStack, Int>>()
        val sorted = mutableListOf<ItemStack>()

        for (i in 0 until wrapper.backpackInventorySize()) {
            val stack = wrapper.getStackInSlot(i)

            if (wrapper.isSlotMemorized(i) || wrapper.isSlotLocked(i)) {
                inPlaceStacks.add(stack to i)
                continue
            } else {
                sorted.add(stack)
            }
        }

        sorted.sortWith { stack1, stack2 ->
            val item1 = stack1.item
            val item2 = stack2.item

            if (stack1.isEmpty && stack2.isEmpty) return@sortWith 0
            else if (stack1.isEmpty) return@sortWith 1
            else if (stack2.isEmpty) return@sortWith -1

            when (wrapper.sortType) {
                SortType.BY_NAME -> {
                    item1.getItemStackDisplayName(stack1).compareTo(item2.getItemStackDisplayName(stack2))
                }

                SortType.BY_MOD_ID -> {
                    item1.registryName!!.namespace.compareTo(item2.registryName!!.namespace)
                }

                SortType.BY_COUNT -> {
                    stack1.count.compareTo(stack2.count)
                }

                SortType.BY_ORE_DICT -> {
                    val oreDict1 = OreDictionary.getOreIDs(stack1).map(OreDictionary::getOreName)
                    val oreDict2 = OreDictionary.getOreIDs(stack2).map(OreDictionary::getOreName)

                    compareLists(oreDict1, oreDict2)
                }
            }
        }

        for ((stack, i) in inPlaceStacks) {
            sorted.add(i, stack)
        }

        wrapper.backpackItemStackHandler.setSize(wrapper.backpackInventorySize())

        for ((slotIndex, stack) in sorted.withIndex()) {
            wrapper.backpackItemStackHandler.setStackInSlot(slotIndex, stack)
        }
    }

    fun transferPlayerInventoryToBackpack(
        wrapper: BackpackWrapper,
        playerInventory: PlayerMainInvWrapper,
        transferMatched: Boolean
    ) {
        for (i in 9 until playerInventory.slots) {
            var stack = playerInventory.getStackInSlot(i)

            if (stack.item is BackpackItem) {
                val currentBackpackWrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)

                if (currentBackpackWrapper === wrapper)
                    continue

                if (!wrapper.canNestBackpack())
                    continue
            }

            for (j in 0 until wrapper.backpackInventorySize()) {
                stack = wrapper.backpackItemStackHandler.insertItemToMemorySlots(stack, false)

                if (transferMatched && wrapper.getStackInSlot(j).isEmpty)
                    continue

                stack = wrapper.insertItem(j, stack, false)
            }

            playerInventory.setStackInSlot(i, stack)
        }
    }

    fun transferBackpackToPlayerInventory(
        wrapper: BackpackWrapper,
        playerInventory: PlayerMainInvWrapper,
        transferMatched: Boolean
    ) {
        for (i in 0 until wrapper.backpackInventorySize()) {
            var stack = wrapper.getStackInSlot(i)

            for (j in 9 until playerInventory.slots) {
                if (transferMatched && playerInventory.getStackInSlot(j).isEmpty)
                    continue

                stack = playerInventory.insertItem(j, stack, false)
            }

            wrapper.backpackItemStackHandler.setStackInSlot(i, stack)
        }
    }

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