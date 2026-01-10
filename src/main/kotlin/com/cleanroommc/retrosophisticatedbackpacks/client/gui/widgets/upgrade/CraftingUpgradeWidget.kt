package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.BigItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.item.ItemStack

class CraftingUpgradeWidget(slotIndex: Int, wrapper: CraftingUpgradeWrapper) :
    ExpandedUpgradeTabWidget<CraftingUpgradeWrapper>(
        slotIndex,
        wrapper,
        4,
        ItemStack(Items.craftingUpgrade),
        wrapper.settingsLangKey
    ) {
    companion object {
        private const val SLOT_SIZE = 18

        private val CRAFTING_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.craft_into_backpack".asTranslationKey()),
                RSBTextures.LEFT_ARROW_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.craft_into_player_inventory".asTranslationKey()),
                RSBTextures.DOWN_ARROW_ICON
            )
        )
    }

    private val craftingMatrix: Array<ItemSlot>
    private val craftingResult: ItemSlot

    init {
        size(80, 155)

        val craftingTypeButtonWidget = CyclicVariantButtonWidget(
            CRAFTING_TYPE_VARIANTS,
            this@CraftingUpgradeWidget.wrapper.craftingDestination.ordinal,
            iconOffset = 2,
            iconSize = 16,
        ) {
            val nextCraftDestination = CraftingUpgradeWrapper.CraftingDestination.entries[it]

            this@CraftingUpgradeWidget.wrapper.craftingDestination = nextCraftDestination
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_CRAFTING_DESTINATION) {
                it.writeEnumValue(nextCraftDestination)
            }
        }.name("craft_destination_button").left(12).top(28)

        child(craftingTypeButtonWidget)

        val craftingMatrixSlotGroupsWidget = SlotGroupWidget().name("crafting_matrix_$slotIndex").disableSortButtons()
        craftingMatrixSlotGroupsWidget.flex().coverChildren().leftRel(0.5F).top(52)

        craftingMatrix = Array(9) {
            val itemSlot =
                ItemSlot().syncHandler("crafting_matrix_$slotIndex", it).pos(it % 3 * SLOT_SIZE, it / 3 * SLOT_SIZE)
                    .name("crafting_slot_$it")

            craftingMatrixSlotGroupsWidget.child(itemSlot)
            itemSlot
        }

        child(craftingMatrixSlotGroupsWidget)

        val craftingResult = SlotGroupWidget().name("crafting_result_$slotIndex").disableSortButtons()
        craftingResult.flex().coverChildren().leftRel(0.5F).top(126)

        this.craftingResult =
            BigItemSlot().syncHandler("crafting_result_$slotIndex", 0).name("crafting_result_slot")
        craftingResult.child(this.craftingResult)

        child(craftingResult)
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)

        context?.let {
            RSBTextures.CRAFTING_ARROW.draw(context, 32, 106, 16, 16, widgetTheme.getThemeOrDefault())
        }
    }

}
