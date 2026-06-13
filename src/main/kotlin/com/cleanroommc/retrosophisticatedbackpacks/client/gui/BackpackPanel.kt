package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.layout.IViewportStack
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.drawable.text.StringKey
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.*
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.NoBackgroundItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade.*
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.CraftingSlotInfo
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.LockedPlayerSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularUpgradeSlot
import com.cleanroommc.retrosophisticatedbackpacks.config.ClientConfig
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.ceilDiv
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.setEnabledIfAndEnabled
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.items.wrapper.PlayerInvWrapper
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper
import java.util.Locale
import kotlin.math.min

class BackpackPanel(
    internal val player: EntityPlayer,
    internal val tileEntity: BackpackTileEntity?,
    internal val syncManager: PanelSyncManager,
    internal val backpackWrapper: BackpackWrapper,
    private val openedBackpackSlotIndex: Int? = null,
    private val backpackName: String? = null
) : ModularPanel("backpack_gui") {
    companion object {
        private const val SLOT_SIZE = 18
        private const val HEIGHT_WITHOUT_STORAGE_SLOTS = 114
        private const val STORAGE_INVENTORY_X = 7
        private const val STORAGE_INVENTORY_Y = 17
        private const val SEARCH_BOX_MIN_WIDTH = 10
        private const val SEARCH_BOX_HEIGHT = 10
        private const val SEARCH_BOX_UNFOCUSED_COLOR = 0xBBBBBB
        private const val SEARCH_BOX_ANIMATION_MS = 200L
        internal const val DISABLED_SLOT_X_POS = -2000
        internal const val VISIBLE_BACKPACK_ROWS = 5
        internal const val INVENTORY_CONTROL_COLUMNS = 2
        private val SORT_TYPE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.sort_by_name".asTranslationKey()),
                RSBTextures.SMALL_A_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.sort_by_mod_id".asTranslationKey()),
                RSBTextures.SMALL_M_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.sort_by_count".asTranslationKey()),
                RSBTextures.SMALL_1_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.sort_by_ore_dict".asTranslationKey()),
                RSBTextures.SMALL_O_ICON
            )
        )

        internal fun defaultPanel(
            syncManager: PanelSyncManager,
            player: EntityPlayer,
            tileEntity: BackpackTileEntity?,
            wrapper: BackpackWrapper,
            width: Int,
            height: Int,
            backpackSlotIndex: Int? = null,
            backpackName: String? = null,
        ): BackpackPanel {
            val panel = BackpackPanel(player, tileEntity, syncManager, wrapper, backpackSlotIndex, backpackName)

            panel.background(IDrawable.EMPTY)
            syncManager.bindPlayerInventory(player)
            panel.recalculateLayout()
            panel.size(panel.panelWidth, panel.panelHeight)

            return panel
        }
    }

    val upgradeSlotWidgets = mutableListOf<ItemSlot>()
    var upgradeSlotGroupWidget = UpgradeSlotGroupWidget(this, backpackWrapper.upgradeSlotsSize())
        private set
    val tabWidgets = mutableListOf<TabWidget>()
    var tankInventoryControlCount = 0
        private set
    var batteryInventoryControlCount = 0
        private set
    var inventoryColumnsTaken = 0
        private set
    var backgroundRowSize = 0
        private set
    var rowSize = 0
        private set
    var colSize = 0
        private set
    var visibleColSize = 0
        private set
    var backpackSlotsWidth = 0
        private set
    var inventoryScrollbarWidth = 0
        private set
    var inventoryAreaWidth = 0
        private set
    var storageInventoryHeight = 0
        private set
    private var playerInventoryXOffset = 0
    private var storageBackgroundTexture: UITexture = RSBTextures.STORAGE_BACKGROUND_9
    val playerInventoryLabelX: Int
        get() = 8 + playerInventoryXOffset
    val playerInventoryLabelY: Int
        get() = panelHeight - 94
    private val playerInventorySlotsY: Int
        get() = playerInventoryLabelY + 11
    val panelWidth: Int
        get() = 14 + backgroundRowSize * SLOT_SIZE + inventoryScrollbarWidth
    val panelHeight: Int
        get() = HEIGHT_WITHOUT_STORAGE_SLOTS + visibleColSize * SLOT_SIZE

    val backpackSyncHandler: BackpackSH = BackpackSH(PlayerMainInvWrapper(player.inventory), backpackWrapper, tileEntity)
    val backpackSlotSyncHandlers: Array<BackpackSlotSH>
    val upgradeSlotSyncHandlers: Array<UpgradeSlotSH>
    val upgradeSlotGroups: Array<UpgradeSlotUpdateGroup>

    var isMemorySettingTabOpened: Boolean = false
    var shouldMemorizeRespectNBT: Boolean = false
    var isSortingSettingTabOpened: Boolean = false
    var isBackpackSettingTabOpened: Boolean = false
    var isItemDisplaySettingTabOpened: Boolean = false
    var currentItemDisplaySelectedSlot: Int = -1
        private set
    internal val isSlotSettingTabOpened: Boolean
        get() = isMemorySettingTabOpened || isSortingSettingTabOpened || isItemDisplaySettingTabOpened
    private lateinit var backpackSettingTabWidget: TabWidget
    private lateinit var memorySettingTabWidget: TabWidget
    private lateinit var sortingSettingTabWidget: TabWidget
    private lateinit var itemDisplaySettingTabWidget: TabWidget
    private var rebuildWidgetsQueued = false
    private var lastUpgradeStructureSignature = emptyList<String>()
    private var reopenBackpackQueued = false
    private var lastScaledHeight = 0
    private val searchSlotDisplayIndices = IntArray(backpackWrapper.backpackInventorySize()) { it }
    private var searchVisibleSlots = backpackWrapper.backpackInventorySize()
    private var lastSearchLayoutKey = ""
    var searchLayoutVersion = 0
        private set
    var isSettingMode: Boolean = false
        set(value) {
            if (field == value)
                return

            field = value
            if (value) {
                closeUpgradeTabs(syncToServer = true)
                tabWidgets.forEach { it.isEnabled = false }
                closeSettingTabs()
            } else {
                closeSettingTabs()
                updateUpgradeWidgets()
            }
            updateSearchLayout(force = true)
            scheduleResize()
        }

    private val searchTerms: List<String>
        get() = backpackWrapper.searchPhrase.trim()
            .split(Regex("\\s+"))
            .filter(String::isNotEmpty)

    override fun onUpdate() {
        super.onUpdate()
        refreshLayoutIfScreenHeightChanged()
        updateSearchLayout()
        if (!rebuildWidgetsQueued)
            queueRebuildIfUpgradeStructureChanged()
        if (reopenBackpackQueued) {
            reopenBackpackQueued = false
            rebuildWidgetsQueued = false
            upgradeSlotSyncHandlers.firstOrNull()?.syncToServer(UpgradeSlotSH.UPDATE_REOPEN_BACKPACK) {}
            return
        }
        if (rebuildWidgetsQueued) {
            rebuildWidgetsQueued = false
            rebuildWidgets()
        }
    }

    fun refreshUpgradeWidgetsAfterSlotChange() {
        if (!isValid)
            return

        if (!queueRebuildIfUpgradeStructureChanged() && !rebuildWidgetsQueued)
            updateUpgradeWidgets()
    }

    private fun queueRebuildIfUpgradeStructureChanged(): Boolean {
        if (!isValid)
            return false

        val signature = upgradeStructureSignature()
        if (signature == lastUpgradeStructureSignature)
            return false

        lastUpgradeStructureSignature = signature
        rebuildWidgetsQueued = true
        if (syncManager.isClient && lastUpgradeStructureSignature.isNotEmpty())
            reopenBackpackQueued = true
        return true
    }

    fun rebuildWidgets() {
        rebuildWidgetsQueued = false
        reopenBackpackQueued = false
        if (isValid) {
            removeAll()
        }

        recalculateLayout()
        size(panelWidth, panelHeight)
        updateSearchLayout(force = true)
        upgradeSlotWidgets.clear()
        tabWidgets.clear()
        upgradeSlotGroupWidget = UpgradeSlotGroupWidget(this, backpackWrapper.upgradeSlotsSize())
        currentItemDisplaySelectedSlot = -1

        addPlayerInventoryWidgets()
        addBackpackInventorySlots()
        addUpgradeSlots()
        addSettingTab()
        addUpgradeTabs()
        addTexts(player)
        addSortingButtons()
        addSearchBox()
        addTransferButtons()
        closeSettingTabs()
        lastUpgradeStructureSignature = upgradeStructureSignature()
        updateUpgradeWidgets()
        scheduleResize()
    }

    private fun upgradeStructureSignature(): List<String> =
        (0 until backpackWrapper.upgradeSlotsSize()).map { slotIndex ->
            val stack = backpackWrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
            if (stack.isEmpty) "empty"
            else stack.item.registryName?.toString() ?: stack.item.javaClass.name
        }

    private fun recalculateLayout() {
        tankInventoryControlCount = min(backpackWrapper.tankUpgradeSlots().size, 2)
        batteryInventoryControlCount = min(backpackWrapper.batteryUpgradeSlots().size, 1)
        inventoryColumnsTaken = (tankInventoryControlCount + batteryInventoryControlCount) * INVENTORY_CONTROL_COLUMNS
        backgroundRowSize = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
        rowSize = (backgroundRowSize - inventoryColumnsTaken).coerceAtLeast(1)
        colSize = backpackWrapper.backpackInventorySize().ceilDiv(rowSize)
        visibleColSize = min(colSize, maxVisibleBackpackRows())
        backpackSlotsWidth = rowSize * SLOT_SIZE
        inventoryScrollbarWidth = if (colSize > visibleColSize) BackpackInventoryScrollWidget.SCROLLBAR_WIDTH else 0
        inventoryAreaWidth = backgroundRowSize * SLOT_SIZE + inventoryScrollbarWidth
        storageInventoryHeight = visibleColSize * SLOT_SIZE
        playerInventoryXOffset =
            when {
                backgroundRowSize > 9 && inventoryScrollbarWidth > 0 -> 30
                backgroundRowSize > 9 -> 27
                inventoryScrollbarWidth > 0 -> 3
                else -> 0
            }
        storageBackgroundTexture =
            when {
                backgroundRowSize > 9 && inventoryScrollbarWidth > 0 -> RSBTextures.STORAGE_BACKGROUND_12_WIDER
                backgroundRowSize > 9 -> RSBTextures.STORAGE_BACKGROUND_12
                inventoryScrollbarWidth > 0 -> RSBTextures.STORAGE_BACKGROUND_9_WIDER
                else -> RSBTextures.STORAGE_BACKGROUND_9
            }
    }

    private fun refreshLayoutIfScreenHeightChanged() {
        val scaledHeight = ScaledResolution(Minecraft.getMinecraft()).scaledHeight
        if (scaledHeight == lastScaledHeight)
            return

        lastScaledHeight = scaledHeight
        rebuildWidgetsQueued = true
    }

    private fun maxVisibleBackpackRows(): Int =
        ((ScaledResolution(Minecraft.getMinecraft()).scaledHeight - HEIGHT_WITHOUT_STORAGE_SLOTS) / SLOT_SIZE)
            .coerceAtLeast(1)

    init {
        recalculateLayout()
        lastScaledHeight = ScaledResolution(Minecraft.getMinecraft()).scaledHeight
        syncManager.syncValue("backpack_wrapper", backpackSyncHandler)

        // Backpack slots
        backpackSlotSyncHandlers = Array(backpackWrapper.backpackInventorySize()) {
            val backpackSlot = ModularBackpackSlot(backpackWrapper, it).slotGroup("backpack_inventory")
            val syncHandler = BackpackSlotSH(backpackWrapper, backpackSlot)

            syncManager.syncValue("backpack", it, syncHandler)
            syncHandler
        }

        syncManager.registerSlotGroup(SlotGroup("backpack_inventory", rowSize, 100, true))

        // Upgrade slots
        upgradeSlotSyncHandlers = Array(backpackWrapper.upgradeSlotsSize()) {
            val upgradeSlot = ModularUpgradeSlot(
                this,
                backpackWrapper,
                it
            ).slotGroup("upgrade_inventory")
            val syncHandler = UpgradeSlotSH(upgradeSlot) {
                refreshUpgradeWidgetsAfterSlotChange()
            }
            upgradeSlot.changeListener { _, _, isClient, _ ->
                if (isClient)
                    refreshUpgradeWidgetsAfterSlotChange()
            }

            syncManager.syncValue("upgrades", it, syncHandler)
            syncHandler
        }

        syncManager.registerSlotGroup(SlotGroup("upgrade_inventory", 1, 99, true))

        // Upgrade slot inventory pre register
        upgradeSlotGroups = Array(backpackWrapper.upgradeSlotsSize()) {
            UpgradeSlotUpdateGroup(this, backpackWrapper, it)
        }

    }

    fun getBackpackContainer(): BackpackContainer {
        return syncManager.container as BackpackContainer
    }

    override fun onInit() {
        super.onInit()
        updateUpgradeWidgets()
    }

    // Currently only main hand slot will be locked if it's the backpack being opened
    internal fun modifyPlayerSlot(
        syncManager: PanelSyncManager,
        inventoryType: PlayerInventoryGuiData.InventoryType,
        slotIndex: Int,
        player: EntityPlayer
    ) {
        // Bauble slot does not exist in backpack screen
        if (inventoryType == PlayerInventoryGuiData.InventoryType.PLAYER_BAUBLES)
            return

        syncManager.itemSlot(
            "player",
            slotIndex,
            LockedPlayerSlot(PlayerInvWrapper(player.inventory), slotIndex)
                .slotGroup("player_inventory")
        )
    }

    internal fun addSortingButtons() {
        val rightAnchor = if (Loader.isModLoaded("bogosorter")) 30 else 7

        val sortTypeButton = CyclicVariantButtonWidget(
            SORT_TYPE_VARIANTS,
            backpackWrapper.sortType.ordinal,
            iconOffset = 0,
            iconSize = 12
        ) { mouseButton ->
            val nextSortType = SortType.entries[mouseButton]

            backpackSyncHandler.setSortType(nextSortType)
            backpackSyncHandler.syncToServer(BackpackSH.UPDATE_SET_SORT_TYPE) {
                it.writeEnumValue(nextSortType)
            }
        }
            .setEnabledIf {
                !isSettingMode
            }
            .top(4)
            .right(rightAnchor)
            .size(12)
        val sortButton = TransferButtonWidget(RSBTextures.SOLID_UP_ARROW_ICON, RSBTextures.SOLID_UP_ARROW_ICON)
            .top(4)
            .right(rightAnchor + 14)
            .size(12)
            .setEnabledIf {
                !isSettingMode
            }
            .onMousePressed { mouseButton ->
                if (mouseButton == 0) {
                    Interactable.playButtonClickSound()
                    BackpackInventoryHelper.sortInventory(backpackWrapper)
                    backpackSyncHandler.syncToServer(BackpackSH.UPDATE_SORT_INV) {
                        for (i in 0 until backpackWrapper.backpackInventorySize()) {
                            it.writeItemStack(backpackWrapper.getStackInSlot(i))
                        }
                    }
                    true
                } else false
            }
            .tooltipStatic {
                it.addLine(IKey.lang("gui.sort_inventory".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

        child(sortButton)
            .child(sortTypeButton)
    }

    internal fun addTransferButtons() {
        val transferButtonsShiftX = if (Loader.isModLoaded("bogosorter")) -23 else 0

        val transferToBackpackButton =
            TransferButtonWidget(RSBTextures.DOT_UP_ARROW_ICON, RSBTextures.SOLID_UP_ARROW_ICON)
                .top(playerInventoryLabelY - 2)
                .left(playerInventoryLabelX + 137 + transferButtonsShiftX)
                .size(12)
                .setEnabledIf {
                    !isSettingMode
                }
                .onMousePressed { mouseButton ->
                    if (mouseButton == 0) {
                        val transferMatched = !Interactable.hasShiftDown()

                        Interactable.playButtonClickSound()
                        backpackSyncHandler.transferToBackpack(transferMatched)
                        backpackSyncHandler.syncToServer(BackpackSH.UPDATE_TRANSFER_TO_BACKPACK_INV) {
                            it.writeBoolean(transferMatched)
                        }
                        true
                    } else false
                }
                .tooltipAutoUpdate(true)
                .tooltipDynamic {
                    if (Interactable.hasShiftDown()) {
                        it.addLine(IKey.lang("gui.transfer_to_backpack_inv".asTranslationKey()))
                    } else {
                        it.addLine(IKey.lang("gui.transfer_to_backpack_inv_matched_1".asTranslationKey()))
                            .addLine(
                                IKey.lang("gui.transfer_to_backpack_inv_matched_2".asTranslationKey()).style(IKey.GRAY)
                            )
                    }

                    it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
                }
        val transferToPlayerButton =
            TransferButtonWidget(RSBTextures.DOT_DOWN_ARROW_ICON, RSBTextures.SOLID_DOWN_ARROW_ICON)
                .top(playerInventoryLabelY - 2)
                .left(playerInventoryLabelX + 149 + transferButtonsShiftX)
                .size(12)
                .setEnabledIf {
                    !isSettingMode
                }
                .onMousePressed { mouseButton ->
                    if (mouseButton == 0) {
                        val transferMatched = !Interactable.hasShiftDown()

                        Interactable.playButtonClickSound()
                        backpackSyncHandler.transferToPlayerInventory(transferMatched)
                        backpackSyncHandler.syncToServer(BackpackSH.UPDATE_TRANSFER_TO_PLAYER_INV) {
                            it.writeBoolean(transferMatched)
                        }
                        true
                    } else false
                }
                .tooltipAutoUpdate(true)
                .tooltipDynamic {
                    if (Interactable.hasShiftDown()) {
                        it.addLine(IKey.lang("gui.transfer_to_player_inv".asTranslationKey()))
                    } else {
                        it.addLine(IKey.lang("gui.transfer_to_player_inv_matched_1".asTranslationKey()))
                            .addLine(
                                IKey.lang("gui.transfer_to_player_inv_matched_2".asTranslationKey()).style(IKey.GRAY)
                            )
                    }

                    it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
                }

        child(transferToPlayerButton)
            .child(transferToBackpackButton)
    }

    internal fun addSearchBox() {
        val rightAnchor = if (Loader.isModLoaded("bogosorter")) 30 else 7
        val sortButtonLeft = panelWidth - (rightAnchor + 14) - 12
        val left = 7
        val width = (sortButtonLeft - 1 - left).coerceAtLeast(SEARCH_BOX_MIN_WIDTH)
        if (width <= 0) {
            return
        }

        child(
            SearchBoxWidget()
                .top(5)
                .left(left)
                .size(width, SEARCH_BOX_HEIGHT)
                .setEnabledIf { !isSettingMode }
        )
    }

    internal fun addPlayerInventoryWidgets() {
        child(
            SlotGroupWidget.playerInventory(0, false) { _, _ ->
                NoBackgroundItemSlot()
            }
                .disableSortButtons()
                .pos(playerInventoryLabelX - 1, playerInventorySlotsY)
        )
    }

    internal fun hasSearchPhrase(): Boolean =
        searchTerms.isNotEmpty()

    internal fun isSearchViewActive(): Boolean =
        !isSettingMode && hasSearchPhrase()

    internal fun updateSearchLayout(force: Boolean = false) {
        val key = if (isSearchViewActive())
            "${isSettingMode}|${backpackWrapper.searchPhrase}|${searchInventorySignature()}"
        else "${isSettingMode}|${backpackWrapper.searchPhrase}|${backpackWrapper.backpackInventorySize()}"
        if (!force && key == lastSearchLayoutKey) {
            return
        }

        lastSearchLayoutKey = key
        searchVisibleSlots = 0

        if (!isSearchViewActive()) {
            for (slotIndex in searchSlotDisplayIndices.indices) {
                searchSlotDisplayIndices[slotIndex] = slotIndex
            }
            searchVisibleSlots = backpackWrapper.backpackInventorySize()
        } else {
            for (slotIndex in searchSlotDisplayIndices.indices) {
                val stack = backpackWrapper.getStackInSlot(slotIndex)
                searchSlotDisplayIndices[slotIndex] =
                    if (!backpackWrapper.isSlotBlockedByMobCatcher(slotIndex) && !stack.isEmpty && matchesSearch(stack))
                        searchVisibleSlots++
                    else DISABLED_SLOT_X_POS
            }
        }

        searchLayoutVersion++
    }

    internal fun isSearchSlotVisible(slotIndex: Int): Boolean =
        !isSearchViewActive() || searchSlotDisplayIndices.getOrNull(slotIndex)?.let { it >= 0 } == true

    internal fun searchSlotX(slotIndex: Int): Int {
        val displayIndex = searchSlotDisplayIndices.getOrNull(slotIndex) ?: return DISABLED_SLOT_X_POS
        return if (displayIndex < 0) DISABLED_SLOT_X_POS else displayIndex % rowSize * SLOT_SIZE
    }

    internal fun searchSlotY(slotIndex: Int): Int {
        val displayIndex = searchSlotDisplayIndices.getOrNull(slotIndex) ?: return 0
        return if (displayIndex < 0) 0 else displayIndex / rowSize * SLOT_SIZE
    }

    internal fun searchVisibleSlotCount(): Int =
        if (isSearchViewActive()) searchVisibleSlots else backpackWrapper.backpackInventorySize()

    internal fun searchDisplayRows(): Int =
        if (isSearchViewActive()) searchVisibleSlots.coerceAtLeast(1).ceilDiv(rowSize).coerceAtLeast(visibleColSize)
        else colSize

    private fun searchInventorySignature(): String =
        buildString {
            append(backpackWrapper.backpackInventorySize())
            for (slotIndex in 0 until backpackWrapper.backpackInventorySize()) {
                val stack = backpackWrapper.getStackInSlot(slotIndex)
                append('|')
                if (stack.isEmpty) {
                    append("empty")
                } else {
                    append(stack.item.registryName)
                        .append(':')
                        .append(stack.metadata)
                        .append(':')
                        .append(stack.count)
                }
                append(':')
                    .append(backpackWrapper.isSlotBlockedByMobCatcher(slotIndex))
            }
        }

    internal fun matchesSearch(stack: ItemStack): Boolean {
        val terms = searchTerms
        if (terms.isEmpty()) {
            return true
        }
        if (stack.isEmpty) {
            return false
        }

        val displayName = stack.displayName.lowercase(Locale.ROOT)
        val registryName = stack.item.registryName
        val modId = registryName?.namespace?.lowercase(Locale.ROOT) ?: ""
        val tooltipLines by lazy {
            stack.getTooltip(player, ITooltipFlag.TooltipFlags.NORMAL)
                .joinToString("\n") { TextFormatting.getTextWithoutFormattingCodes(it) ?: it }
                .lowercase(Locale.ROOT)
        }

        return terms.all { rawTerm ->
            val term = rawTerm.lowercase(Locale.ROOT)
            when {
                term.startsWith("@") -> modId.contains(term.drop(1))
                term.startsWith("#") -> tooltipLines.contains(term.drop(1))
                else -> displayName.contains(term)
            }
        }
    }

    private fun setSearchPhrase(phrase: String) {
        val trimmed = phrase.take(50)
        if (backpackWrapper.searchPhrase == trimmed) {
            return
        }
        backpackWrapper.searchPhrase = trimmed
        updateSearchLayout(force = true)
        backpackSyncHandler.syncToServer(BackpackSH.UPDATE_SEARCH_PHRASE) {
            it.writeString(trimmed)
        }
    }

    private inner class SearchBoxWidget : VanillaTextFieldWidget<SearchBoxWidget>(1, SEARCH_BOX_MIN_WIDTH - 2, 8) {
        private var lastSyncedText = backpackWrapper.searchPhrase
        private var lastFocusChangeTime = Minecraft.getSystemTime()
        private var currentWidth = -1
        private var currentTheme = WidgetTheme.getDefault().theme

        init {
            textField.setMaxStringLength(50)
            textField.setText(backpackWrapper.searchPhrase)
            textField.setTextColor(if (backpackWrapper.searchPhrase.isEmpty()) SEARCH_BOX_UNFOCUSED_COLOR else 0xFFFFFF)
            textField.setDisabledTextColour(SEARCH_BOX_UNFOCUSED_COLOR)
            tooltipBuilder {
                it.addLine(IKey.lang("gui.search".asTranslationKey()))
                    .addLine(IKey.lang("gui.search_detail".asTranslationKey()).style(IKey.GRAY))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
        }

        override fun isInside(stack: IViewportStack, mx: Int, my: Int, absolute: Boolean): Boolean {
            if (isSettingMode) {
                return false
            }
            val x = if (absolute) stack.unTransformX(mx.toFloat(), my.toFloat()) else mx
            val y = if (absolute) stack.unTransformY(mx.toFloat(), my.toFloat()) else my
            return y >= 0 && y < area.height && x >= visualX() && x < area.width
        }

        override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {}

        override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
            currentTheme = widgetTheme.theme
            if (isSettingMode) {
                return
            }
            GlStateManager.color(1f, 1f, 1f, 1f)
            val visualWidth = visualWidth()
            val visualX = visualX()
            Gui.drawRect(visualX, 0, visualX + visualWidth, area.height, 0xFF777777.toInt())
            if (!isFocused() && textField.text.isEmpty()) {
                RSBTextures.SEARCH_ICON.draw(
                    context,
                    visualX,
                    0,
                    SEARCH_BOX_MIN_WIDTH,
                    SEARCH_BOX_HEIGHT,
                    currentTheme
                )
            } else {
                drawTextField()
            }
            GlStateManager.color(1f, 1f, 1f, 1f)
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            if (isSettingMode) {
                return Interactable.Result.IGNORE
            }
            val mouseX = context.mouseX
            val mouseY = context.mouseY
            if (mouseY < 0 || mouseY >= area.height || mouseX < visualX() || mouseX >= area.width) {
                return Interactable.Result.IGNORE
            }
            if (mouseButton == 1) {
                onTextChanged("")
                return Interactable.Result.SUCCESS
            }
            if (mouseButton != 0) {
                return Interactable.Result.STOP
            }
            return super.onMousePressed(mouseButton)
        }

        override fun onUpdate() {
            super.onUpdate()
            if (isSettingMode) {
                if (isFocused()) {
                    context.removeFocus()
                }
                return
            }
            animateWidth()
            if (backpackWrapper.searchPhrase != lastSyncedText && backpackWrapper.searchPhrase != textField.text) {
                lastSyncedText = backpackWrapper.searchPhrase
                textField.setText(lastSyncedText)
            }
        }

        override fun onTextChanged(text: String) {
            val trimmed = text.take(50)
            if (textField.text != trimmed) {
                textField.setText(trimmed)
            }
            if (trimmed == lastSyncedText) {
                return
            }
            lastSyncedText = trimmed
            setSearchPhrase(lastSyncedText)
        }

        override fun onFocusChanged(focused: Boolean) {
            lastFocusChangeTime = Minecraft.getSystemTime()
            textField.setTextColor(if (focused) 0xFFFFFF else SEARCH_BOX_UNFOCUSED_COLOR)
        }

        private fun visualWidth(): Int {
            animateWidth()
            return currentWidth.coerceIn(SEARCH_BOX_MIN_WIDTH, area.width)
        }

        private fun visualX(): Int = area.width - visualWidth()

        override fun textFieldX(): Int = visualX() + 1

        override fun textFieldY(): Int = 1

        override fun textFieldWidth(): Int = visualWidth() - 6

        override fun textFieldHeight(): Int = SEARCH_BOX_HEIGHT

        override fun mouseXForTextField(): Int = context.mouseX

        override fun mouseYForTextField(): Int = context.mouseY

        private fun animateWidth() {
            val target = if (isFocused() || textField.text.isNotEmpty()) area.width else SEARCH_BOX_MIN_WIDTH
            if (currentWidth < 0) {
                currentWidth = target
                return
            }
            if (currentWidth == target) {
                return
            }
            val elapsed = (Minecraft.getSystemTime() - lastFocusChangeTime).coerceAtLeast(0L)
            val ratio = (elapsed.toFloat() / SEARCH_BOX_ANIMATION_MS).coerceIn(0f, 1f)
            val eased = if (ratio < 0.5f) 4f * ratio * ratio * ratio else 1f - Math.pow((-2f * ratio + 2f).toDouble(), 3.0).toFloat() / 2f
            currentWidth = if (target == area.width) {
                (SEARCH_BOX_MIN_WIDTH + (area.width - SEARCH_BOX_MIN_WIDTH) * eased).toInt()
            } else {
                (area.width - (area.width - SEARCH_BOX_MIN_WIDTH) * eased).toInt()
            }.coerceIn(SEARCH_BOX_MIN_WIDTH, area.width)
        }
    }

    internal fun addBackpackInventorySlots() {
        val inventoryArea = SlotGroupWidget().disableSortButtons()
            .size(inventoryAreaWidth, storageInventoryHeight)
            .pos(STORAGE_INVENTORY_X, STORAGE_INVENTORY_Y)
            .setEnabledIf { !isSettingMode || isSlotSettingTabOpened }

        inventoryArea.child(
            if (inventoryScrollbarWidth > 0) BackpackInventoryScrollWidget(this).pos(0, 0)
            else BackpackInventoryScrollWidget.createSlots(this, visibleColSize).pos(0, 0)
        )

        val tankSlots = backpackWrapper.tankUpgradeSlots().take(tankInventoryControlCount)
        var controlIndex = 0
        for ((index, slot) in tankSlots.withIndex()) {
            if (backpackWrapper.upgradeItemStackHandler.inventory[slot]
                    .getCapability(Capabilities.TANK_UPGRADE_CAPABILITY, null) == null
            ) continue
            inventoryArea.child(
                TankInventoryControlWidget(
                    upgradeSlotSyncHandlers[slot],
                    slot,
                    backpackWrapper,
                    storageInventoryHeight
                )
                    .pos(backpackSlotsWidth + inventoryScrollbarWidth + controlIndex * TankInventoryControlWidget.WIDTH, 0)
                    .name("tank_inventory_control_$slot")
                    .setEnabledIf { !isSettingMode && !isSearchViewActive() }
            )
            controlIndex++
        }

        val batterySlots = backpackWrapper.batteryUpgradeSlots().take(batteryInventoryControlCount)
        for (slot in batterySlots) {
            if (backpackWrapper.upgradeItemStackHandler.inventory[slot]
                    .getCapability(Capabilities.BATTERY_UPGRADE_CAPABILITY, null) == null
            ) continue
            inventoryArea.child(
                BatteryInventoryControlWidget(
                    slot,
                    backpackWrapper,
                    storageInventoryHeight
                )
                    .pos(backpackSlotsWidth + inventoryScrollbarWidth + controlIndex * BatteryInventoryControlWidget.WIDTH, 0)
                    .name("battery_inventory_control_$slot")
                    .setEnabledIf { !isSettingMode && !isSearchViewActive() }
            )
            controlIndex++
        }

        child(inventoryArea)
    }

    internal fun addUpgradeSlots() {
        upgradeSlotGroupWidget.name("upgrade_inventory")
        upgradeSlotGroupWidget.size(25, 13 + backpackWrapper.upgradeSlotsSize() * 16).left(-21)
        upgradeSlotGroupWidget.setEnabledIf { !isSettingMode }

        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val itemSlot = NoBackgroundItemSlot(RSBTextures.EMPTY_UPGRADE_SLOT)
                .syncHandler("upgrades", i)
                .pos(5, 5 + i * 16)
                .name("slot_${i}")

            upgradeSlotWidgets.add(itemSlot)
            upgradeSlotGroupWidget.child(itemSlot)
        }

        child(upgradeSlotGroupWidget)
    }

    internal fun addSettingTab() {
        val backToBackpackTab = BackToBackpackTabWidget()
            .setEnabledIfAndEnabled({ isSettingMode }, false)

        backpackSettingTabWidget = TabWidget(2).name("backpack_setting_tab")
        backpackSettingTabWidget.isEnabled = false
        backpackSettingTabWidget.expandedWidget = BackpackMainSettingsWidget(this, backpackSettingTabWidget)
        backpackSettingTabWidget.tabIcon = RSBTextures.BACKPACK_SETTINGS_ICON
        backpackSettingTabWidget.tooltipDynamic {
            it.clearText()
                .addLine(IKey.lang("gui.backpack_settings.tooltip".asTranslationKey()))
                .addLine(
                    IKey.lang(
                        if (backpackSettingTabWidget.showExpanded)
                            "gui.backpack_settings.tooltip_open_detail".asTranslationKey()
                        else "gui.backpack_settings.tooltip_detail".asTranslationKey()
                    ).style(IKey.GRAY)
                )
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

        sortingSettingTabWidget = TabWidget(3).name("sorting_setting_tab")
        sortingSettingTabWidget.isEnabled = false
        sortingSettingTabWidget.expandedWidget = SortingSettingWidget(this, sortingSettingTabWidget)
        sortingSettingTabWidget.tabIcon = RSBTextures.NO_SORT_ICON
        sortingSettingTabWidget.tooltipDynamic {
            it.clearText()
                .addLine(IKey.lang("gui.sorting_settings.tooltip".asTranslationKey()))
                .addLine(
                    IKey.lang(
                        if (sortingSettingTabWidget.showExpanded)
                            "gui.sorting_settings.tooltip_open_detail".asTranslationKey()
                        else "gui.sorting_settings.tooltip_detail".asTranslationKey()
                    ).style(IKey.GRAY)
                )
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

        memorySettingTabWidget = TabWidget(4).name("memory_setting_tab")
        memorySettingTabWidget.isEnabled = false
        memorySettingTabWidget.expandedWidget = MemorySettingWidget(this, memorySettingTabWidget)
        memorySettingTabWidget.tabIcon = RSBTextures.BRAIN_ICON
        memorySettingTabWidget.tooltipDynamic {
            it.clearText()
                .addLine(IKey.lang("gui.memory_settings.tooltip".asTranslationKey()))
                .addLine(
                    IKey.lang(
                        if (memorySettingTabWidget.showExpanded)
                            "gui.memory_settings.tooltip_open_detail".asTranslationKey()
                        else "gui.memory_settings.tooltip_detail".asTranslationKey()
                    ).style(IKey.GRAY)
                )
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

        itemDisplaySettingTabWidget = TabWidget(5).name("item_display_setting_tab")
        itemDisplaySettingTabWidget.isEnabled = false
        itemDisplaySettingTabWidget.expandedWidget = ItemDisplaySettingsWidget(this, itemDisplaySettingTabWidget)
        itemDisplaySettingTabWidget.tabIcon = RSBTextures.ITEM_DISPLAY_SETTINGS_ICON
        itemDisplaySettingTabWidget.tooltipDynamic {
            it.clearText()
                .addLine(IKey.lang("gui.item_display_settings.tooltip".asTranslationKey()))
                .addLine(
                    IKey.lang(
                        if (itemDisplaySettingTabWidget.showExpanded)
                            "gui.item_display_settings.tooltip_open_detail".asTranslationKey()
                        else "gui.item_display_settings.tooltip_detail".asTranslationKey()
                    ).style(IKey.GRAY)
                )
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

        child(SettingTabWidget().setEnabledIf { !isSettingMode })
        if (!Config.itemDisplayDisabled) {
            child(itemDisplaySettingTabWidget)
        }
        child(memorySettingTabWidget)
            .child(sortingSettingTabWidget)
            .child(backpackSettingTabWidget)
            .child(backToBackpackTab)
    }

    internal fun addUpgradeTabs() {
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val tab = TabWidget(i + 2).name("upgrade_tab_${i}")

            tab.isEnabled = false
            tabWidgets.add(tab)
        }

        // Allows most-top widget to be drawn on top of any other widgets
        for (tab in tabWidgets.asReversed()) {
            child(tab)
        }
    }

    internal fun addTexts(player: EntityPlayer) {
        val titleWidget = TextWidget(StringKey(backpackName ?: backpackWrapper.getDisplayName().formattedText))
            .pos(8, 6)
            .setEnabledIf { !isSettingMode }
        val settingsTitleWidget = IKey.lang("gui.settings".asTranslationKey()).asWidget()
            .pos(8, 6)
            .setEnabledIf { isSettingMode }
        settingsTitleWidget.isEnabled = false
        child(titleWidget)
        child(settingsTitleWidget)
        child(TextWidget(StringKey(player.inventory.displayName.formattedText)).pos(playerInventoryLabelX, playerInventoryLabelY))
    }

    fun openMemorySettings(tabWidget: TabWidget, open: Boolean) {
        if (!isSettingMode)
            return

        memorySettingTabWidget.showExpanded = open
        isMemorySettingTabOpened = open
        shouldMemorizeRespectNBT =
            open && (memorySettingTabWidget.expandedWidget as? MemorySettingWidget)?.isRespectNBT() == true

        if (open)
            closeOtherSettingTabs(memorySettingTabWidget)
        updateSettingTabEnabledStates(memorySettingTabWidget, open)
    }

    fun openSortingSettings(tabWidget: TabWidget, open: Boolean) {
        if (!isSettingMode)
            return

        sortingSettingTabWidget.showExpanded = open
        isSortingSettingTabOpened = open

        if (open)
            closeOtherSettingTabs(sortingSettingTabWidget)
        updateSettingTabEnabledStates(sortingSettingTabWidget, open)
    }

    fun openBackpackSettings(tabWidget: TabWidget, open: Boolean) {
        if (!isSettingMode)
            return

        backpackSettingTabWidget.showExpanded = open
        isBackpackSettingTabOpened = open

        if (open)
            closeOtherSettingTabs(backpackSettingTabWidget)
        updateSettingTabEnabledStates(backpackSettingTabWidget, open)
    }

    fun openItemDisplaySettings(tabWidget: TabWidget, open: Boolean) {
        if (!isSettingMode || Config.itemDisplayDisabled)
            return

        itemDisplaySettingTabWidget.showExpanded = open
        isItemDisplaySettingTabOpened = open
        currentItemDisplaySelectedSlot = if (open) backpackWrapper.getFirstItemDisplaySlot() else -1

        if (open)
            closeOtherSettingTabs(itemDisplaySettingTabWidget)
        updateSettingTabEnabledStates(itemDisplaySettingTabWidget, open)
    }

    fun setCurrentItemDisplaySelectedSlot(slotIndex: Int) {
        currentItemDisplaySelectedSlot = slotIndex
    }

    private fun closeSettingTabs() {
        if (!this::memorySettingTabWidget.isInitialized || !this::sortingSettingTabWidget.isInitialized ||
            !this::backpackSettingTabWidget.isInitialized || !this::itemDisplaySettingTabWidget.isInitialized)
            return

        backpackSettingTabWidget.showExpanded = false
        memorySettingTabWidget.showExpanded = false
        sortingSettingTabWidget.showExpanded = false
        itemDisplaySettingTabWidget.showExpanded = false
        backpackSettingTabWidget.isEnabled = isSettingMode
        memorySettingTabWidget.isEnabled = isSettingMode
        sortingSettingTabWidget.isEnabled = isSettingMode
        itemDisplaySettingTabWidget.isEnabled = isSettingMode && !Config.itemDisplayDisabled
        isBackpackSettingTabOpened = false
        isMemorySettingTabOpened = false
        shouldMemorizeRespectNBT = false
        isSortingSettingTabOpened = false
        isItemDisplaySettingTabOpened = false
        currentItemDisplaySelectedSlot = -1
    }

    private fun closeOtherSettingTabs(openTab: TabWidget) {
        if (openTab != backpackSettingTabWidget) {
            backpackSettingTabWidget.showExpanded = false
            isBackpackSettingTabOpened = false
        }
        if (openTab != sortingSettingTabWidget) {
            sortingSettingTabWidget.showExpanded = false
            isSortingSettingTabOpened = false
        }
        if (openTab != memorySettingTabWidget) {
            memorySettingTabWidget.showExpanded = false
            isMemorySettingTabOpened = false
            shouldMemorizeRespectNBT = false
        }
        if (openTab != itemDisplaySettingTabWidget) {
            itemDisplaySettingTabWidget.showExpanded = false
            isItemDisplaySettingTabOpened = false
            currentItemDisplaySelectedSlot = -1
        }
    }

    private fun updateSettingTabEnabledStates(openTab: TabWidget, open: Boolean) {
        listOf(backpackSettingTabWidget, sortingSettingTabWidget, memorySettingTabWidget, itemDisplaySettingTabWidget)
            .forEach { it.isEnabled = !open || it == openTab }
        if (Config.itemDisplayDisabled) {
            itemDisplaySettingTabWidget.isEnabled = false
        }
    }

    private fun closeUpgradeTabs(syncToServer: Boolean) {
        for (slotIndex in 0 until backpackWrapper.upgradeSlotsSize()) {
            val wrapper = backpackWrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
                .getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: continue

            if (!wrapper.isTabOpened)
                continue

            wrapper.isTabOpened = false
            tabWidgets.getOrNull(slotIndex)?.showExpanded = false
            if (syncToServer) {
                upgradeSlotSyncHandlers[slotIndex].syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE) {
                    it.writeBoolean(false)
                }
            }
        }
    }

    private inline fun <reified V : ExpandedUpgradeTabWidget<U>, reified U : UpgradeWrapper<*>> updateAndCheckRecreation(
        widget: ExpandedTabWidget?,
        wrapper: U
    ): Boolean {
        if (widget is V && widget::class == V::class && widget.wrapper::class == wrapper::class) {
            widget.wrapper = wrapper
            return false
        }
        return true
    }

    private inline fun <reified V : ExpandedUpgradeTabWidget<*>> updateAndCheckRecreation(
        widget: ExpandedTabWidget?,
        wrapper: Any
    ): Boolean {
        if (widget is V && widget::class == V::class) {
            return !widget.consumePossibleWrapper(wrapper)
        }
        return true
    }

    private fun clearUpgradeTab(tabWidget: TabWidget) {
        tabWidget.showExpanded = false
        tabWidget.isEnabled = false
        tabWidget.expandedWidget = null
        tabWidget.tabIcon = null
        tabWidget.tooltip().reset()
    }


    private fun updateUpgradeWidgets() {
        if (!isValid)
            return

        if (isSettingMode) {
            tabWidgets.forEach {
                clearUpgradeTab(it)
            }
            syncToggles()
            scheduleResize()
            return
        }

        var tabIndex = 0
        var openedTabIndex: Int? = null

        for (slotIndex in 0 until backpackWrapper.upgradeSlotsSize()) {
            val stack: ItemStack = backpackWrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
            val item = stack.item

            if (!(item is UpgradeItem && item.hasTab))
                continue

            val wrapper = stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: continue

            if (wrapper.isTabOpened) {
                if (openedTabIndex != null) {
                    wrapper.isTabOpened = false
                    upgradeSlotSyncHandlers[slotIndex].syncToServer(UpgradeSlotSH.UPDATE_UPGRADE_TAB_STATE) {
                        it.writeBoolean(false)
                    }
                    continue
                }

                openedTabIndex = slotIndex
            }
        }
        // Shifted forward to account for settings tab.
        var tabDisplayIndex = 2

        // Sync all tabs to their corresponding upgrade
        for (slotIndex in 0 until backpackWrapper.upgradeSlotsSize()) {
            val stack: ItemStack = backpackWrapper.upgradeItemStackHandler.getStackInSlot(slotIndex)
            val item = stack.item

            val tabWidget = tabWidgets[tabIndex]

            if (!(item is UpgradeItem && item.hasTab)) {
                clearUpgradeTab(tabWidget)
                tabIndex++
                continue
            }

            val upgradeSlotGroup = upgradeSlotGroups[slotIndex]
            val wrapper: UpgradeWrapper<*> = stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: run {
                clearUpgradeTab(tabWidget)
                tabIndex++
                continue
            }
            tabWidget.showExpanded = wrapper.isTabOpened
            tabWidget.isEnabled = true
            // Ensure correct tab position
            tabWidget.tabOrder = tabDisplayIndex
            tabWidget.tabIcon = ItemDrawable(stack)
            tabWidget.tooltip {
                it.clearText()
                    .addLine(IKey.str(item.getItemStackDisplayName(stack)))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

            when (wrapper) {
                is CraftingUpgradeWrapper -> {
                    upgradeSlotGroup.updateCraftingDelegate(wrapper)
                    if (updateAndCheckRecreation<CraftingUpgradeWidget, CraftingUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = CraftingUpgradeWidget(slotIndex, wrapper)
                }

                is AdvancedFeedingUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedFeedingUpgradeWidget, AdvancedFeedingUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedFeedingUpgradeWidget(slotIndex, wrapper)
                }

                is FeedingUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<FeedingUpgradeWidget, FeedingUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = FeedingUpgradeWidget(slotIndex, wrapper)
                }

                is AdvancedFilterUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedFilterUpgradeWidget, AdvancedFilterUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedFilterUpgradeWidget(slotIndex, wrapper)
                }

                is FilterUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<FilterUpgradeWidget, FilterUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = FilterUpgradeWidget(slotIndex, wrapper)
                }

                is AdvancedMagnetUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedMagnetUpgradeWidget, AdvancedMagnetUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedMagnetUpgradeWidget(slotIndex, wrapper, stack)
                }

                is MagnetUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<MagnetUpgradeWidget, MagnetUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = MagnetUpgradeWidget(slotIndex, wrapper, stack)
                }

                is AdvancedVoidUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedVoidUpgradeWidget, AdvancedVoidUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedVoidUpgradeWidget(slotIndex, wrapper, stack)
                }

                is VoidUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<VoidUpgradeWidget, VoidUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = VoidUpgradeWidget(slotIndex, wrapper, stack)
                }

                is AdvancedRefillUpgradeWrapper -> {
                    upgradeSlotGroup.updateLargeBasicFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedRefillUpgradeWidget, AdvancedRefillUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedRefillUpgradeWidget(slotIndex, wrapper, stack)
                }

                is RefillUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<RefillUpgradeWidget, RefillUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = RefillUpgradeWidget(slotIndex, wrapper, stack)
                }

                is AdvancedCompactingUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedCompactingUpgradeWidget, AdvancedCompactingUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedCompactingUpgradeWidget(slotIndex, wrapper, stack)
                }

                is CompactingUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<CompactingUpgradeWidget, CompactingUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = CompactingUpgradeWidget(slotIndex, wrapper, stack)
                }

                is AdvancedJukeboxUpgradeWrapper -> {
                    upgradeSlotGroup.updateJukeboxDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedJukeboxUpgradeWidget, AdvancedJukeboxUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedJukeboxUpgradeWidget(slotIndex, wrapper, stack)
                }

                is JukeboxUpgradeWrapper -> {
                    upgradeSlotGroup.updateJukeboxDelegate(wrapper)
                    if (updateAndCheckRecreation<JukeboxUpgradeWidget, JukeboxUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = JukeboxUpgradeWidget(slotIndex, wrapper, stack)
                }

                is AdvancedToolSwapperUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedToolSwapperUpgradeWidget, AdvancedToolSwapperUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedToolSwapperUpgradeWidget(slotIndex, wrapper, stack)
                }

                is TankUpgradeWrapper -> {
                    upgradeSlotGroup.updateTankDelegate(wrapper)
                    if (updateAndCheckRecreation<TankUpgradeWidget, TankUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = TankUpgradeWidget(slotIndex, wrapper, stack)
                }

                is AdvancedPumpUpgradeWrapper -> {
                    if (updateAndCheckRecreation<AdvancedPumpUpgradeWidget, PumpUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AdvancedPumpUpgradeWidget(slotIndex, wrapper, stack)
                }

                is PumpUpgradeWrapper -> {
                    if (updateAndCheckRecreation<PumpUpgradeWidget, PumpUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = PumpUpgradeWidget(slotIndex, wrapper, stack)
                }

                is BatteryUpgradeWrapper -> {
                    upgradeSlotGroup.updateBatteryDelegate(wrapper)
                    if (updateAndCheckRecreation<BatteryUpgradeWidget, BatteryUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = BatteryUpgradeWidget(slotIndex, wrapper, stack)
                }

                is AnvilUpgradeWrapper -> {
                    upgradeSlotGroup.updateAnvilDelegate(wrapper)
                    if (updateAndCheckRecreation<AnvilUpgradeWidget, AnvilUpgradeWrapper>(
                            tabWidget.expandedWidget,
                            wrapper
                        )
                    )
                        tabWidget.expandedWidget = AnvilUpgradeWidget(slotIndex, wrapper, stack)
                }

                is IAdvancedFilterable -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<AdvancedExpandedTabWidget<*>>(tabWidget.expandedWidget, wrapper))
                        tabWidget.expandedWidget = AdvancedExpandedTabWidget(
                            slotIndex,
                            wrapper,
                            stack,
                            wrapper.settingsLangKey
                        )
                }

                is IBasicFilterable -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    if (updateAndCheckRecreation<BasicExpandedTabWidget<*>>(tabWidget.expandedWidget, wrapper))
                        tabWidget.expandedWidget = BasicExpandedTabWidget(
                            slotIndex,
                            wrapper,
                            stack,
                            wrapper.settingsLangKey
                        )
                }
            }

            tabIndex++
            tabDisplayIndex++
        }

        if (openedTabIndex != null) {
            val tabWidget = tabWidgets[openedTabIndex]
            val upperboundIndex = min(
                openedTabIndex + (tabWidget.expandedWidget?.coveredTabSize ?: 0),
                tabWidgets.size
            )

            for (tabIndex in openedTabIndex + 1 until upperboundIndex) {
                tabWidgets[tabIndex].isEnabled = false
            }
        }

        disableUnusedTabWidgets(tabIndex)
        syncToggles()
        scheduleResize()
    }

    private fun disableUnusedTabWidgets(startTabIndex: Int) {
        for (i in startTabIndex until backpackWrapper.upgradeSlotsSize()) {
            clearUpgradeTab(tabWidgets[i])
        }
    }

    private fun syncToggles() {
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val toggleWidget = upgradeSlotGroupWidget.toggleWidgets[i]
            val wrapper = toggleWidget.getWrapper()

            if (wrapper != null) {
                toggleWidget.isToggleEnabled = wrapper.enabled
                toggleWidget.isEnabled = true
            } else {
                toggleWidget.isToggleEnabled = false
                toggleWidget.isEnabled = false
            }
        }
    }

    override fun shouldAnimate(): Boolean =
        ClientConfig.enableAnimation && super.shouldAnimate()

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        renderStorageBackground(widgetTheme.theme)
    }

    private fun renderStorageBackground(theme: WidgetTheme) {
        val slotsTopBottomHeight = min(storageInventoryHeight / 2, 150)
        var yOffset = 0

        storageBackgroundTexture.drawSubArea(
            0f,
            0f,
            area.width.toFloat(),
            (STORAGE_INVENTORY_Y + slotsTopBottomHeight).toFloat(),
            0f,
            0f,
            area.width / 256f,
            (STORAGE_INVENTORY_Y + slotsTopBottomHeight) / 256f,
            theme
        )

        if (storageInventoryHeight / 2 > 150) {
            val middleHeight = (storageInventoryHeight / 2 - 150) * 2
            storageBackgroundTexture.drawSubArea(
                0f,
                (STORAGE_INVENTORY_Y + slotsTopBottomHeight).toFloat(),
                area.width.toFloat(),
                middleHeight.toFloat(),
                0f,
                STORAGE_INVENTORY_Y / 256f,
                area.width / 256f,
                (STORAGE_INVENTORY_Y + middleHeight) / 256f,
                theme
            )
            yOffset = middleHeight
        }

        storageBackgroundTexture.drawSubArea(
            0f,
            (yOffset + STORAGE_INVENTORY_Y + slotsTopBottomHeight).toFloat(),
            area.width.toFloat(),
            (97 + slotsTopBottomHeight).toFloat(),
            0f,
            (256 - (97 + slotsTopBottomHeight)) / 256f,
            area.width / 256f,
            1f,
            theme
        )
    }

    fun getOpenCraftingUpgradeSlot(): Int? {

        for (slotIndex in 0 until backpackWrapper.upgradeSlotsSize()) {
            val slot = upgradeSlotWidgets[slotIndex]
            val stack: ItemStack = slot.slot.stack
            val item = stack.item

            if (!(item is UpgradeItem && item.hasTab)) {
                continue
            }

            val wrapper: UpgradeWrapper<*> = stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: continue

            if (wrapper is CraftingUpgradeWrapper && wrapper.isTabOpened) {
                return slotIndex
            }
        }
        return null
    }

    fun getCraftingInfo(slotIndex: Int): CraftingSlotInfo {
        return upgradeSlotGroups[slotIndex].craftingInfo
    }
}
