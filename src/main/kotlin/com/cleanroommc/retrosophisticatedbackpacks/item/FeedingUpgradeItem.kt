package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.FeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider

class FeedingUpgradeItem(registryName: String, private val isAdvanced: Boolean = false) :
    UpgradeItem(registryName, true, true) {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        val key = "tooltip.${if (isAdvanced) "advanced_" else ""}feed_upgrade"

        tooltip.add(TextComponentTranslation(key.asTranslationKey()).formattedText)
    }

    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val wrapper = if (isAdvanced) AdvancedFeedingUpgradeWrapper() else FeedingUpgradeWrapper()
        nbt?.let(wrapper::deserializeNBT)
        return wrapper
    }
}