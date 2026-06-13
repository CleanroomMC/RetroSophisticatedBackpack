package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import net.minecraft.item.ItemStack

object UpgradeFilterUtils {
    fun IBasicFilterable.matchesAllowEmpty(stack: ItemStack): Boolean {
        if (filterItems.inventory.all(ItemStack::isEmpty)) {
            return filterType == IBasicFilterable.FilterType.BLACKLIST
        }

        return checkFilter(stack)
    }
}
