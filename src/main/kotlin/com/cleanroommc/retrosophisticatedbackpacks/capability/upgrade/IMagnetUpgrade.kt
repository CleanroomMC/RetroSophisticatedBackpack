package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraft.nbt.NBTTagCompound

sealed interface IMagnetUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    val range: Double
    var pickupItems: Boolean

    fun canPickup(stack: ItemStack, backpackWrapper: BackpackWrapper): Boolean

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IMAGNET_UPGRADE_CAPABILITY
}
