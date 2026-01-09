package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import net.minecraft.item.ItemStack

open class AdvancedExpandedTabWidget<T>(
    slotIndex: Int,
    wrap: T,
    delegatedIconStack: ItemStack,
    titleKey: String,
    filterSyncKey: String = "adv_common_filter",
    coveredTabSize: Int = 5,
    width: Int = 100,
) : ExpandedUpgradeTabWidget<T>(slotIndex, wrap, coveredTabSize, delegatedIconStack, titleKey, width)
        where T : IAdvancedFilterable, T : UpgradeWrapper<*> {
    protected val startingRow: Row = Row()
        .height(0)
        .name("starting_row") as Row
    protected val filterWidget: AdvancedFilterWidget = AdvancedFilterWidget(slotIndex, wrap, filterSyncKey)
        .width(88)
        .coverChildrenHeight()
        .name("adv_filter_widget")

    override fun onWrapperChange(after: T) {
        super.onWrapperChange(after)
        filterWidget.filterableWrapper = after
    }

    init {
        val column = Column()
            .pos(8, 28)
            .width(88)
            .childPadding(2)
            .child(startingRow)
            .child(filterWidget)

        child(column)
    }
}

