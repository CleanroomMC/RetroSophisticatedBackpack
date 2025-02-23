package com.cleanroommc.retrosophisticatedbackpacks.item

import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable

abstract class RankedUpgradeItem<CP>(
    registryName: String,
    private val wrapperFactory: () -> CP,
) : UpgradeItem(registryName, true)
        where CP : ICapabilityProvider, CP : INBTSerializable<NBTTagCompound> {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)
    }

    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val capability = wrapperFactory.invoke()
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}