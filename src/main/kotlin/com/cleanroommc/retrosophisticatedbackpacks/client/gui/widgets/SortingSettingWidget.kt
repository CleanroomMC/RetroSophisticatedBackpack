package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackSettingPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class SortingSettingWidget(
    private val panel: BackpackPanel,
    private val settingPanel: BackpackSettingPanel,
    private val parentTabWidget: TabWidget
) : ExpandedTabWidget(
    2,
    RSBTextures.NO_SORT_ICON,
    "gui.sorting_settings".asTranslationKey(),
    width = 75,
    expandDirection = ExpandDirection.LEFT
) {
    private val buttonRow: Row = Row()
        .leftRel(0.5f)
        .height(20)
        .coverChildrenWidth()
        .childPadding(2) as Row

    private val lockAllButton: ButtonWidget<*> = ButtonWidget()
        .size(20)
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

                return@onMousePressed true
            }

            false
        }
        .tooltipStatic {
            it.addLine(IKey.lang("gui.lock_all_sort".asTranslationKey()))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    private val unlockAllButton: ButtonWidget<*> = ButtonWidget()
        .size(20)
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

                return@onMousePressed true
            }

            false
        }
        .tooltipStatic {
            it.addLine(IKey.lang("gui.unlock_all_sort".asTranslationKey()))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    init {
        buttonRow.top(28)
            .child(lockAllButton)
            .child(unlockAllButton)

        child(buttonRow)
    }

    override fun updateTabState() {
        parentTabWidget.showExpanded = !parentTabWidget.showExpanded
        panel.isSortingSettingTabOpened = parentTabWidget.showExpanded
        settingPanel.updateTabState(1)
    }
}