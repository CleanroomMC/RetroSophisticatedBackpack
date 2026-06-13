package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class MemorySettingWidget(
    private val panel: BackpackPanel,
    private val parentTabWidget: TabWidget
) : ExpandedTabWidget(
    2,
    RSBTextures.BRAIN_ICON,
    "gui.memory_settings".asTranslationKey(),
    width = 75,
    tabHeight = 48,
    expandDirection = ExpandDirection.RIGHT
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

    private val memorizeAllButton: ButtonWidget<*> = ButtonWidget()
        .pos(3, 24)
        .size(18)
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
        .pos(21, 24)
        .size(18)
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
    ) { this@MemorySettingWidget.panel.shouldMemorizeRespectNBT = it != 0 }
        .pos(39, 24)
        .size(18)

    init {
        child(memorizeAllButton)
            .child(unmemorizeAllButton)
            .child(respectNBTButton)
    }

    fun isRespectNBT(): Boolean =
        respectNBTButton.index != 0

    override fun updateTabState() {
        panel.openMemorySettings(parentTabWidget, !parentTabWidget.showExpanded)
    }
}
