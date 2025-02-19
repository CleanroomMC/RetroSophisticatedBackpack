package com.cleanroommc.retrosophisticatedbackpacks.backpack

import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider

interface ISidelessCapabilityProvider : ICapabilityProvider {
    val acceptableCapabilities: List<Capability<*>>

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        facing == null && acceptableCapabilities.contains(capability)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        if (hasCapability(capability, facing)) capability.cast(this as T) else null
}