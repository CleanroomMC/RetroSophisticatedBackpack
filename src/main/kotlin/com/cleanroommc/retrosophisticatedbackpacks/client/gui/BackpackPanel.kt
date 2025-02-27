package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.api.IPanelHandler
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.AdaptableUITexture
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.drawable.text.StringKey
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widget.WidgetTree
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.*
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.BackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularUpgradeSlot
import com.cleanroommc.retrosophisticatedbackpacks.config.ClientConfig
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.ceilDiv
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.items.wrapper.PlayerInvWrapper
import kotlin.math.min

class BackpackPanel(
    internal val player: EntityPlayer,
    internal val tileEntity: BackpackTileEntity?,
    internal val syncManager: PanelSyncManager,
    internal val backpackWrapper: BackpackWrapper,
    internal val backpackContainer: BackpackContainer
) : ModularPanel("backpack_gui") {
    companion object {
        private const val SLOT_SIZE = 18
        private val LAYERED_TAB_TEXTURE = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(132, 0, 124, 256)
            .adaptable(4)
            .tiled()
            .build() as AdaptableUITexture
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
        ): BackpackPanel {
            val panel = BackpackPanel(player, tileEntity, syncManager, wrapper, BackpackContainer(wrapper, backpackSlotIndex))
                .size(width, height) as BackpackPanel
            syncManager.containerCustomizer = panel.backpackContainer
            syncManager.bindPlayerInventory(player)
            panel.bindPlayerInventory()

            return panel
        }
    }

    val upgradeSlotWidgets = mutableListOf<ItemSlot>()
    val upgradeSlotGroupWidget = UpgradeSlotGroupWidget(this, backpackWrapper.upgradeSlotsSize())
    val tabWidgets = mutableListOf<TabWidget>()
    val rowSize = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
    val colSize = backpackWrapper.backpackInventorySize().ceilDiv(rowSize)

    val backpackSyncHandler: BackpackSH = BackpackSH(PlayerInvWrapper(player.inventory), backpackWrapper)
    val backpackSlotSyncHandlers: Array<BackpackSlotSH>
    val upgradeSlotSyncHandlers: Array<UpgradeSlotSH>
    val upgradeSlotGroups: Array<UpgradeSlotUpdateGroup>

    val settingPanel: IPanelHandler
    var isMemorySettingTabOpened: Boolean = false
    var isSortingSettingTabOpened: Boolean = false

    init {
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
            val syncHandler = UpgradeSlotSH(upgradeSlot)

            upgradeSlot.changeListener { lastStack, _, isClient, init ->
                if (isClient)
                    updateUpgradeWidgets()
            }

            syncManager.syncValue("upgrades", it, syncHandler)
            syncHandler
        }

        syncManager.registerSlotGroup(SlotGroup("upgrade_inventory", 1, 99, true))

        // Upgrade slot inventory pre register
        upgradeSlotGroups = Array(backpackWrapper.upgradeSlotsSize()) {
            UpgradeSlotUpdateGroup(this, backpackWrapper, it)
        }

        settingPanel = syncManager.panel("setting_panel", { syncManager, syncHandler ->
            BackpackSettingPanel(this)
        }, true)
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
            object : ModularSlot(PlayerInvWrapper(player.inventory), slotIndex) {
                override fun canTakeStack(playerIn: EntityPlayer): Boolean = false
            }.slotGroup("player_inventory")
        )
    }

    internal fun addSortingButtons() {
        val sortButton = ButtonWidget()
            .top(4)
            .right(21)
            .size(12)
            .overlay(RSBTextures.SORT_ICON)
            .onMousePressed {
                if (it == 0) {
                    Interactable.playButtonClickSound()
                    backpackSyncHandler.sortInventory()
                    backpackSyncHandler.syncToServer(BackpackSH.UPDATE_SORT_INV)
                    true
                } else false
            }
            .tooltipStatic {
                it.addLine(IKey.lang("gui.sort_inventory".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
        val sortTypeButton = CyclicVariantButtonWidget(
            SORT_TYPE_VARIANTS,
            backpackWrapper.sortType.ordinal,
            iconOffset = 0,
            iconSize = 12
        ) {
            val nextSortType = SortType.entries[it]

            backpackSyncHandler.setSortType(nextSortType)
            backpackSyncHandler.syncToServer(BackpackSH.UPDATE_SET_SORT_TYPE) {
                it.writeEnumValue(nextSortType)
            }
        }.top(4)
            .right(7)
            .size(12)

        child(sortButton)
            .child(sortTypeButton)
    }

    internal fun addTransferButtons() {
        val transferToPlayerButton = ButtonWidget()
            .top(17 + colSize * 18)
            .right(21)
            .size(12)
            .overlay(RSBTextures.DOT_DOWN_ARROW_ICON)
            .onMousePressed {
                if (it == 0) {
                    Interactable.playButtonClickSound()
                    backpackSyncHandler.transferToPlayerInventory()
                    backpackSyncHandler.syncToServer(BackpackSH.UPDATE_TRANSFER_TO_PLAYER_INV)
                    true
                } else false
            }
            .tooltipStatic {
                it.addLine(IKey.lang("gui.transfer_to_player_inv".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
        val transferToBackpackButton = ButtonWidget()
            .top(17 + colSize * 18)
            .right(7)
            .size(12)
            .overlay(RSBTextures.DOT_UP_ARROW_ICON)
            .onMousePressed {
                if (it == 0) {
                    Interactable.playButtonClickSound()
                    backpackSyncHandler.transferToBackpack()
                    backpackSyncHandler.syncToServer(BackpackSH.UPDATE_TRANSFER_TO_BACKPACK_INV)
                    true
                } else false
            }
            .tooltipStatic {
                it.addLine(IKey.lang("gui.transfer_to_backpack_inv".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

        child(transferToPlayerButton)
            .child(transferToBackpackButton)
    }

    internal fun addBackpackInventorySlots() {
        val backpackSlotGroupWidget = SlotGroupWidget().debugName("backpack_inventory")
        backpackSlotGroupWidget.flex().coverChildren().leftRel(0.5F).top(17)

        for (i in 0 until backpackWrapper.backpackInventorySize()) {
            val itemSlot = BackpackSlot(this, backpackWrapper)
                .syncHandler("backpack", i)
                .pos(i % rowSize * SLOT_SIZE, i / rowSize * SLOT_SIZE)
                .debugName("slot_${i}")

            backpackSlotGroupWidget.child(itemSlot)
        }

        child(backpackSlotGroupWidget)
    }

    internal fun addUpgradeSlots() {
        upgradeSlotGroupWidget.debugName("upgrade_inventory")
        upgradeSlotGroupWidget.flex().size(23, 10 + backpackWrapper.upgradeSlotsSize() * 18).left(-21)

        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val itemSlot = ItemSlot().syncHandler("upgrades", i).pos(5, 5 + i * 18).debugName("slot_${i}")

            upgradeSlotWidgets.add(itemSlot)
            upgradeSlotGroupWidget.child(itemSlot)
        }

        child(upgradeSlotGroupWidget)
    }

    internal fun addSettingTab() {
        child(SettingTabWidget())
    }

    internal fun addUpgradeTabs() {
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val tab = TabWidget(i + 1).debugName("upgrade_tab_${i}")

            tab.isEnabled = false
            tabWidgets.add(tab)
        }

        // Allows most-top widget to be drawn on top of any other widgets
        for (tab in tabWidgets.asReversed()) {
            child(tab)
        }
    }

    internal fun addTexts(player: EntityPlayer) {
        // TODO: Delegates to itemstack or tileentity's display name
        child(TextWidget(StringKey(backpackWrapper.getDisplayName().formattedText)).pos(8, 6))
        child(TextWidget(StringKey(player.inventory.displayName.formattedText)).pos(8, 18 + colSize * 18))
    }

    private fun updateUpgradeWidgets() {
        var tabIndex = 0
        var openedTabIndex: Int? = null

        resetTabState()

        for ((slotIndex, slotWidget) in upgradeSlotWidgets.withIndex()) {
            val stack = slotWidget.slot.stack
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

                    return
                }

                openedTabIndex = slotIndex
            }
        }

        // Sync all tabs to their corresponding upgrade
        for (slotIndex in 0 until backpackWrapper.upgradeSlotsSize()) {
            val slot = upgradeSlotWidgets[slotIndex]
            val stack = slot.slot.stack
            val item = stack.item

            if (!(item is UpgradeItem && item.hasTab))
                continue

            val tabWidget = tabWidgets[tabIndex]
            val upgradeSlotGroup = upgradeSlotGroups[slotIndex]
            val wrapper = stack.getCapability(Capabilities.UPGRADE_CAPABILITY, null) ?: continue
            tabWidget.showExpanded = wrapper.isTabOpened
            tabWidget.isEnabled = true
            tabWidget.tabIcon = ItemDrawable(slot.slot.stack)
            tabWidget.tooltip {
                it.clearText()
                    .addLine(IKey.str(item.getItemStackDisplayName(stack)))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }


            when (wrapper) {
                is CraftingUpgradeWrapper -> {
                    tabWidget.expandedWidget = CraftingUpgradeWidget(slotIndex, wrapper)
                }

                is AdvancedFeedingUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    tabWidget.expandedWidget = AdvancedFeedingUpgradeWidget(slotIndex, wrapper)
                }

                is FeedingUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    tabWidget.expandedWidget = FeedingUpgradeWidget(slotIndex, wrapper)
                }

                is AdvancedFilterUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    tabWidget.expandedWidget = AdvancedFilterUpgradeWidget(slotIndex, wrapper)
                }

                is FilterUpgradeWrapper -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    tabWidget.expandedWidget = FilterUpgradeWidget(slotIndex, wrapper)
                }

                is IAdvancedFilterable -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    tabWidget.expandedWidget = AdvancedExpandedTabWidget(
                        slotIndex,
                        wrapper,
                        stack,
                        wrapper.settingsLangKey
                    )
                }

                is IBasicFilterable -> {
                    upgradeSlotGroup.updateFilterDelegate(wrapper)
                    tabWidget.expandedWidget = BasicExpandedTabWidget(
                        slotIndex,
                        wrapper,
                        stack,
                        wrapper.settingsLangKey
                    )
                }
            }

            context.jeiSettings.addJeiExclusionArea(tabWidget.expandedWidget)
            tabIndex++
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
        WidgetTree.resize(this)
    }

    private fun resetTabState() {
        for (tabWidget in tabWidgets) {
            if (tabWidget.expandedWidget != null) {
                context.jeiSettings.removeJeiExclusionArea(tabWidget.expandedWidget)
            }
        }
    }

    private fun disableUnusedTabWidgets(startTabIndex: Int) {
        for (i in startTabIndex until backpackWrapper.upgradeSlotsSize()) {
            tabWidgets[i].isEnabled = false
        }
    }

    private fun syncToggles() {
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val toggleWidget = upgradeSlotGroupWidget.toggleWidgets[i]
            val wrapper = toggleWidget.getWrapper()

            if (wrapper != null) {
                toggleWidget.isEnabled = true
                toggleWidget.isToggleEnabled = wrapper.enabled
            } else {
                toggleWidget.isEnabled = false
            }
        }
    }

    override fun shouldAnimate(): Boolean =
        ClientConfig.enableAnimation

    override fun postDraw(context: ModularGuiContext, transformed: Boolean) {
        super.postDraw(context, transformed)

        // Nasty hack to draw over upgrade tabs
        LAYERED_TAB_TEXTURE.draw(context, flex.area.width - 6, 0, 6, flex.area.height, WidgetTheme.getDefault())
    }
}