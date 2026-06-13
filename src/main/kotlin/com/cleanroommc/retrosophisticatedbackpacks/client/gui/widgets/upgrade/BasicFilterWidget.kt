package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.value.ISyncOrValue
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IContentsFilterable
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.setEnabledIfAndEnabled

class BasicFilterWidget(
    var filterableWrapper: IBasicFilterable,
    slotIndex: Int,
    syncKey: String = "common_filter",
    private val showFilterButton: Boolean = true,
    private val slotFactory: (Int, () -> UpgradeSlotSH?) -> PhantomItemSlot = { _, _ -> PhantomItemSlot() }
) :
    ParentWidget<BasicFilterWidget>() {
    companion object {
        private val FILTER_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.whitelist".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.blacklist".asTranslationKey()), RSBTextures.CROSS_ICON),
        )
        private val CONTENTS_FILTER_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.allow".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.block".asTranslationKey()), RSBTextures.CROSS_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.match_backpack_contents".asTranslationKey()), RSBTextures.MATCH_BACKPACK_CONTENTS_ICON),
        )
    }

    private val filterTypeButton: CyclicVariantButtonWidget
    private val filterSlots: List<PhantomItemSlot>
    var slotSyncHandler: UpgradeSlotSH? = null
        private set

    init {
        syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            if (filterableWrapper is IContentsFilterable) CONTENTS_FILTER_TYPE_VARIANTS else FILTER_TYPE_VARIANTS,
            filterButtonIndex(),
            iconOffset = 1,
            buttonWidth = 18,
            buttonHeight = 18,
            hasCustomTexture = true
        ) { index ->
            updateFilterType(index)
        }
            .size(18, 18)

        val slotGroup = SlotGroupWidget().name("${syncKey}s")
        slotGroup.coverChildren().top(if (showFilterButton) 21 else 0)
        slotGroup.disableSortButtons()
        slotGroup.setEnabledIfAndEnabled {
            (filterableWrapper as? IContentsFilterable)?.contentsFilterType != IContentsFilterable.ContentsFilterType.STORAGE
        }
        filterSlots = mutableListOf<PhantomItemSlot>()

        val filterSlotCount = filterableWrapper.filterItems.slots
        val slotsInRow = if (filterSlotCount > 0) filterableWrapper.slotsInRow.coerceIn(1, filterSlotCount) else 1
        for (i in 0 until filterSlotCount) {
            val slot =
                slotFactory(i) { slotSyncHandler }.syncHandler("${syncKey}_$slotIndex", i)
                    .pos(i % slotsInRow * 18, i / slotsInRow * 18) as PhantomItemSlot

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        if (showFilterButton) {
            child(filterTypeButton)
        }
        child(slotGroup)
    }

    private fun filterButtonIndex(): Int =
        (filterableWrapper as? IContentsFilterable)?.contentsFilterType?.ordinal ?: filterableWrapper.filterType.ordinal

    private fun updateFilterType(index: Int) {
        val contentsFilterable = filterableWrapper as? IContentsFilterable
        if (contentsFilterable != null) {
            val filterType = IContentsFilterable.ContentsFilterType.entries[index]
            contentsFilterable.contentsFilterType = filterType
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_CONTENTS_FILTERABLE) {
                it.writeEnumValue(filterType)
            }
            return
        }

        filterableWrapper.filterType = IBasicFilterable.FilterType.entries[index]
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_BASIC_FILTERABLE) {
            it.writeEnumValue(filterableWrapper.filterType)
        }
    }

    override fun isValidSyncOrValue(syncHandler: ISyncOrValue): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }
}
