package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import net.minecraft.item.ItemStack

open class BasicExpandedTabWidget<T>(
    slotIndex: Int,
    override val wrapper: T,
    delegatedIconStack: ItemStack,
    titleKey: String,
    filterSyncKey: String = "common_filter",
    width: Int = 75,
) : ExpandedTabWidget<T>(slotIndex, 4, delegatedIconStack, titleKey, width)
        where T : IBasicFilterable, T: UpgradeWrapper<*> {
    protected val filterWidget: BasicFilterWidget = BasicFilterWidget(wrapper, slotIndex, filterSyncKey)
        .top(28)
        .left(8)

    init {
        child(filterWidget)
    }
}