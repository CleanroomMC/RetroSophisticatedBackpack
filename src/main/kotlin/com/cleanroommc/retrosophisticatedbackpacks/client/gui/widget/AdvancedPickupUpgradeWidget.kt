package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.Rectangle
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widget.WidgetTree
import com.cleanroommc.modularui.widgets.*
import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.AdvancedPickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.PickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.value.sync.UpgradeSlotSH
import net.minecraft.util.text.TextFormatting

class AdvancedPickupUpgradeWidget(
    slotIndex: Int,
    private val advWrapper: AdvancedPickupUpgradeWrapper
) : ExpandedTabWidget(4) {
    companion object {
        private val MATCH_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_item".asTranslationKey()),
                RSBTextures.BY_ITEM_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_mod_id".asTranslationKey()),
                RSBTextures.BY_MOD_ID_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_ore_dict".asTranslationKey()),
                RSBTextures.CONSIDER_ORE_DICT_ICON
            ),
        )

        private val IGNORE_DURABILITY_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.consider_durability".asTranslationKey()),
                RSBTextures.CONSIDER_DURABILITY_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_durability".asTranslationKey()),
                RSBTextures.IGNORE_DURABILITY_ICON
            ),
        )

        private val IGNORE_NBT_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.consider_nbt".asTranslationKey()),
                RSBTextures.CONSIDER_NBT_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_nbt".asTranslationKey()),
                RSBTextures.IGNORE_NBT_ICON
            ),
        )
    }

    private val filterTypeButton: CyclicVariantButtonWidget
    private val matchTypeButton: CyclicVariantButtonWidget
    private val ignoreDurabilityButton: CyclicVariantButtonWidget
    private val ignoreNBTButton: CyclicVariantButtonWidget

    private val itemBasedConfigurationGroup: Column
    private val oreDictBasedConfigurationGroup: Column
    private val filterSlots: List<ItemSlot>

    private val oreDictTextField: TextFieldWidget
    private val oreDictList: OreDictRegexListWidget

    private var focusedOreDictEntry: OreDictEntryWidget? = null
    private var slotSyncHandler: UpgradeSlotSH? = null

    init {
        size(100, 150).syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            PickupUpgradeWidget.FILTER_TYPE_VARIANTS,
            advWrapper.filterType.ordinal
        ) { index ->
            advWrapper.filterType = PickupUpgradeWrapper.FilterType.entries[index]
            updatePickupWrapper()
        }

        matchTypeButton = CyclicVariantButtonWidget(
            MATCH_TYPE_VARIANTS,
            advWrapper.matchType.ordinal
        ) {
            advWrapper.matchType = AdvancedPickupUpgradeWrapper.MatchType.entries[it]
            updatePickupWrapper()
        }

        val inEffect = advWrapper.matchType == AdvancedPickupUpgradeWrapper.MatchType.ITEM

        ignoreDurabilityButton = CyclicVariantButtonWidget(
            IGNORE_DURABILITY_VARIANTS,
            if (advWrapper.ignoreDurability) 1 else 0
        ) {
            advWrapper.ignoreDurability = it == 1
            updatePickupWrapper()
        }
        ignoreDurabilityButton.inEffect = inEffect

        ignoreNBTButton = CyclicVariantButtonWidget(
            IGNORE_NBT_VARIANTS,
            if (advWrapper.ignoreNBT) 1 else 0
        ) {
            advWrapper.ignoreNBT = it == 1
            updatePickupWrapper()
        }
        ignoreDurabilityButton.inEffect = inEffect

        // Buttons
        val buttonRow = Row()
            .leftRelOffset(0.5f, 1)
            .size(88, 20)
            .top(36)
            .childPadding(2)

        val itemBasedConfigButtonRow = Row()
            .childPadding(2)
            .size(44, 20)
            .left(44)
            .child(ignoreDurabilityButton)
            .child(ignoreNBTButton)
            .setEnabledIf { advWrapper.matchType == AdvancedPickupUpgradeWrapper.MatchType.ITEM }
            .debugName("item_based_button_list")

        val addOreDictEntryButton = ButtonWidget()
            .size(20, 20)
            .overlay(RSBTextures.ADD_ICON)
            .onMousePressed {
                val oreName = oreDictTextField.text

                if (oreName.isBlank())
                    return@onMousePressed false

                advWrapper.oreDictEntries.add(oreName)
                oreDictList.child(OreDictEntryWidget(oreName, 75))
                updatePickupWrapper()
                WidgetTree.resize(oreDictList)

                true
            }
            .tooltipDynamic {
                it.addLine(IKey.lang("gui.add_ore_dict_entry".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
            .debugName("add_ore_dict_button")

        val removeOreDictEntryButton = ButtonWidget()
            .size(20, 20)
            .overlay(RSBTextures.REMOVE_ICON)
            .onMousePressed {
                val focusedOreDictEntry = focusedOreDictEntry

                if (focusedOreDictEntry == null)
                    return@onMousePressed false

                advWrapper.oreDictEntries.remove(focusedOreDictEntry.text)
                oreDictList.removeChild(focusedOreDictEntry)
                updatePickupWrapper()
                WidgetTree.resize(oreDictList)

                true
            }
            .tooltipDynamic {
                it.addLine(IKey.lang("gui.remove_ore_dict_entry".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

        val oreDictBasedConfigButtonRow = Row()
            .size(44, 20)
            .childPadding(2)
            .left(44)
            .child(addOreDictEntryButton)
            .child(removeOreDictEntryButton)
            .setEnabledIf { advWrapper.matchType == AdvancedPickupUpgradeWrapper.MatchType.ORE_DICT }
            .debugName("ore_dict_based_config_buttons")

        buttonRow
            .child(filterTypeButton)
            .child(matchTypeButton)
            .child(itemBasedConfigButtonRow)
            .child(oreDictBasedConfigButtonRow)
            .debugName("button_list")

        // Item-based configuration widgets
        val slotGroup = SlotGroupWidget().debugName("adv_pickup_filters")
        slotGroup.coverChildren().leftRel(0.5f)

        filterSlots = mutableListOf<ItemSlot>()

        for (i in 0 until 16) {
            val slot = ItemSlot().syncHandler("adv_pickup_filter_$slotIndex", i).pos(i % 4 * 18, i / 4 * 18)

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        itemBasedConfigurationGroup = Column()
            .size(88, 85)
            .leftRel(0.5f)
            .top(60)
            .child(slotGroup)
            .setEnabledIf { advWrapper.matchType != AdvancedPickupUpgradeWrapper.MatchType.ORE_DICT }
            .debugName("item_based_config_group") as Column

        // Ore-dict-based configuration widgets
        oreDictTextField = TextFieldWidget()
            .size(88, 15)
            .leftRel(0.5f)
            .bottom(3)
            .tooltipDynamic {
                it.addLine(IKey.lang("gui.ore_dict_input_help".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

        oreDictList = OreDictRegexListWidget()
            .size(80, 65)

        oreDictList.background(Rectangle().setColor(Color.BLACK.main))

        for (entry in advWrapper.oreDictEntries)
            oreDictList.child(OreDictEntryWidget(entry, 75))

        oreDictBasedConfigurationGroup = Column()
            .size(88, 85)
            .leftRel(0.5f)
            .top(60)
            .child(oreDictList)
            .child(oreDictTextField)
            .setEnabledIf { advWrapper.matchType == AdvancedPickupUpgradeWrapper.MatchType.ORE_DICT }
            .debugName("ore_dict_based_config_group") as Column

        child(buttonRow)
            .child(itemBasedConfigurationGroup)
            .child(oreDictBasedConfigurationGroup)
            .child(
                TextWidget(IKey.lang("gui.advanced_pickup_settings".asTranslationKey()))
                    .size(60, 20)
                    .leftRel(0.85f)
                    .topRelAnchor(0.125f, 0.5f)
            )
    }

    private fun updatePickupWrapper() {
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ADVANCED_PICKUP_UPGRADE_TYPE) {
            it.writeEnumValue(advWrapper.filterType)
            it.writeEnumValue(advWrapper.matchType)
            it.writeBoolean(advWrapper.ignoreDurability)
            it.writeBoolean(advWrapper.ignoreNBT)

            it.writeInt(advWrapper.oreDictEntries.size)

            for (entry in advWrapper.oreDictEntries) {
                it.writeString(entry)
            }
        }
    }

    override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    private class OreDictRegexListWidget() : ListWidget<OreDictEntryWidget, OreDictRegexListWidget>() {
        fun removeChild(widget: OreDictEntryWidget): Boolean =
            remove(widget)
    }

    private inner class OreDictEntryWidget(val text: String, width: Int) : TextWidget(IKey.str(text)), Interactable {
        var selected = false
            set(value) {
                if (value) key.style(TextFormatting.UNDERLINE)
                else key.removeStyle()

                field = value
            }

        init {
            size(width, 10)
                .left(2)
                .color(Color.GREY.main)
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            for (child in oreDictList.children) {
                if (child == this)
                    continue
                (child as OreDictEntryWidget).selected = false
            }

            if (selected) {
                selected = false
                focusedOreDictEntry = null
            } else {
                selected = true
                focusedOreDictEntry = this
            }

            return Interactable.Result.SUCCESS
        }
    }
}