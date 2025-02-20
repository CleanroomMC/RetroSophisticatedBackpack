package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

sealed interface IFeedingUpgrade {
    // This considers upgrade's settings, and split 1 food from (backpack only) inventory and returns it
    // if it meets requirements
    fun getFeedingStack(inventory: IInventory, foodLevel: Int, health: Float, maxHealth: Float): ItemStack
}
