package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.PickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH

class PickupUpgradeWidget(
    slotIndex: Int,
    private val wrapper: PickupUpgradeWrapper
) : ExpandedTabWidget(3) {
    companion object {
        val FILTER_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.whitelist".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.blacklist".asTranslationKey()), RSBTextures.CROSS_ICON),
        )
    }

    private val filterTypeButton: CyclicVariantButtonWidget
    private val filterSlots: List<ItemSlot>
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        size(70, 120).syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            FILTER_TYPE_VARIANTS,
            wrapper.filterType.ordinal
        ) { index ->
            wrapper.filterType = PickupUpgradeWrapper.FilterType.entries[index]
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_PICKUP_UPGRADE_PICKUP_TYPE) {
                it.writeEnumValue(wrapper.filterType)
            }
        }
            .size(20, 20)
            .leftRelOffset(0.2F, -2)
            .top(36)

        val slotGroup = SlotGroupWidget().debugName("pickup_filters")
        slotGroup.coverChildren().leftRel(0.5f).top(60)
        filterSlots = mutableListOf<ItemSlot>()

        for (i in 0 until 9) {
            val slot = ItemSlot().syncHandler("pickup_filter_$slotIndex", i).pos(i % 3 * 18, i / 3 * 18)

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        child(filterTypeButton)
            .child(slotGroup)
            .child(
                TextWidget(IKey.lang("gui.pickup_settings".asTranslationKey()))
                    .size(40, 20)
                    .leftRel(0.85f)
                    .topRelAnchor(0.125f, 0.5f)
            )
    }

    override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }
}