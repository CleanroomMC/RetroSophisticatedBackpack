package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.AdvancedPickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.value.sync.UpgradeSlotSH

class AdvancedPickupUpgradeWidget(
    panel: BackpackPanel,
    slotIndex: Int,
    private val advWrapper: AdvancedPickupUpgradeWrapper
) : ExpandedTabWidget(4) {
    companion object {
        private val MATCH_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_item".asTranslationKey()),
                RSBTextures.BY_ITEM_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_mod_id".asTranslationKey()),
                RSBTextures.BY_MOD_ID_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_ore_dict".asTranslationKey()),
                RSBTextures.CONSIDER_ORE_DICT_ICON
            ),
        )

        private val IGNORE_DURABILITY_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.consider_durability".asTranslationKey()),
                RSBTextures.CONSIDER_DURABILITY_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_durability".asTranslationKey()),
                RSBTextures.IGNORE_DURABILITY_ICON
            ),
        )

        private val IGNORE_NBT_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.consider_nbt".asTranslationKey()),
                RSBTextures.CONSIDER_NBT_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_nbt".asTranslationKey()),
                RSBTextures.IGNORE_NBT_ICON
            ),
        )
    }

    private val filterTypeButton: CyclicVariantButtonWidget
    private val matchTypeButton: CyclicVariantButtonWidget
    private val ignoreDurabilityButton: CyclicVariantButtonWidget
    private val ignoreNBTButton: CyclicVariantButtonWidget
    private val filterSlots: List<ItemSlot>
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        size(100, 150).syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            PickupUpgradeWidget.FILTER_TYPE_VARIANTS,
            true,
            advWrapper.filterType.ordinal
        ) { index ->
            advWrapper.filterType = PickupUpgradeWrapper.FilterType.entries[index]
            updatePickupWrapper()
        }

        matchTypeButton = CyclicVariantButtonWidget(
            MATCH_TYPE_VARIANTS,
            true,
            advWrapper.matchType.ordinal
        ) {
            advWrapper.matchType = AdvancedPickupUpgradeWrapper.MatchType.entries[it]

            if (advWrapper.matchType == AdvancedPickupUpgradeWrapper.MatchType.ITEM) {
                ignoreNBTButton.inEffect = true
                ignoreDurabilityButton.inEffect = true
            } else {
                ignoreNBTButton.inEffect = false
                ignoreDurabilityButton.inEffect = false
            }

            updatePickupWrapper()
        }

        val inEffect = advWrapper.matchType == AdvancedPickupUpgradeWrapper.MatchType.ITEM

        ignoreDurabilityButton = CyclicVariantButtonWidget(
            IGNORE_DURABILITY_VARIANTS,
            inEffect,
            if (advWrapper.ignoreDurability) 1 else 0
        ) {
            advWrapper.ignoreDurability = it == 1
            updatePickupWrapper()
        }

        ignoreNBTButton = CyclicVariantButtonWidget(
            IGNORE_NBT_VARIANTS,
            inEffect,
            if (advWrapper.ignoreNBT) 1 else 0
        ) {
            advWrapper.ignoreNBT = it == 1
            updatePickupWrapper()
        }

        val row = Row()
            .leftRelOffset(0.5f, 1)
            .size(88, 20)
            .top(36)
            .childPadding(2)
            .child(filterTypeButton)
            .child(matchTypeButton)
            .child(ignoreDurabilityButton)
            .child(ignoreNBTButton)
            .debugName("button_list")

        val slotGroup = SlotGroupWidget().debugName("adv_pickup_filters")
        slotGroup.coverChildren().leftRel(0.5f).top(60)

        val filterSlots = mutableListOf<ItemSlot>()

        for (i in 0 until 16) {
            val slot = ItemSlot().syncHandler("adv_pickup_filter_$slotIndex", i).pos(i % 4 * 18, i / 4 * 18)

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        this.filterSlots = filterSlots

        child(row)
            .child(slotGroup)
            .child(
                TextWidget(IKey.lang("gui.advanced_pickup_settings".asTranslationKey())).leftRel(0.85f)
                    .topRelAnchor(0.125f, 0.5f)
            )
    }

    private fun updatePickupWrapper() {
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ADVANCED_PICKUP_UPGRADE_TYPE) {
            it.writeEnumValue(advWrapper.filterType)
            it.writeEnumValue(advWrapper.matchType)
            it.writeBoolean(advWrapper.ignoreDurability)
            it.writeBoolean(advWrapper.ignoreNBT)
        }
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