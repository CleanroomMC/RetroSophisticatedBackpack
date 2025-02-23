package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

class StackUpgradeItem(registryName: String, val multiplier: () -> Int) : UpgradeItem(registryName) {
    override fun addInformation(
        stack: ItemStack,
        worldIn: World?,
        tooltip: MutableList<String>,
        flagIn: ITooltipFlag
    ) {
        tooltip.add(TextComponentTranslation("tooltip.stack_upgrade".asTranslationKey(), multiplier()).formattedText)
    }
}
