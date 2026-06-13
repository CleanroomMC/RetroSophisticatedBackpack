package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack

open class BasicExpandedTabWidget<T>(
    slotIndex: Int,
    wrap: T,
    delegatedIconStack: ItemStack,
    titleKey: String,
    filterSyncKey: String = "common_filter",
    coveredTabSize: Int = 4,
    width: Int = 75,
    contentX: Int = 8,
    contentY: Int = 28,
    contentWidth: Int = 64,
    contentPadding: Int = 2,
    filterWidth: Int = 64,
    showFilterButton: Boolean = true,
    slotFactory: (Int, () -> UpgradeSlotSH?) -> PhantomItemSlot = { _, _ -> PhantomItemSlot() },
) : ExpandedUpgradeTabWidget<T>(slotIndex, wrap, coveredTabSize, delegatedIconStack, titleKey, width)
        where T : IBasicFilterable, T : UpgradeWrapper<*> {
    protected val startingRow: Row = Row()
        .height(0)
        .name("starting_row") as Row
    protected val filterWidget: BasicFilterWidget = BasicFilterWidget(wrap, slotIndex, filterSyncKey, showFilterButton, slotFactory)
        .width(filterWidth)
        .coverChildrenHeight()
        .name("filter_widget")

    override fun onWrapperChange(after: T) {
        super.onWrapperChange(after)
        filterWidget.filterableWrapper = after
    }

    init {
        val column = Column()
            .pos(contentX, contentY)
            .width(contentWidth)
            .childPadding(contentPadding)
            .child(startingRow)
            .child(filterWidget)

        child(column)
    }
}

internal fun filterTabWidth(slotsInRow: Int): Int =
    maxOf(75, 3 + slotsInRow.coerceAtLeast(1) * 18 + 6)

internal fun filterTabSize(filterSlots: Int, slotsInRow: Int, hasTopButtonRow: Boolean = true, hasFilterButtonRow: Boolean = true): Int {
    val columns = slotsInRow.coerceAtLeast(1)
    val rows = ((filterSlots.coerceAtLeast(1) + columns - 1) / columns).coerceAtLeast(1)
    val bottom = 24 + (if (hasTopButtonRow) 20 else 0) + (if (hasFilterButtonRow) 21 else 0) + rows * 18 + 6
    return ((bottom + 29) / 30).coerceAtLeast(3)
}
