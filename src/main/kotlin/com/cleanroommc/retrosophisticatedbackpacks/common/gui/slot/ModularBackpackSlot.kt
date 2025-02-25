package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.item.ItemStack

class ModularBackpackSlot(
    private val wrapper: BackpackWrapper,
    index: Int
) : ModularSlot(wrapper.backpackItemStackHandler, index) {
    fun getMemoryStack(): ItemStack =
        wrapper.getMemorizedStack(slotIndex)

    override fun getItemStackLimit(stack: ItemStack): Int =
        stack.maxStackSize * wrapper.getTotalStackMultiplier()
}