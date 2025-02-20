package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedPickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class AdvancedPickupUpgradeWidget(
    syncManager: PanelSyncManager,
    slotIndex: Int,
    advWrapper: AdvancedPickupUpgradeWrapper
) : ExpandedTabWidget(4) {
    val filterWidget: AdvanceFilterWidget

    init {
        size(100, 150)

        filterWidget = AdvanceFilterWidget(syncManager, slotIndex, advWrapper)
            .leftRel(0.5f)
            .top(36)

        child(filterWidget)
            .child(
                TextWidget(IKey.lang("gui.advanced_pickup_settings".asTranslationKey()))
                    .size(60, 20)
                    .leftRel(0.85f)
                    .topRelAnchor(0.125f, 0.5f)
            )
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }
}