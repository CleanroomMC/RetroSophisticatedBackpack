package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import net.minecraft.item.ItemStack

open class AdvancedExpandedTabWidget<T>(
    slotIndex: Int,
    override val wrapper: T,
    delegatedIconStack: ItemStack,
    titleKey: String,
    filterSyncKey: String = "adv_common_filter",
    coveredTabSize: Int = 5,
    width: Int = 100,
) : ExpandedUpgradeTabWidget<T>(slotIndex, coveredTabSize, delegatedIconStack, titleKey, width)
        where T : IAdvancedFilterable, T : UpgradeWrapper<*> {
    protected val startingRow: Row = Row()
        .height(0)
        .debugName("starting_row") as Row
    protected val filterWidget: AdvancedFilterWidget = AdvancedFilterWidget(slotIndex, wrapper, filterSyncKey)
        .width(88)
        .coverChildrenHeight()
        .debugName("adv_filter_widget")

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

