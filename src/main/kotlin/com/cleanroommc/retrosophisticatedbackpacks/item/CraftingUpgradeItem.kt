package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.ICapabilityProvider

class CraftingUpgradeItem(registryName: String) : UpgradeItem(registryName, true) {
    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val wrapper = CraftingUpgradeWrapper()
        nbt?.let(wrapper::deserializeNBT)
        return wrapper
    }
}
