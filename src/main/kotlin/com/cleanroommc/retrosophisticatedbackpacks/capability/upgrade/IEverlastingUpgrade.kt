package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

interface IEverlastingUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IEVERLASTING_UPGRADE_CAPABILITY

    object Impl : IEverlastingUpgrade {
        override fun serializeNBT(): NBTTagCompound = NBTTagCompound()
        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }
}
