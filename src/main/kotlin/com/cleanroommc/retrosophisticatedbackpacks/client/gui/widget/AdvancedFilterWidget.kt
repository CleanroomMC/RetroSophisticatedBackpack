package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.drawable.text.TextRenderer
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widget.WidgetTree
import com.cleanroommc.modularui.widgets.*
import com.cleanroommc.modularui.widgets.layout.Column
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvanceFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.drawable.Outline
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraftforge.oredict.OreDictionary

class AdvancedFilterWidget(
    private val syncManager: PanelSyncManager,
    slotIndex: Int,
    private val filterableWrapper: IAdvanceFilterable,
    syncKey: String = "adv_common_filter",
) : ParentWidget<AdvancedFilterWidget>() {
    companion object {
        private val FILTER_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.whitelist".asTranslationKey()), RSBTextures.CHECK_ICON),
            CyclicVariantButtonWidget.Variant(IKey.lang("gui.blacklist".asTranslationKey()), RSBTextures.CROSS_ICON),
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
    private val filterSlots: List<ItemSlot>

    private val oreDictTextField: TextFieldWidget
    private val oreDictList: OreDictRegexListWidget

    private var focusedOreDictEntry: OreDictEntryWidget? = null
    var slotSyncHandler: UpgradeSlotSH? = null
        private set

    init {
        coverChildren().syncHandler("upgrades", slotIndex)

        filterTypeButton = CyclicVariantButtonWidget(
            FILTER_TYPE_VARIANTS,
            filterableWrapper.filterType.ordinal
        ) { index ->
            filterableWrapper.filterType = IBasicFilterable.FilterType.entries[index]
            updateWrapper()
        }

        matchTypeButton = CyclicVariantButtonWidget(
            MATCH_TYPE_VARIANTS,
            filterableWrapper.matchType.ordinal
        ) {
            filterableWrapper.matchType = IAdvanceFilterable.MatchType.entries[it]
            updateWrapper()
        }

        val inEffect = filterableWrapper.matchType == IAdvanceFilterable.MatchType.ITEM

        ignoreDurabilityButton = CyclicVariantButtonWidget(
            IGNORE_DURABILITY_VARIANTS,
            if (filterableWrapper.ignoreDurability) 1 else 0
        ) {
            filterableWrapper.ignoreDurability = it == 1
            updateWrapper()
        }
        ignoreDurabilityButton.inEffect = inEffect

        ignoreNBTButton = CyclicVariantButtonWidget(
            IGNORE_NBT_VARIANTS,
            if (filterableWrapper.ignoreNBT) 1 else 0
        ) {
            filterableWrapper.ignoreNBT = it == 1
            updateWrapper()
        }
        ignoreDurabilityButton.inEffect = inEffect

        // Buttons
        val buttonRow = Row()
            .leftRelOffset(0.5f, 1)
            .size(88, 20)
            .childPadding(2)

        val itemBasedConfigButtonRow = Row()
            .childPadding(2)
            .size(44, 20)
            .left(44)
            .child(ignoreDurabilityButton)
            .child(ignoreNBTButton)
            .setEnabledIf { filterableWrapper.matchType == IAdvanceFilterable.MatchType.ITEM }
            .debugName("item_based_button_list")

        val addOreDictEntryButton = ButtonWidget()
            .size(20, 20)
            .overlay(RSBTextures.ADD_ICON)
            .onMousePressed {
                val oreName = oreDictTextField.text

                if (oreName.isBlank())
                    return@onMousePressed false

                filterableWrapper.oreDictEntries.add(oreName)
                oreDictList.child(OreDictEntryWidget(this, oreName, 77))
                updateWrapper()
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

                filterableWrapper.oreDictEntries.remove(focusedOreDictEntry.text)
                oreDictList.removeChild(focusedOreDictEntry)
                updateWrapper()
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
            .setEnabledIf { filterableWrapper.matchType == IAdvanceFilterable.MatchType.ORE_DICT }
            .debugName("ore_dict_based_config_buttons")

        buttonRow
            .child(filterTypeButton)
            .child(matchTypeButton)
            .child(itemBasedConfigButtonRow)
            .child(oreDictBasedConfigButtonRow)
            .debugName("button_list")

        // Item-based configuration widgets
        val slotGroup = SlotGroupWidget().debugName("${syncKey}s")
        slotGroup.coverChildren().leftRel(0.5f)

        filterSlots = mutableListOf<ItemSlot>()

        for (i in 0 until 16) {
            val slot = ItemSlot().syncHandler("${syncKey}_$slotIndex", i).pos(i % 4 * 18, i / 4 * 18)

            filterSlots.add(slot)
            slotGroup.child(slot)
        }

        itemBasedConfigurationGroup = Column()
            .size(88, 85)
            .leftRel(0.5f)
            .top(24)
            .child(slotGroup)
            .setEnabledIf { filterableWrapper.matchType != IAdvanceFilterable.MatchType.ORE_DICT }
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
            .size(82, 65)

        for (entry in filterableWrapper.oreDictEntries)
            oreDictList.child(OreDictEntryWidget(this, entry, 77))

        oreDictBasedConfigurationGroup = Column()
            .size(88, 85)
            .leftRel(0.5f)
            .top(24)
            .child(oreDictList)
            .child(oreDictTextField)
            .setEnabledIf { filterableWrapper.matchType == IAdvanceFilterable.MatchType.ORE_DICT }
            .debugName("ore_dict_based_config_group") as Column

        child(buttonRow)
            .child(itemBasedConfigurationGroup)
            .child(oreDictBasedConfigurationGroup)
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

    override fun isValidSyncHandler(syncHandler: SyncHandler?): Boolean {
        if (syncHandler is UpgradeSlotSH)
            slotSyncHandler = syncHandler
        return slotSyncHandler != null
    }

    private class OreDictRegexListWidget() : ListWidget<OreDictEntryWidget, OreDictRegexListWidget>() {
        companion object {
            private val BACKGROUND_TILE_TEXTURE = UITexture.builder()
                .location(Tags.MOD_ID, "gui/gui_controls")
                .imageSize(256, 256)
                .uv(29, 146, 66, 56)
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
        TextWidget(IKey.str(" $text")), Interactable {
        companion object {
            private const val PAUSE_TIME = 60
        }

        private var line = TextRenderer.Line("", 0f)
        private var time: Long = 0
        private var scroll = 0
        private var hovering = false
        private var pauseTimer = 0
        private var selected = false

        init {
            size(width, 12)
                .left(1)
                .overlay(Outline(Color.WHITE.main))
                .color(Color.GREY.main)
                .shadow(true)

            tooltipBuilder {
                it.showUpTimer(5).pos(RichTooltip.Pos.NEXT_TO_MOUSE)

                if (line.width > area.width)
                    it.addLine(key)

                val stack = parent.syncManager.cursorItem

                if (!stack.isEmpty) {
                    val testMatched = OreDictionary
                        .getOreIDs(stack)
                        .map(OreDictionary::getOreName)
                        .any { Regex(text).matches(it) }

                    if (testMatched)
                        it.addLine(RSBTextures.CHECK_ICON)
                }
            }
        }

        override fun onMouseStartHover() {
            hovering = true

            if (!selected)
                overlay(Outline(Color.GREY.main))
        }

        override fun onMouseEndHover() {
            hovering = false
            scroll = 0
            time = 0
            markTooltipDirty()
            overlay(Outline(Color.WHITE.main))
        }

        override fun onUpdate() {
            super.onUpdate()

            if (pauseTimer > 0) {
                if (++pauseTimer == PAUSE_TIME) {
                    pauseTimer = if (scroll == 0) 0 else 1
                    scroll = 0
                }
                return
            }

            if (hovering && ++time % 2 == 0L && ++scroll == line.upperWidth() - area.width - 1) {
                pauseTimer = 1
            }
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

        override fun draw(context: ModularGuiContext?, widgetTheme: WidgetTheme?) {
            checkString()
            val renderer = TextRenderer.SHARED
            renderer.setColor(color)
            renderer.setAlignment(alignment, (area.w() + 1).toFloat(), area.h().toFloat())
            renderer.setShadow(isShadow)
            renderer.setPos(area.padding.left, area.padding.top + 2)
            renderer.setScale(scale)
            renderer.setSimulate(false)
            renderer.drawCut(line)
        }

        override fun drawOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
            if (!selected && !hovering)
                return

            val overlay = getCurrentOverlay(context.theme, widgetTheme)

            overlay.drawAtZero(context, area.width + 2, area.height + 2, widgetTheme)
        }

        private fun checkString() {
            val s = key.get()

            if (s != line.text) {
                TextRenderer.SHARED.setScale(scale)
                line = TextRenderer.SHARED.line(s)
                scroll = 0
                markTooltipDirty()
            }
        }
    }
}