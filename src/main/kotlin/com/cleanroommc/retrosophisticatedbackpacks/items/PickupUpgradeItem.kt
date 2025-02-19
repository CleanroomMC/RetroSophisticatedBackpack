package com.cleanroommc.retrosophisticatedbackpacks.items

import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.AdvancedPickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider

class PickupUpgradeItem(val registryName: String, val isAdvanced: Boolean = false) : UpgradeItem(registryName, true) {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        val key = "tooltip.${if (isAdvanced) "advanced_" else ""}pickup_upgrade"

        tooltip.add(TextComponentTranslation(key.asTranslationKey()).formattedText)
    }

    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val wrapper = if (isAdvanced) AdvancedPickupUpgradeWrapper() else PickupUpgradeWrapper()
        nbt?.let(wrapper::deserializeNBT)
        return wrapper
    }
}