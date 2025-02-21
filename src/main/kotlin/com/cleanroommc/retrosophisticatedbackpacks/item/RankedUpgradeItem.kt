package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable

abstract class RankedUpgradeItem<CP>(
    registryName: String,
    private val capabilityProvider: () -> CP,
) : UpgradeItem(registryName, true)
        where CP : ICapabilityProvider, CP : INBTSerializable<NBTTagCompound> {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(TextComponentTranslation("tooltip.${registryName}".asTranslationKey()).formattedText)
    }

    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val capability = capabilityProvider.invoke()
        nbt?.let(capability::deserializeNBT)
        return capability
    }
}