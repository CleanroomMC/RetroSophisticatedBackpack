package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

sealed interface IRestockUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    fun canRestock(stack: ItemStack): Boolean

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IRESTOCK_UPGRADE_CAPABILITY
}