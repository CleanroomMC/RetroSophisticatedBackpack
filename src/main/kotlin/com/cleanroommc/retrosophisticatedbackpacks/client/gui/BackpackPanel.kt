package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.drawable.AdaptableUITexture
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.drawable.text.StringKey
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widget.WidgetTree
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpacks.Tags
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
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
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

        internal fun defaultPanel(
            syncManager: PanelSyncManager,
            player: EntityPlayer,
            tileEntity: BackpackTileEntity?,
            backpackWrapper: BackpackWrapper,
            width: Int,
            height: Int
        ): BackpackPanel {
            val panel = BackpackPanel(player, tileEntity, syncManager, backpackWrapper, BackpackContainer())
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

    val upgradeSlotSyncHandlers: Array<UpgradeSlotSH>
    val upgradeSlotGroups: Array<UpgradeSlotUpdateGroup>

    init {
        // Backpack slots
        for (i in 0 until backpackWrapper.backpackInventorySize()) {
            syncManager.itemSlot(
                "backpack",
                i,
                ModularBackpackSlot(backpackWrapper, i)
                    .slotGroup("backpack_inventory")
            )
        }

        syncManager.registerSlotGroup(SlotGroup("backpack_inventory", rowSize, 100, true))

        // Upgrade slots
        upgradeSlotSyncHandlers = Array(backpackWrapper.upgradeSlotsSize()) {
            val upgradeSlot = ModularUpgradeSlot(
                backpackWrapper.upgradeItemStackHandler,
                it,
                backpackWrapper::canAddStackUpgrade,
                backpackWrapper::canRemoveStackUpgrade,
                backpackWrapper::canReplaceStackUpgrade,
                backpackWrapper::canRemoveInceptionUpgrade
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
            UpgradeSlotUpdateGroup(syncManager, backpackWrapper, it)
        }
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

    internal fun addBackpackInventorySlots() {
        val backpackSlotGroupWidget = SlotGroupWidget().debugName("backpack_inventory")
        backpackSlotGroupWidget.flex().coverChildren().leftRel(0.5F).top(17)

        for (i in 0 until backpackWrapper.backpackInventorySize()) {
            val itemSlot = BackpackSlot()
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
            val tab = TabWidget(i).debugName("upgrade_tab_${i}")

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

            when (wrapper) {
                is CraftingUpgradeWrapper -> {
                    tabWidget.expandedWidget = CraftingUpgradeWidget(slotIndex, wrapper)
                }

                is AdvancedFeedingUpgradeWrapper -> {
                    upgradeSlotGroup.updateAdvancedFilterDelegate(wrapper)
                    tabWidget.expandedWidget = AdvancedFeedingUpgradeWidget(slotIndex, wrapper)
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
            val expandedTabWidget = min(
                openedTabIndex + (tabWidget.expandedWidget?.coveredTabSize ?: 0),
                tabWidgets.size
            )

            for (tabIndex in openedTabIndex + 1 until expandedTabWidget) {
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