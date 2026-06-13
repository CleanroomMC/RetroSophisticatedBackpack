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

    override fun getSlotStackLimit(): Int =
        if (wrapper.isSlotBlockedByMobCatcher(slotIndex)) 0 else Int.MAX_VALUE

    override fun getItemStackLimit(stack: ItemStack): Int =
        if (wrapper.isSlotBlockedByMobCatcher(slotIndex)) 0 else wrapper.getStackLimit(stack)

    override fun isItemValid(stack: ItemStack): Boolean =
        !wrapper.isSlotBlockedByMobCatcher(slotIndex) && super.isItemValid(stack)
}
