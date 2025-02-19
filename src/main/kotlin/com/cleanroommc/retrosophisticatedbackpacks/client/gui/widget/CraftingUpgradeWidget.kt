package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget

class CraftingUpgradeWidget() : ExpandedTabWidget(4) {
    companion object {
        private const val SLOT_SIZE = 18
    }

    private val craftingMatrix: Array<ItemSlot>
    private val craftingResult: ItemSlot

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

        val craftingResult = SlotGroupWidget().debugName("crafting_result")
        craftingResult.flex().coverChildren().leftRel(0.5F).top(120)

        this@CraftingUpgradeWidget.craftingResult = ItemSlot().syncHandler("crafting_result", 0).debugName("crafting_result_slot")
        craftingResult.child(this@CraftingUpgradeWidget.craftingResult)

        child(craftingResult)
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }
}