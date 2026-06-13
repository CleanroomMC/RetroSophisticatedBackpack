package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.widget.ScrollWidget
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.BackpackSlot

class BackpackInventoryScrollWidget(panel: BackpackPanel) :
    ScrollWidget<BackpackInventoryScrollWidget>(VerticalScrollData(false, SCROLLBAR_WIDTH)) {
    private val panel = panel
    private var lastSearchLayoutVersion = panel.searchLayoutVersion

    init {
        val scrollData = scrollArea.scrollY
        scrollData.scrollSize = panel.colSize * SLOT_SIZE
        scrollData.scrollSpeed = SLOT_SIZE

        size(panel.backpackSlotsWidth + panel.inventoryScrollbarWidth, panel.visibleColSize * SLOT_SIZE)

        child(createSlots(panel, panel.colSize))
    }

    override fun onUpdate() {
        super.onUpdate()
        val scrollData = scrollArea.scrollY
        scrollData.scrollSize = panel.searchDisplayRows() * SLOT_SIZE
        if (lastSearchLayoutVersion != panel.searchLayoutVersion) {
            lastSearchLayoutVersion = panel.searchLayoutVersion
            scrollData.scrollTo(scrollArea, 0)
        } else {
            scrollData.clamp(scrollArea)
        }
    }

    companion object {
        const val SCROLLBAR_WIDTH = 6
        private const val SLOT_SIZE = 18

        fun createSlots(panel: BackpackPanel, visibleRows: Int): SlotGroupWidget {
            val slots = SlotGroupWidget().name("backpack_inventory").disableSortButtons()
            slots.size(panel.backpackSlotsWidth, visibleRows * SLOT_SIZE)
            slots.child(BackpackSlotBackgroundWidget(panel, visibleRows))
            for (i in 0 until panel.backpackWrapper.backpackInventorySize()) {
                slots.child(
                    BackpackSlot(panel, panel.backpackWrapper)
                        .syncHandler("backpack", i)
                        .pos(i % panel.rowSize * SLOT_SIZE, i / panel.rowSize * SLOT_SIZE)
                        .name("slot_$i")
                )
            }
            slots.child(
                MobCatcherInventoryControlWidget(panel)
                    .pos(0, 0)
                    .name("mob_catcher_inventory_control")
                    .setEnabledIf { !panel.isSettingMode && !panel.isSearchViewActive() }
            )
            return slots
        }
    }

    private class BackpackSlotBackgroundWidget(private val panel: BackpackPanel, rows: Int) :
        Widget<BackpackSlotBackgroundWidget>() {
        init {
            size(panel.backpackSlotsWidth, rows * SLOT_SIZE)
        }

        override fun canHover(): Boolean = false

        override fun canHoverThrough(): Boolean = true

        override fun canClickThrough(): Boolean = true

        override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
            val theme = widgetTheme.theme
            panel.updateSearchLayout()
            for (i in 0 until panel.searchVisibleSlotCount()) {
                RSBTextures.SLOT_BACKGROUND.draw(
                    context,
                    i % panel.rowSize * SLOT_SIZE,
                    i / panel.rowSize * SLOT_SIZE,
                    SLOT_SIZE,
                    SLOT_SIZE,
                    theme
                )
            }
        }
    }
}
