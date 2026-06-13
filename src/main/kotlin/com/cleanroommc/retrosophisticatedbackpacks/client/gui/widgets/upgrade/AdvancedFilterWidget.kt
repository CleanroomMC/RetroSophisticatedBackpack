package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.value.ISyncOrValue
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.drawable.text.TextRenderer
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.modularui.widgets.ListWidget
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IContentsFilterable
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.drawable.Outline
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.setEnabledIfAndEnabled
import net.minecraftforge.oredict.OreDictionary

class AdvancedFilterWidget(
    slotIndex: Int,
    var filterableWrapper: IAdvancedFilterable,
    syncKey: String = "adv_common_filter",
) : ParentWidget<AdvancedFilterWidget>() {
    companion object {
        private val FILTER_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.whitelist".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.blacklist".asTranslationKey()), RSBTextures.CROSS_ICON),
        )
        private val CONTENTS_FILTER_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.allow".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.block".asTranslationKey()), RSBTextures.CROSS_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.match_backpack_contents".asTranslationKey()), RSBTextures.MATCH_BACKPACK_CONTENTS_ICON),
        )

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
                RSBTextures.MATCH_ORE_DICT_ICON
            ),
        )

        private val IGNORE_DURABILITY_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_durability".asTranslationKey()),
                RSBTextures.MATCH_DURABILITY_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.ignore_durability".asTranslationKey()),
                RSBTextures.IGNORE_DURABILITY_ICON
            ),
        )

        private val IGNORE_NBT_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.match_nbt".asTranslationKey()),
                RSBTextures.MATCH_NBT_ICON
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
    private val filterSlots: List<PhantomItemSlot>

    private val oreDictTextField: TextFieldWidget
    private val oreDictList: OreDictRegexListWidget

    private var focusedOreDictEntry: OreDictEntryWidget? = null
    var slotSyncHandler: UpgradeSlotSH? = null
        private set

    init {
        syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            if (filterableWrapper is IContentsFilterable) CONTENTS_FILTER_TYPE_VARIANTS else FILTER_TYPE_VARIANTS,
            filterButtonIndex(),
            iconOffset = 1,
            buttonWidth = 18,
            buttonHeight = 18,
            hasCustomTexture = true
        ) { index ->
            updateFilterType(index)
        }

        matchTypeButton = CyclicVariantButtonWidget(
            MATCH_TYPE_VARIANTS,
            filterableWrapper.matchType.ordinal,
            iconOffset = 1,
            buttonWidth = 18,
            buttonHeight = 18,
            hasCustomTexture = true
        ) {
            filterableWrapper.matchType = IAdvancedFilterable.MatchType.entries[it]
            if (filterableWrapper.matchType == IAdvancedFilterable.MatchType.ORE_DICT) {
                (filterableWrapper as? IContentsFilterable)?.let { contentsFilterable ->
                    if (contentsFilterable.contentsFilterType == IContentsFilterable.ContentsFilterType.STORAGE) {
                        contentsFilterable.contentsFilterType = IContentsFilterable.ContentsFilterType.ALLOW
                        filterTypeButton.selectIndex(contentsFilterable.contentsFilterType.ordinal)
                        syncContentsFilterType(contentsFilterable.contentsFilterType)
                    }
                }
            }
            updateWrapper()
        }

        val inEffect = filterableWrapper.matchType == IAdvancedFilterable.MatchType.ITEM

        ignoreDurabilityButton = CyclicVariantButtonWidget(
            IGNORE_DURABILITY_VARIANTS,
            if (filterableWrapper.ignoreDurability) 1 else 0,
            iconOffset = 1,
            buttonWidth = 18,
            buttonHeight = 18,
            hasCustomTexture = true
        ) {
            filterableWrapper.ignoreDurability = it == 1
            updateWrapper()
        }
        ignoreDurabilityButton.inEffect = inEffect

        ignoreNBTButton = CyclicVariantButtonWidget(
            IGNORE_NBT_VARIANTS,
            if (filterableWrapper.ignoreNBT) 1 else 0,
            iconOffset = 1,
            buttonWidth = 18,
            buttonHeight = 18,
            hasCustomTexture = true
        ) {
            filterableWrapper.ignoreNBT = it == 1
            updateWrapper()
        }
        ignoreNBTButton.inEffect = inEffect

        val filterSlotCount = filterableWrapper.filterItems.slots
        val slotsInRow = if (filterSlotCount > 0) filterableWrapper.slotsInRow.coerceIn(1, filterSlotCount) else 1
        val filterRows = if (filterSlotCount > 0) (filterSlotCount + slotsInRow - 1) / slotsInRow else 1
        val filterWidth = slotsInRow * 18
        val filterHeight = filterRows * 18

        // Buttons
        val buttonRow = Row()
            .size(filterWidth, 18)
            .childPadding(0)

        val itemBasedConfigButtonRow = Row()
            .childPadding(0)
            .size(36, 18)
            .left(36)
            .child(ignoreDurabilityButton)
            .child(ignoreNBTButton)
            .setEnabledIfAndEnabled { filterableWrapper.matchType == IAdvancedFilterable.MatchType.ITEM }
            .name("item_based_button_list")

        val addOreDictEntryButton = ButtonWidget()
            .size(18, 18)
            .overlay(RSBTextures.ADD_ICON)
            .onMousePressed {
                val oreName = oreDictTextField.text

                if (oreName.isBlank())
                    return@onMousePressed false

                filterableWrapper.oreDictEntries.add(oreName)
                oreDictList.child(OreDictEntryWidget(this, oreName, filterWidth - 11))
                oreDictTextField.text = ""
                updateWrapper()
                oreDictList.scheduleResize()

                true
            }
            .tooltipDynamic {
                it.addLine(IKey.lang("gui.add_ore_dict_entry".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
            .name("add_ore_dict_button")

        val removeOreDictEntryButton = ButtonWidget()
            .size(18, 18)
            .overlay(RSBTextures.REMOVE_ICON)
            .onMousePressed {
                val focusedOreDictEntry = focusedOreDictEntry

                if (focusedOreDictEntry == null)
                    return@onMousePressed false

                filterableWrapper.oreDictEntries.remove(focusedOreDictEntry.text)
                oreDictList.removeChild(focusedOreDictEntry)
                updateWrapper()
                oreDictList.scheduleResize()
                true
            }
            .tooltipDynamic {
                it.addLine(IKey.lang("gui.remove_ore_dict_entry".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

        val oreDictBasedConfigButtonRow = Row()
            .size(36, 18)
            .childPadding(0)
            .left(36)
            .child(addOreDictEntryButton)
            .child(removeOreDictEntryButton)
            .setEnabledIfAndEnabled { filterableWrapper.matchType == IAdvancedFilterable.MatchType.ORE_DICT }
            .name("ore_dict_based_config_buttons")

        buttonRow
            .child(filterTypeButton)
            .child(matchTypeButton)
            .child(itemBasedConfigButtonRow)
            .child(oreDictBasedConfigButtonRow)
            .name("button_list")

        // Item-based configuration widgets
        val slotGroup = SlotGroupWidget().name("${syncKey}s")
        slotGroup.coverChildren()
        slotGroup.disableSortButtons()
        slotGroup.setEnabledIfAndEnabled {
            (filterableWrapper as? IContentsFilterable)?.contentsFilterType != IContentsFilterable.ContentsFilterType.STORAGE
        }
        filterSlots = mutableListOf<PhantomItemSlot>()

        for (i in 0 until filterSlotCount) {
            val slot =
                PhantomItemSlot().syncHandler("${syncKey}_$slotIndex", i).pos(i % slotsInRow * 18, i / slotsInRow * 18) as PhantomItemSlot

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        itemBasedConfigurationGroup = Column()
            .size(filterWidth, filterHeight)
            .top(21)
            .child(slotGroup)
            .setEnabledIfAndEnabled { filterableWrapper.matchType != IAdvancedFilterable.MatchType.ORE_DICT }
            .name("item_based_config_group") as Column

        // Ore-dict-based configuration widgets
        oreDictTextField = TextFieldWidget()
            .size(filterWidth, 15)
            .leftRel(0.5f)
            .bottom(3)
            .tooltipDynamic {
                val stack = panel.context.mc.player.inventory.itemStack

                if (!stack.isEmpty) {
                    val oreDicts = OreDictionary.getOreIDs(stack)
                        .map(OreDictionary::getOreName)

                    it.addLine(IKey.lang("gui.ore_dict_list_entries".asTranslationKey()))
                    // FIXME: Is there a better way to add a separator instead of relying on stretched texture?
                    it.addLine(RSBTextures.REMOVE_ICON)

                    if (oreDicts.isNotEmpty()) {
                        for (oreDictName in oreDicts)
                            it.addLine(IKey.str(oreDictName).style(IKey.GRAY))
                    } else {
                        it.addLine(IKey.lang("gui.none".asTranslationKey()).style(IKey.GRAY))
                    }
                } else
                    it.addLine(IKey.lang("gui.ore_dict_input_help".asTranslationKey()))
                        .addLine(RSBTextures.REMOVE_ICON)
                        .addLine(IKey.lang("gui.ore_dict_input_help.pro_tip".asTranslationKey()))
                        .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
            .tooltipAutoUpdate(true)

        oreDictList = OreDictRegexListWidget()
            .size(filterWidth - 6, maxOf(18, filterHeight - 20))

        for (entry in filterableWrapper.oreDictEntries)
            oreDictList.child(OreDictEntryWidget(this, entry, filterWidth - 11))

        oreDictBasedConfigurationGroup = Column()
            .size(filterWidth, filterHeight)
            .top(21)
            .child(oreDictList)
            .child(oreDictTextField)
            .setEnabledIfAndEnabled { filterableWrapper.matchType == IAdvancedFilterable.MatchType.ORE_DICT }
            .name("ore_dict_based_config_group") as Column

        child(buttonRow)
            .child(itemBasedConfigurationGroup)
            .child(oreDictBasedConfigurationGroup)
    }

    private fun filterButtonIndex(): Int =
        (filterableWrapper as? IContentsFilterable)?.contentsFilterType?.ordinal ?: filterableWrapper.filterType.ordinal

    private fun updateFilterType(index: Int) {
        val contentsFilterable = filterableWrapper as? IContentsFilterable
        if (contentsFilterable != null) {
            var filterType = IContentsFilterable.ContentsFilterType.entries[index]
            if (filterableWrapper.matchType == IAdvancedFilterable.MatchType.ORE_DICT &&
                filterType == IContentsFilterable.ContentsFilterType.STORAGE
            ) {
                filterType = filterType.next()
                filterTypeButton.selectIndex(filterType.ordinal)
            }
            contentsFilterable.contentsFilterType = filterType
            syncContentsFilterType(filterType)
            return
        }

        filterableWrapper.filterType = IBasicFilterable.FilterType.entries[index]
        updateWrapper()
    }

    private fun syncContentsFilterType(filterType: IContentsFilterable.ContentsFilterType) {
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_CONTENTS_FILTERABLE) {
            it.writeEnumValue(filterType)
        }
    }

    private fun updateWrapper() {
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ADVANCED_FILTERABLE) {
            it.writeEnumValue(filterableWrapper.filterType)
            it.writeEnumValue(filterableWrapper.matchType)
            it.writeBoolean(filterableWrapper.ignoreDurability)
            it.writeBoolean(filterableWrapper.ignoreNBT)

            it.writeInt(filterableWrapper.oreDictEntries.size)

            for (entry in filterableWrapper.oreDictEntries) {
                it.writeString(entry)
            }
        }
    }

    override fun isValidSyncOrValue(syncHandler: ISyncOrValue): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }

    private class OreDictRegexListWidget() : ListWidget<OreDictEntryWidget, OreDictRegexListWidget>() {
        companion object {
            private val BACKGROUND_TILE_TEXTURE = UITexture.builder()
                .location(Tags.MOD_ID, "gui/gui_controls")
                .imageSize(256, 256)
                .xy(29, 146, 66, 56)
                .adaptable(1)
                .tiled()
                .build()
        }

        init {
            background(BACKGROUND_TILE_TEXTURE)
        }

        fun removeChild(widget: OreDictEntryWidget): Boolean =
            remove(widget)
    }

    private class OreDictEntryWidget(val parent: AdvancedFilterWidget, val text: String, width: Int) :
        TextWidget<OreDictEntryWidget>(IKey.str(" $text")), Interactable {
        private val outline = Outline(Color.WHITE.main)
        private var line = TextRenderer.Line("", 0f)
        private var selected = false

        init {
            size(width, 12)
                .color(Color.GREY.main)
                .shadow(true)

            tooltipDynamic {
                it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)

                if (line.width > area.width)
                    it.addLine(key)

                val stack = panel.context.mc.player.inventory.itemStack

                if (!stack.isEmpty) {
                    val testMatched = OreDictionary
                        .getOreIDs(stack)
                        .map(OreDictionary::getOreName)
                        .any { oreDictName -> Regex(text).matches(oreDictName) }

                    if (testMatched)
                        it.addLine(RSBTextures.CHECK_ICON.asIcon().width(9))
                    else
                        it.addLine(RSBTextures.CROSS_ICON.asIcon().width(9))
                }
            }
        }

        override fun onMouseStartHover() {
            super.onMouseStartHover()
            markTooltipDirty()
        }

        override fun onMouseEndHover() {
            super.onMouseEndHover()
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            for (child in parent.oreDictList.children) {
                if (child == this)
                    continue
                (child as OreDictEntryWidget).selected = false
            }

            if (selected) {
                selected = false
                parent.focusedOreDictEntry = null
            } else {
                selected = true
                parent.focusedOreDictEntry = this
            }

            return Interactable.Result.SUCCESS
        }

        override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
            if (!selected && !isHovering)
                return
            context?.let {
                if (selected) outline.color = Color.WHITE.main
                else outline.color = Color.GREY.main

                outline.drawAtZero(context, area.width + 2, area.height + 2, widgetTheme.getThemeOrDefault())
            }
        }
    }
}
