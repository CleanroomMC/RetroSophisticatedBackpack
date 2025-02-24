package com.cleanroommc.retrosophisticatedbackpacks.backpack

import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack

object BackpackDataFixer {
    fun fixFeedingUpgrade(filterStacks: ExposedItemStackHandler) {
        for (slotIndex in 0 until filterStacks.slots) {
            val stack = filterStacks.getStackInSlot(slotIndex)

            if (stack.item !is ItemFood) {
                filterStacks.setStackInSlot(slotIndex, ItemStack.EMPTY)
            }
        }
    }
}