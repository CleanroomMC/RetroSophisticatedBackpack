package com.cleanroommc.retrosophisticatedbackpacks.capability

import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider

interface ISidelessCapabilityProvider : ICapabilityProvider {
    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        if (hasCapability(capability, facing)) capability.cast(this as T) else null
}