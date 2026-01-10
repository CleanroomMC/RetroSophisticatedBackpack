package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackSettingPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class MemorySettingWidget(
    private val panel: BackpackPanel,
    private val settingPanel: BackpackSettingPanel,
    private val parentTabWidget: TabWidget
) : ExpandedTabWidget(
    2,
    RSBTextures.BRAIN_ICON,
    "gui.memory_settings".asTranslationKey(),
    width = 75,
    expandDirection = ExpandDirection.LEFT
) {
    companion object {
        private val RESPECT_NBT_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_nbt".asTranslationKey()),
                RSBTextures.IGNORE_NBT_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_nbt".asTranslationKey()),
                RSBTextures.MATCH_NBT_ICON
            )
        )
    }

    private val buttonRow: Row = Row()
        .leftRel(0.5f)
        .height(20)
        .coverChildrenWidth()
        .childPadding(2) as Row

    private val memorizeAllButton: ButtonWidget<*> = ButtonWidget()
        .size(20)
        .overlay(RSBTextures.ALL_FOUR_SLOT_ICON)
        .onMousePressed {
            if (it == 0) {
                val wrapper = panel.backpackWrapper

                for (i in 0 until wrapper.backpackInventorySize()) {
                    wrapper.setMemoryStack(i, panel.shouldMemorizeRespectNBT)
                }

                panel.backpackSlotSyncHandlers.forEach { syncHandler ->
                    syncHandler.syncToServer(BackpackSlotSH.UPDATE_SET_MEMORY_STACK) {
                        it.writeBoolean(panel.shouldMemorizeRespectNBT)
                    }
                }

                Utils.invalidateSortingContext()
                return@onMousePressed true
            }

            false
        }
        .tooltipStatic {
            it.addLine(IKey.lang("gui.memorize_all".asTranslationKey()))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    private val unmemorizeAllButton: ButtonWidget<*> = ButtonWidget()
        .size(20)
        .overlay(RSBTextures.NONE_FOUR_SLOT_ICON)
        .onMousePressed {
            if (it == 0) {
                val wrapper = panel.backpackWrapper

                for (i in 0 until wrapper.backpackInventorySize()) {
                    wrapper.unsetMemoryStack(i)
                }

                panel.backpackSlotSyncHandlers.forEach { syncHandler ->
                    syncHandler.syncToServer(BackpackSlotSH.UPDATE_UNSET_MEMORY_STACK)
                }

                Utils.invalidateSortingContext()
                return@onMousePressed true
            }

            false
        }
        .tooltipStatic {
            it.addLine(IKey.lang("gui.unmemorize_all".asTranslationKey()))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }
    private val respectNBTButton: CyclicVariantButtonWidget = CyclicVariantButtonWidget(
        RESPECT_NBT_VARIANTS
    ) {
        this@MemorySettingWidget.panel.shouldMemorizeRespectNBT = it != 0
    }

    init {
        buttonRow.top(28)
            .child(memorizeAllButton)
            .child(unmemorizeAllButton)
            .child(respectNBTButton)

        child(buttonRow)
    }

    fun isRespectNBT(): Boolean =
        respectNBTButton.index != 0

    override fun updateTabState() {
        parentTabWidget.showExpanded = !parentTabWidget.showExpanded
        panel.isMemorySettingTabOpened = parentTabWidget.showExpanded
        settingPanel.updateTabState(0)
    }
}
