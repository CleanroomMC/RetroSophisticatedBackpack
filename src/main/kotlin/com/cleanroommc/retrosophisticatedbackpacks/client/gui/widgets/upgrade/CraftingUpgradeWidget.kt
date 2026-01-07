package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class CraftingUpgradeWidget(slotIndex: Int, wrap: CraftingUpgradeWrapper) :
    ExpandedUpgradeTabWidget<CraftingUpgradeWrapper>(slotIndex, wrap, 4, ItemStack(Items.craftingUpgrade), "") {
    companion object {
        private const val SLOT_SIZE = 18
        private val CRAFTING_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.sort_by_name".asTranslationKey()),
                RSBTextures.SMALL_A_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.sort_by_mod_id".asTranslationKey()),
                RSBTextures.SMALL_M_ICON
            )
        )

    }

    private val craftingMatrix: Array<ItemSlot>
    private val craftingResult: ItemSlot

    init {
        size(70, 150)

        val craftingTypeButtonWidget = CyclicVariantButtonWidget(
            CRAFTING_TYPE_VARIANTS,
            wrapper.craftingDestination.ordinal,
            iconSize = 12
        ) {
            val nextCraftDestination = CraftingUpgradeWrapper.CraftingDestination.entries[it]

            wrapper.craftingDestination = nextCraftDestination
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_CRAFTING_SHIFT) {
                it.writeEnumValue(nextCraftDestination)
            }
        }.leftRel(0.7F).top(12)
        child(craftingTypeButtonWidget)

        val craftingMatrixSlotGroupsWidget = SlotGroupWidget().name("crafting_matrix_$slotIndex")
        craftingMatrixSlotGroupsWidget.flex().coverChildren().leftRel(0.5F).top(36)

        craftingMatrix = Array(9) {
            val itemSlot = ItemSlot().syncHandler("crafting_matrix_$slotIndex", it).pos(it % 3 * SLOT_SIZE, it / 3 * SLOT_SIZE)
                .name("crafting_slot_$it")

            craftingMatrixSlotGroupsWidget.child(itemSlot)
            itemSlot
        }

        child(craftingMatrixSlotGroupsWidget)

        val craftingResult = SlotGroupWidget().name("crafting_result_$slotIndex")
        craftingResult.flex().coverChildren().leftRel(0.5F).top(120)

        this@CraftingUpgradeWidget.craftingResult = ItemSlot().syncHandler("crafting_result_$slotIndex", 0).name("crafting_result_slot")
        craftingResult.child(this@CraftingUpgradeWidget.craftingResult)

        child(craftingResult)
    }
}