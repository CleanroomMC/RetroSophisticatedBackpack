package com.cleanroommc.retrosophisticatedbackpack.items

import com.cleanroommc.retrosophisticatedbackpack.backpack.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider

class CraftingUpgradeItem(registryName: String) : UpgradeItem(registryName, true) {
    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider {
        val wrapper = CraftingUpgradeWrapper()

        if (nbt != null) {
            wrapper.deserializeNBT(nbt)
        }

        return wrapper
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(TextComponentTranslation("tooltip.crafting_upgrade".asTranslationKey()).formattedText)
        tooltip.add(TextComponentString("WIP Item, do not use!").setStyle(Style().setColor(TextFormatting.RED)).formattedText)
    }
}