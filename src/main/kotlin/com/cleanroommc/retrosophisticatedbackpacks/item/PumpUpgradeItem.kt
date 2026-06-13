package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IPumpUpgrade
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.ICapabilityProvider

class PumpUpgradeItem(registryName: String, private val wrapperFactory: () -> IPumpUpgrade) :
    UpgradeItem(registryName, true) {
    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val capability = wrapperFactory()
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}
