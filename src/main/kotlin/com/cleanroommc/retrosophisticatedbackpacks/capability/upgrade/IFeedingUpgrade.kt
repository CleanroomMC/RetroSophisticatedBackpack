package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

sealed interface IFeedingUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    // This considers upgrade's settings, and split 1 food from (backpack only) inventory and returns it
    // if it meets requirements
    fun getFeedingStack(inventory: IInventory, foodLevel: Int, health: Float, maxHealth: Float): ItemStack
}
