package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.ITankUpgrade
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.ICapabilityProvider

class TankUpgradeItem(registryName: String, wrapperFactory: () -> ITankUpgrade) :
    UpgradeItem(registryName, true) {
    private val wrapperFactory = wrapperFactory

    override fun initCapabilities(stack: net.minecraft.item.ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val capability = wrapperFactory.invoke()
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}
