package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedFeedingUpgradeWidget(
    slotIndex: Int,
    wrapper: AdvancedFeedingUpgradeWrapper
) : AdvancedExpandedTabWidget<AdvancedFeedingUpgradeWrapper>(
    slotIndex,
    wrapper,
    ItemStack(Items.advancedFeedingUpgrade),
    "gui.advanced_feeding_settings".asTranslationKey(),
    coveredTabSize = 6,
    filterSyncKey = "adv_feeding_filter"
) {
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
                IKey.lang("gui.consider_health".asTranslationKey()),
                RSBTextures.HALF_HEART_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_health".asTranslationKey()),
                RSBTextures.IGNORE_HALF_HEART_ICON
            ),
        )
    }

    val hungerButtonWidget: CyclicVariantButtonWidget
    val heartButtonWidget: CyclicVariantButtonWidget

    init {
        hungerButtonWidget = CyclicVariantButtonWidget(HUNGER_VARIANTS, wrapper.hungerFeedingStrategy.ordinal) {
            wrapper.hungerFeedingStrategy = AdvancedFeedingUpgradeWrapper.FeedingStrategy.Hunger.entries[it]
            updateWrapper()
        }

        heartButtonWidget = CyclicVariantButtonWidget(HEART_VARIANTS, wrapper.healthFeedingStrategy.ordinal) {
            wrapper.healthFeedingStrategy = AdvancedFeedingUpgradeWrapper.FeedingStrategy.HEALTH.entries[it]
            updateWrapper()
        }

        startingRow
            .leftRel(0.5f)
            .height(20)
            .childPadding(2)
            .child(hungerButtonWidget)
            .child(heartButtonWidget)
    }

    fun updateWrapper() {
        filterWidget.slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ADVANCED_FEEDING) {
            it.writeEnumValue(wrapper.hungerFeedingStrategy)
            it.writeEnumValue(wrapper.healthFeedingStrategy)
        }
    }
}