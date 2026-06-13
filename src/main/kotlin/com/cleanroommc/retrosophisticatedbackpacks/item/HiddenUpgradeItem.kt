package com.cleanroommc.retrosophisticatedbackpacks.item

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable

abstract class HiddenUpgradeItem<CP>(
    registryName: String,
    private val wrapperFactory: () -> CP,
) : UpgradeItem(registryName, false)
        where CP : ICapabilityProvider, CP : INBTSerializable<NBTTagCompound> {
    override fun initCapabilities(stack: net.minecraft.item.ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val capability = wrapperFactory.invoke()
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}
