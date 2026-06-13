package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class SortingSettingWidget(
    private val panel: BackpackPanel,
    private val parentTabWidget: TabWidget
) : ExpandedTabWidget(
    2,
    RSBTextures.NO_SORT_ICON,
    "gui.sorting_settings".asTranslationKey(),
    width = 75,
    tabHeight = 48,
    expandDirection = ExpandDirection.RIGHT
) {
    private val lockAllButton: ButtonWidget<*> = ButtonWidget()
        .pos(3, 24)
        .size(18)
        .overlay(RSBTextures.ALL_FOUR_SLOT_ICON)
        .onMousePressed {
            if (it == 0) {
                val wrapper: BackpackWrapper = panel.backpackWrapper

                for (i in 0 until wrapper.backpackInventorySize()) {
                    wrapper.setSlotLocked(i, true)
                }

                panel.backpackSlotSyncHandlers.forEach { syncHandler ->
                    syncHandler.syncToServer(BackpackSlotSH.UPDATE_SET_SLOT_LOCK)
                }

                Utils.invalidateSortingContext()

                return@onMousePressed true
            }

            false
        }
        .tooltipStatic {
            it.addLine(IKey.lang("gui.lock_all_sort".asTranslationKey()))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    private val unlockAllButton: ButtonWidget<*> = ButtonWidget()
        .pos(21, 24)
        .size(18)
        .overlay(RSBTextures.NONE_FOUR_SLOT_ICON)
        .onMousePressed {
            if (it == 0) {
                val wrapper = panel.backpackWrapper

                for (i in 0 until wrapper.backpackInventorySize()) {
                    wrapper.setSlotLocked(i, false)
                }

                panel.backpackSlotSyncHandlers.forEach { syncHandler ->
                    syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_SLOT_LOCK)
                }

                Utils.invalidateSortingContext()
                return@onMousePressed true
            }

            false
        }
        .tooltipStatic {
            it.addLine(IKey.lang("gui.unlock_all_sort".asTranslationKey()))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    init {
        child(lockAllButton)
            .child(unlockAllButton)
    }

    override fun updateTabState() {
        panel.openSortingSettings(parentTabWidget, !parentTabWidget.showExpanded)
    }
}
