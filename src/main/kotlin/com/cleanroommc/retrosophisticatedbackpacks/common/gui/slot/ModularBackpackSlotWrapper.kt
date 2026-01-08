package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.bogosorter.compat.SlotDelegate
import net.minecraft.item.ItemStack

class ModularBackpackSlotWrapper(private val slot: ModularBackpackSlot) : SlotDelegate(slot) {
    override fun `bogo$getMaxStackSize`(itemStack: ItemStack): Int =
        slot.getItemStackLimit(itemStack)

    override fun `bogo$getItemStackLimit`(itemStack: ItemStack): Int =
        slot.getItemStackLimit(itemStack)
}