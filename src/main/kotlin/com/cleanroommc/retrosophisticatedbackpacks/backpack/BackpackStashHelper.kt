package com.cleanroommc.retrosophisticatedbackpacks.backpack

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper

object BackpackStashHelper {
    enum class Result {
        MATCH_AND_SPACE,
        SPACE,
        NO_SPACE
    }

    fun getStashResult(backpackStack: ItemStack, stack: ItemStack): Result {
        val wrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return Result.NO_SPACE
        return getStashResult(wrapper, stack)
    }

    fun getStashResult(wrapper: BackpackWrapper, stack: ItemStack): Result {
        if (stack.isEmpty || wrapper.insertStack(stack.copy(), true, true).count == stack.count) {
            return Result.NO_SPACE
        }
        return if (hasMatchingStack(wrapper, stack) || hasMatchingMemory(wrapper, stack)) Result.MATCH_AND_SPACE else Result.SPACE
    }

    fun stash(wrapper: BackpackWrapper, stack: ItemStack, simulate: Boolean): ItemStack =
        wrapper.insertStack(stack.copy(), simulate, true)

    private fun hasMatchingStack(wrapper: BackpackWrapper, stack: ItemStack): Boolean =
        (0 until wrapper.slots).any {
            val slotStack = wrapper.getStackInSlot(it)
            !slotStack.isEmpty && ItemHandlerHelper.canItemStacksStack(slotStack, stack)
        }

    private fun hasMatchingMemory(wrapper: BackpackWrapper, stack: ItemStack): Boolean =
        (0 until wrapper.backpackInventorySize()).any {
            val memoryStack = wrapper.getMemorizedStack(it)
            !memoryStack.isEmpty && if (wrapper.isMemoryStackRespectNBT(it)) {
                ItemStack.areItemStacksEqual(stack, memoryStack)
            } else {
                stack.isItemEqualIgnoreDurability(memoryStack)
            }
        }
}
