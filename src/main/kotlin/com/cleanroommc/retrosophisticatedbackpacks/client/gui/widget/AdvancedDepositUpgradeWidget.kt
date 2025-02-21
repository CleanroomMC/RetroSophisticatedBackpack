package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedDepositUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedDepositUpgradeWidget(
    syncManager: PanelSyncManager,
    slotIndex: Int,
    advWrapper: AdvancedDepositUpgradeWrapper
) : ExpandedTabWidget(4, ItemStack(Items.advancedDepositUpgrade)) {
    val filterWidget: AdvancedFilterWidget

    init {
        size(100, 150)

        filterWidget = AdvancedFilterWidget(syncManager, slotIndex, advWrapper)
            .leftRel(0.5f)
            .top(36)

        child(filterWidget)
            .child(
                TextWidget(IKey.lang("gui.advanced_deposit_settings".asTranslationKey()))
                    .size(60, 20)
                    .leftRel(0.85f)
                    .topRelAnchor(0.125f, 0.5f)
            )
    }
}