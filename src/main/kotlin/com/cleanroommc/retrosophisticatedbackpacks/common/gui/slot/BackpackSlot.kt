package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
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