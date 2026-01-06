package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.FilterUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class FilterUpgradeWidget(
    slotIndex: Int,
    wrap: FilterUpgradeWrapper
) : BasicExpandedTabWidget<FilterUpgradeWrapper>(
    slotIndex,
    wrap,
    ItemStack(Items.filterUpgrade),
    "gui.filter_settings".asTranslationKey(),
    coveredTabSize = 5
) {
    companion object {
        internal val FILTER_WAY_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.input_output".asTranslationKey()),
                RSBTextures.IN_OUT_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.input".asTranslationKey()),
                RSBTextures.IN_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.output".asTranslationKey()),
                RSBTextures.OUT_ICON
            ),
        )
    }

    val filterWayButtonWidget: CyclicVariantButtonWidget

    init {
        filterWayButtonWidget = CyclicVariantButtonWidget(FILTER_WAY_VARIANTS, wrapper.filterWay.ordinal) {
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