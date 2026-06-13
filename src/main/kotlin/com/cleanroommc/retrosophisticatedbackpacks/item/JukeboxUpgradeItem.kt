package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IJukeboxUpgrade
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.ICapabilityProvider

class JukeboxUpgradeItem(
    registryName: String,
    private val wrapperFactory: () -> IJukeboxUpgrade,
) : UpgradeItem(registryName, true, "jukebox") {
    override fun initCapabilities(stack: net.minecraft.item.ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val capability = wrapperFactory.invoke()
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}
