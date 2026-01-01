package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import net.minecraft.item.ItemStack

class CraftingUpgradeWidget(slotIndex: Int, override val wrapper: CraftingUpgradeWrapper) :
    ExpandedUpgradeTabWidget<CraftingUpgradeWrapper>(slotIndex, 4, ItemStack(Items.craftingUpgrade), "") {
    companion object {
        private const val SLOT_SIZE = 18
    }

//    private val craftingMatrix: Array<ItemSlot>
//    private val craftingResult: ItemSlot

    init {
        size(70, 150)

//        val craftingMatrixSlotGroupsWidget = SlotGroupWidget().name("crafting_matrix")
//        craftingMatrixSlotGroupsWidget.flex().coverChildren().leftRel(0.5F).top(36)
//
//        craftingMatrix = Array(9) {
//            val itemSlot = ItemSlot().syncHandler("crafting", it).pos(it % 3 * SLOT_SIZE, it / 3 * SLOT_SIZE)
//                .name("crafting_slot_$it")
//
//            craftingMatrixSlotGroupsWidget.child(itemSlot)
//            itemSlot
//        }
//
//        child(craftingMatrixSlotGroupsWidget)
//
//        val craftingResult = SlotGroupWidget().name("crafting_result")
//        craftingResult.flex().coverChildren().leftRel(0.5F).top(120)
//
//        this@CraftingUpgradeWidget.craftingResult = ItemSlot().syncHandler("crafting_result", 0).name("crafting_result_slot")
//        craftingResult.child(this@CraftingUpgradeWidget.craftingResult)
//
//        child(craftingResult)

        child(IKey.str("UNFINISHED").asWidget().posRel(0.5F, 0.5F))
    }
}