package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.items.ItemStackHandler

class UpgradeItemStackHandler(size: Int) : ItemStackHandler(size) {
    val inventory: NonNullList<ItemStack> =
        stacks
}