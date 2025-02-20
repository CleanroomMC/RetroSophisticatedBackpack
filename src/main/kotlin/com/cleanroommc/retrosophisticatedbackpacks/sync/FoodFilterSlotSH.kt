package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack

class FoodFilterSlotSH(slot: ModularSlot) : FilterSlotSH(slot) {
    override fun isItemValid(itemStack: ItemStack): Boolean =
        itemStack.item is ItemFood
}