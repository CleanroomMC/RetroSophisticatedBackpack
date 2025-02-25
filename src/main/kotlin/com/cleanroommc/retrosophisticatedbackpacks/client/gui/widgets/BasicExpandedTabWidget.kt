package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import net.minecraft.item.ItemStack

open class BasicExpandedTabWidget<T>(
    slotIndex: Int,
    override val wrapper: T,
    delegatedIconStack: ItemStack,
    titleKey: String,
    filterSyncKey: String = "common_filter",
    coveredTabSize: Int = 4,
    width: Int = 75,
) : ExpandedUpgradeTabWidget<T>(slotIndex, coveredTabSize, delegatedIconStack, titleKey, width)
        where T : IBasicFilterable, T : UpgradeWrapper<*> {
    protected val startingRow: Row = Row()
        .height(0)
        .debugName("starting_row") as Row
    protected val filterWidget: BasicFilterWidget = BasicFilterWidget(wrapper, slotIndex, filterSyncKey)
        .width(64)
        .coverChildrenHeight()
        .debugName("filter_widget")

    init {
        val column = Column()
            .pos(8, 28)
            .width(64)
            .childPadding(2)
            .child(startingRow)
            .child(filterWidget)

        child(column)
    }
}