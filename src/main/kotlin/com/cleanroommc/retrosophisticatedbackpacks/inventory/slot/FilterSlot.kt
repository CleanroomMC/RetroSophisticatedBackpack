package com.cleanroommc.retrosophisticatedbackpacks.inventory.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class FilterSlot(itemHandler: IItemHandler, index: Int) : ModularSlot(itemHandler, index, true) {
    override fun getItemStackLimit(stack: ItemStack): Int =
        1
}