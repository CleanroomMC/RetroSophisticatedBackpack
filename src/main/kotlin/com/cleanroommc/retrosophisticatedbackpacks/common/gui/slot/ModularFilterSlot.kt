package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class ModularFilterSlot(itemHandler: IItemHandler, index: Int) : ModularSlot(itemHandler, index) {
    override fun getItemStackLimit(stack: ItemStack): Int =
        1
}