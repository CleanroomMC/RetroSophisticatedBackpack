package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget.PickupUpgradeWidget.Companion.FILTER_TYPE_VARIANTS
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH

class BasicFilterWidget(private val filterableWrapper: IBasicFilterable, private val slotIndex: Int) :
    ParentWidget<BasicFilterWidget>() {
    private val filterTypeButton: CyclicVariantButtonWidget
    private val filterSlots: List<ItemSlot>
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        coverChildren().syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            FILTER_TYPE_VARIANTS,
            filterableWrapper.filterType.ordinal
        ) { index ->
            filterableWrapper.filterType = IBasicFilterable.FilterType.entries[index]
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_BASIC_FILTERABLE) {
                it.writeEnumValue(filterableWrapper.filterType)
            }
        }
            .size(20, 20)

        val slotGroup = SlotGroupWidget().debugName("pickup_filters")
        slotGroup.coverChildren().top(24)
        filterSlots = mutableListOf<ItemSlot>()

        for (i in 0 until 9) {
            val slot = ItemSlot().syncHandler("pickup_filter_$slotIndex", i).pos(i % 3 * 18, i / 3 * 18)

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        child(filterTypeButton)
            .child(slotGroup)
    }

    override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }
}