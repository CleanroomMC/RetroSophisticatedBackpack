package com.cleanroommc.retrosophisticatedbackpacks.item

import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class StackUpgradeItem(registryName: String, val multiplier: () -> Int) : UpgradeItem(registryName) {
    override fun addInformation(
        stack: ItemStack,
        worldIn: World?,
        tooltip: MutableList<String>,
        flagIn: ITooltipFlag
    ) {
    }
}
