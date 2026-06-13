package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToolSwapperUpgrade

class ToolSwapperUpgradeItem(
    registryName: String,
    private val wrapperFactory: () -> IToolSwapperUpgrade,
    hasTab: Boolean = false
) :
    UpgradeItem(registryName, hasTab) {
    override fun initCapabilities(stack: net.minecraft.item.ItemStack, nbt: net.minecraft.nbt.NBTTagCompound?): net.minecraftforge.common.capabilities.ICapabilityProvider {
        val capability = wrapperFactory.invoke()
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}
