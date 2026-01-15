package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IVoidUpgrade
import net.minecraft.item.ItemStack

class ModularBackpackSlot(
    private val wrapper: BackpackWrapper,
    index: Int
) : ModularSlot(wrapper.backpackItemStackHandler, index) {
    fun getMemoryStack(): ItemStack =
        wrapper.getMemorizedStack(slotIndex)

    override fun getSlotStackLimit(): Int =
        Int.MAX_VALUE

    override fun getItemStackLimit(stack: ItemStack): Int =
        stack.maxStackSize * wrapper.getTotalStackMultiplier()

    override fun putStack(stack: ItemStack) {
        // Needs to check if stack is empty, if it's empty and somehow voidable, it would cause player's backpack filled
        // with the original item in the backpack
        if (!stack.isEmpty && wrapper.canVoid(stack, IVoidUpgrade.TransferSource.ALL, IVoidUpgrade.VoidType.ANY))
            return
        
        super.putStack(stack)
    }
}
