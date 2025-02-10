package com.cleanroommc.retrosophisticatedbackpack.backpack

import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.items.ItemStackHandler

class BackpackItemStackHandler(size: Int) : ItemStackHandler(size) {
    val inventory: NonNullList<ItemStack> =
        stacks
}