package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherUpgradeWrapper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.ICapabilityProvider

class MobCatcherUpgradeItem(
    registryName: String,
    val advanced: Boolean,
    private val wrapperFactory: (Boolean) -> MobCatcherUpgradeWrapper
) : UpgradeItem(registryName, false) {
    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val capability = wrapperFactory(advanced)
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}
