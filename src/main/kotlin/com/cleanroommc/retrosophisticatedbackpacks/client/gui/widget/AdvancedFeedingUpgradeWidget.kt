package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedFeedingUpgradeWidget(
    syncManager: PanelSyncManager,
    slotIndex: Int,
    private val advWrapper: AdvancedFeedingUpgradeWrapper
) : ExpandedTabWidget(5, ItemStack(Items.advancedFeedingUpgrade)) {
    companion object {
        private val HUNGER_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.complete_hunger".asTranslationKey()),
                RSBTextures.COMPLETE_HUNGER_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.half_hunger".asTranslationKey()),
                RSBTextures.HALF_HUNGER_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.immediate_hunger".asTranslationKey()),
                RSBTextures.IMMEDIATE_HUNGER_ICON
            ),
        )

        private val HEART_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_health".asTranslationKey()),
                RSBTextures.HALF_HEART_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.consider_health".asTranslationKey()),
                RSBTextures.IGNORE_HALF_HEART_ICON
            ),
        )
    }

    val hungerButtonWidget: CyclicVariantButtonWidget
    val heartButtonWidget: CyclicVariantButtonWidget
    val filterWidget: AdvancedFilterWidget

    init {
        size(100, 180)

        hungerButtonWidget = CyclicVariantButtonWidget(HUNGER_VARIANTS, advWrapper.hungerFeedingStrategy.ordinal) {
            advWrapper.hungerFeedingStrategy = AdvancedFeedingUpgradeWrapper.FeedingStrategy.Hunger.entries[it]
            markOnlyPropertyChanged()
            updateWrapper()
        }
            .left(7)
            .top(36)

        heartButtonWidget = CyclicVariantButtonWidget(HEART_VARIANTS, advWrapper.healthFeedingStrategy.ordinal) {
            advWrapper.healthFeedingStrategy = AdvancedFeedingUpgradeWrapper.FeedingStrategy.HEALTH.entries[it]
            markOnlyPropertyChanged()
            updateWrapper()
        }
            .left(29)
            .top(36)

        filterWidget = AdvancedFilterWidget(syncManager, slotIndex, advWrapper, "adv_feeding_filter")
            .leftRel(0.5f)
            .top(58)

        child(hungerButtonWidget)
            .child(heartButtonWidget)
            .child(filterWidget)
            .child(
                TextWidget(IKey.lang("gui.advanced_feeding_settings".asTranslationKey()))
                    .size(60, 20)
                    .leftRel(0.85f)
                    .topRelAnchor(0.11f, 0.5f)
            )
    }

    fun updateWrapper() {
        filterWidget.slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ADVANCED_FEEDING) {
            it.writeEnumValue(advWrapper.hungerFeedingStrategy)
            it.writeEnumValue(advWrapper.healthFeedingStrategy)
        }
    }
}