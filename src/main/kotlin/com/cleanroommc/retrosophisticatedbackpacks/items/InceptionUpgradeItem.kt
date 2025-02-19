package com.cleanroommc.retrosophisticatedbackpacks.items

import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

class InceptionUpgradeItem(registryName: String) : UpgradeItem(registryName) {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(TextComponentTranslation("tooltip.inception_upgrade".asTranslationKey()).formattedText)
    }
}