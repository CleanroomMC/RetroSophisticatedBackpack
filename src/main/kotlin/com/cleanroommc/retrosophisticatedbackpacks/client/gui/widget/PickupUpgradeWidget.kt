package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.value.sync.UpgradeSlotSH

class PickupUpgradeWidget(
    panel: BackpackPanel,
    slotIndex: Int,
    private val pickupUpgradeWrapper: PickupUpgradeWrapper
) : ExpandedTabWidget(3) {
    companion object {
        private val VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.whitelist".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.blacklist".asTranslationKey()), RSBTextures.CROSS_ICON),
        )

        internal const val PICKUP_FILTER_SYNC_HANDLER = "pickup_filter"
    }

    val filterTypeButton: ButtonWidget<*>
    val filterSlots: List<ItemSlot>
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        size(70, 120).syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            VARIANTS,
            { index ->
                pickupUpgradeWrapper.filterType = PickupUpgradeWrapper.FilterType.entries[index]
                slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_PICKUP_UPGRADE_PICKUP_TYPE) {
                    it.writeEnumValue(pickupUpgradeWrapper.filterType)
                }
            },
            pickupUpgradeWrapper.filterType.ordinal
        )
            .size(20, 20)
            .leftRel(0.2F)
            .top(36)

        val slotGroup = SlotGroupWidget().debugName("pickup_filters")
        slotGroup.coverChildren().leftRel(0.5f).top(60)

        panel.pickupFilterStackHandler.delegatedStackHandler.delegated = pickupUpgradeWrapper::filterItems
        panel.pickupFilterStackHandler.syncToSlot(slotIndex)

        filterSlots = mutableListOf()
        for (i in 0 until 9) {
            val slot = ItemSlot().syncHandler(PICKUP_FILTER_SYNC_HANDLER, i).pos(i % 3 * 18, i / 3 * 18)

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        child(filterTypeButton)
        child(slotGroup)

        child(TextWidget(IKey.lang("gui.pickup_settings".asTranslationKey())).leftRel(0.85f).topRelAnchor(0.125f, 0.5f))
    }

    override fun getIngredientSlots(): List<ItemSlot> =
        filterSlots

    override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }
}