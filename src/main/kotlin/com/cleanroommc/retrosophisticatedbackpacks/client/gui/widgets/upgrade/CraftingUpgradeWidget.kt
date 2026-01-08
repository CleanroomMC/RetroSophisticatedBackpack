package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CraftingCyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.BigItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import net.minecraft.item.ItemStack
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

class CraftingUpgradeWidget(slotIndex: Int, wrap: CraftingUpgradeWrapper) :
    ExpandedUpgradeTabWidget<CraftingUpgradeWrapper>(slotIndex, wrap, 4, ItemStack(Items.craftingUpgrade), "") {
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
        private val CRAFTING_ARROW = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls.png")
            .imageSize(256, 256)
            .xy(97, 209, 16, 16)
            .build()


    }

    private val craftingMatrix: Array<ItemSlot>
    private val craftingResult: ItemSlot

    init {
        size(70, 150)

        val craftingTypeButtonWidget = CraftingCyclicVariantButtonWidget(
            CRAFTING_TYPE_VARIANTS,
            wrapper.craftingDestination.ordinal,
            iconSize = 16
        ) {
            val nextCraftDestination = CraftingUpgradeWrapper.CraftingDestination.entries[it]

            wrapper.craftingDestination = nextCraftDestination
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_CRAFTING_SHIFT) {
                it.writeEnumValue(nextCraftDestination)
            }
        }.left(8).top(24)

        child(craftingTypeButtonWidget)

        val craftingMatrixSlotGroupsWidget = SlotGroupWidget().name("crafting_matrix_$slotIndex")
        craftingMatrixSlotGroupsWidget.flex().coverChildren().leftRel(0.5F).top(46)

        craftingMatrix = Array(9) {
            val itemSlot = ItemSlot().syncHandler("crafting_matrix_$slotIndex", it).pos(it % 3 * SLOT_SIZE, it / 3 * SLOT_SIZE)
                .name("crafting_slot_$it")

            craftingMatrixSlotGroupsWidget.child(itemSlot)
            itemSlot
        }

        child(craftingMatrixSlotGroupsWidget)

        val craftingResult = SlotGroupWidget().name("crafting_result_$slotIndex")
        craftingResult.flex().coverChildren().leftRel(0.5F).top(120)

        this@CraftingUpgradeWidget.craftingResult = BigItemSlot().syncHandler("crafting_result_$slotIndex", 0).name("crafting_result_slot")
        craftingResult.child(this@CraftingUpgradeWidget.craftingResult)

        child(craftingResult)
    }
    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)

        context?.let {
            CRAFTING_ARROW.draw(context, 27, 100, 16, 16, widgetTheme.getThemeOrDefault())
        }
    }

}