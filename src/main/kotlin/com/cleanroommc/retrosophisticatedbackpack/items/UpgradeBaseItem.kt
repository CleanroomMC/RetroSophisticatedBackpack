package com.cleanroommc.retrosophisticatedbackpack.items

import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

class UpgradeBaseItem(registryName: String) : UpgradeItem(registryName, false) {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(TextComponentTranslation("tooltip.upgrade_base".asTranslationKey()).formattedText)
    }
}