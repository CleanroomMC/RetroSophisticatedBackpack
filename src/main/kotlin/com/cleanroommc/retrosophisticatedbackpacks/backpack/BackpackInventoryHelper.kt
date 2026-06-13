package com.cleanroommc.retrosophisticatedbackpacks.backpack

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
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
                val comparedValue = item2.compareTo(item1)

                if (comparedValue != 0)
                    return comparedValue
            }

            return list2.size.compareTo(list1.size)
        }

        // Merges all slots first
        for (i in 0 until wrapper.backpackInventorySize() - 1) {
            if (wrapper.isSlotLocked(i))
                continue

            val isMemorizedSlot = wrapper.isSlotMemorized(i)
            val baseStack = wrapper.getStackInSlot(i)
            val maxSize = wrapper.getStackLimit(baseStack)

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
                    item2.getItemStackDisplayName(stack2).compareTo(item1.getItemStackDisplayName(stack1))
                }

                SortType.BY_MOD_ID -> {
                    item2.registryName!!.namespace.compareTo(item1.registryName!!.namespace)
                }

                SortType.BY_COUNT -> {
                    stack2.count.compareTo(stack1.count)
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
            if (stack.isEmpty)
                continue

            if (transferMatched && !backpackContainsOrMemory(wrapper, stack))
                continue

            if (stack.item is BackpackItem) {
                val currentBackpackWrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)

                if (currentBackpackWrapper === wrapper)
                    continue

                if (!wrapper.canNestBackpack())
                    continue
            }

            stack = if (transferMatched) insertIntoMatchingBackpackSlots(wrapper, stack)
            else insertIntoBackpackMatchingFirst(wrapper, stack)

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
            if (stack.isEmpty)
                continue

            if (transferMatched && !handlerContains(playerInventory, stack, 0))
                continue

            stack = if (transferMatched) insertIntoMatchingHandlerSlots(playerInventory, stack, 0)
            else insertIntoHandlerMatchingFirst(playerInventory, stack, 9)

            wrapper.backpackItemStackHandler.setStackInSlot(i, stack)
        }
    }

    private fun insertIntoBackpackMatchingFirst(wrapper: BackpackWrapper, stack: ItemStack): ItemStack {
        var stack = insertIntoMatchingBackpackSlots(wrapper, stack)
        for (slot in 0 until wrapper.backpackInventorySize()) {
            stack = wrapper.insertItem(slot, stack, false)
            if (stack.isEmpty)
                break
        }
        return stack
    }

    private fun insertIntoMatchingBackpackSlots(wrapper: BackpackWrapper, stack: ItemStack): ItemStack {
        var stack = stack
        for (slot in 0 until wrapper.backpackInventorySize()) {
            if (!matchesStackKey(wrapper.getStackInSlot(slot), stack))
                continue

            stack = wrapper.insertItem(slot, stack, false)
            if (stack.isEmpty)
                return stack
        }

        for (slot in 0 until wrapper.backpackInventorySize()) {
            if (!matchesMemorySlot(wrapper, slot, stack))
                continue

            stack = wrapper.insertItem(slot, stack, false)
            if (stack.isEmpty)
                return stack
        }

        return stack
    }

    private fun insertIntoHandlerMatchingFirst(handler: IItemHandlerModifiable, stack: ItemStack, firstSlot: Int): ItemStack {
        var stack = insertIntoMatchingHandlerSlots(handler, stack, firstSlot)
        for (slot in firstSlot until handler.slots) {
            stack = handler.insertItem(slot, stack, false)
            if (stack.isEmpty)
                break
        }
        return stack
    }

    private fun insertIntoMatchingHandlerSlots(handler: IItemHandlerModifiable, stack: ItemStack, firstSlot: Int): ItemStack {
        var stack = stack
        for (slot in firstSlot until handler.slots) {
            if (!matchesStackKey(handler.getStackInSlot(slot), stack))
                continue

            stack = handler.insertItem(slot, stack, false)
            if (stack.isEmpty)
                break
        }
        return stack
    }

    private fun backpackContainsOrMemory(wrapper: BackpackWrapper, stack: ItemStack): Boolean =
        handlerContains(wrapper.backpackItemStackHandler, stack, 0) ||
                wrapper.backpackItemStackHandler.memorizedSlotStack.indices.any { matchesMemorySlot(wrapper, it, stack) }

    private fun handlerContains(handler: IItemHandler, stack: ItemStack, firstSlot: Int): Boolean {
        for (slot in firstSlot until handler.slots) {
            if (matchesStackKey(handler.getStackInSlot(slot), stack)) {
                return true
            }
        }
        return false
    }

    private fun matchesStackKey(first: ItemStack, second: ItemStack): Boolean =
        !first.isEmpty && !second.isEmpty &&
                ItemStack.areItemsEqual(first, second) &&
                ItemStack.areItemStackTagsEqual(first, second)

    private fun matchesMemorySlot(wrapper: BackpackWrapper, slot: Int, stack: ItemStack): Boolean {
        val memoryStack = wrapper.backpackItemStackHandler.memorizedSlotStack[slot]
        return !memoryStack.isEmpty && !stack.isEmpty &&
                if (wrapper.backpackItemStackHandler.memorizedSlotRespectNbtList[slot])
                    ItemStack.areItemStacksEqual(stack, memoryStack)
                else stack.isItemEqualIgnoreDurability(memoryStack)
    }

    fun attemptDepositOnTileEntity(wrapper: BackpackWrapper, destination: TileEntity, facing: EnumFacing): Boolean {
        if (Config.isInteractionBlockDisallowed(destination.blockType)) {
            return false
        }
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
        if (Config.isInteractionBlockDisallowed(source.blockType)) {
            return false
        }
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
            val sourceStack = source.getStackInSlot(i)

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
        when (handler) {
            is ISidedInventory -> SidedInvWrapper(handler, facing)
            is IInventory -> InvWrapper(handler)
            else -> handler as? IItemHandler
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
}
