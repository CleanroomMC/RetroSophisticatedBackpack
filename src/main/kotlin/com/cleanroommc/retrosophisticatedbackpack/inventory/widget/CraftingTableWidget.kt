package com.cleanroommc.retrosophisticatedbackpack.inventory.widget

import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpack.inventory.BackpackContainer
import net.minecraftforge.items.wrapper.InvWrapper

class CraftingTableWidget() : ExpandedTabWidget() {
    companion object {
        private const val SLOT_SIZE = 18
    }

    private val craftingMatrix: Array<ItemSlot>

    init {
        size(70, 150)

        val craftingMatrixSlotGroupsWidget = SlotGroupWidget().debugName("crafting_matrix")
        craftingMatrixSlotGroupsWidget.flex().coverChildren().leftRel(0.5F).top(36)

        craftingMatrix = Array(9) {
            val itemSlot = ItemSlot().syncHandler("crafting", it).pos(it % 3 * SLOT_SIZE, it / 3 * SLOT_SIZE)
                .debugName("crafting_slot_$it")

            craftingMatrixSlotGroupsWidget.child(itemSlot)
            itemSlot
        }

        child(craftingMatrixSlotGroupsWidget)
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawBackground(context, widgetTheme)

        TAB_TEXTURE.draw(context, 0, 0, flex.area.width, flex.area.height, widgetTheme)
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)
    }
}