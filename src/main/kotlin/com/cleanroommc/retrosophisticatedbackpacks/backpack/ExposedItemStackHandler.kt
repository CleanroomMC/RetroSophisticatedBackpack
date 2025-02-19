package com.cleanroommc.retrosophisticatedbackpacks.backpack

import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.items.ItemStackHandler

open class ExposedItemStackHandler(size: Int) : ItemStackHandler(size) {
    val inventory: NonNullList<ItemStack>
        get() = stacks
}