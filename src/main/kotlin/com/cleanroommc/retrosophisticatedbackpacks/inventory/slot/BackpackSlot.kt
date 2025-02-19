package com.cleanroommc.retrosophisticatedbackpacks.inventory.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.items.BackpackItem
import net.minecraft.item.ItemStack

class BackpackSlot(
    private val stackMultiplier: () -> Int,
    private val canNestBackpack: () -> Boolean,
    itemHandler: BackpackItemStackHandler,
    index: Int
) : ModularSlot(itemHandler, index) {
    override fun isItemValid(stack: ItemStack): Boolean =
        stack.item !is BackpackItem || canNestBackpack()

    override fun getItemStackLimit(stack: ItemStack): Int {
        return stack.maxStackSize * stackMultiplier()
    }
}