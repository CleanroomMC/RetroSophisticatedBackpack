package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFilterUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedFilterUpgradeWidget(
    slotIndex: Int,
    wrapper: AdvancedFilterUpgradeWrapper
) : AdvancedExpandedTabWidget<AdvancedFilterUpgradeWrapper>(
    slotIndex,
    wrapper,
    ItemStack(Items.advancedFilterUpgrade),
    "gui.advanced_filter_settings".asTranslationKey(),
    coveredTabSize = 6
) {
    val filterWayButtonWidget: CyclicVariantButtonWidget

    init {
        filterWayButtonWidget =
            CyclicVariantButtonWidget(FilterUpgradeWidget.FILTER_WAY_VARIANTS, wrapper.filterWay.ordinal) {
                wrapper.filterWay = IFilterUpgrade.FilterWayType.entries[it]
                updateWrapper()
            }

        startingRow
            .leftRel(0.5f)
            .height(20)
            .childPadding(2)
            .child(filterWayButtonWidget)
    }

    fun updateWrapper() {
        filterWidget.slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_FILTER_WAY) {
            it.writeEnumValue(wrapper.filterWay)
        }
    }
}