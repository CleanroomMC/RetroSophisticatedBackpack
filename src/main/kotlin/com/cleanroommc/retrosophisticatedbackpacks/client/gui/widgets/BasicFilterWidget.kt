package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class BasicFilterWidget(
    private val filterableWrapper: IBasicFilterable,
    slotIndex: Int,
    syncKey: String = "common_filter"
) :
    ParentWidget<BasicFilterWidget>() {
    companion object {
        private val FILTER_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.whitelist".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.blacklist".asTranslationKey()), RSBTextures.CROSS_ICON),
        )
    }

    private val filterTypeButton: CyclicVariantButtonWidget
    private val filterSlots: List<ItemSlot>
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        size(60, 54).syncHandler("upgrades", slotIndex)

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

        val slotGroup = SlotGroupWidget().debugName("${syncKey}s")
        slotGroup.coverChildren().top(26)
        filterSlots = mutableListOf<ItemSlot>()

        for (i in 0 until 9) {
            val slot = ItemSlot().syncHandler("${syncKey}_$slotIndex", i).pos(i % 3 * 18, i / 3 * 18)

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