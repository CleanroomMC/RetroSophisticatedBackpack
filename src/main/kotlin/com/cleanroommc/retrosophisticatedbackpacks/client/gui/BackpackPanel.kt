package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.api.drawable.IKey
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
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpacks.Config
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget.*
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.BackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.UpgradeSlot
import com.cleanroommc.retrosophisticatedbackpacks.item.*
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

    val upgradeSlotGroups: Array<UpgradeSlotUpdateGroup>

    // Useful when interacting with widgets that only changes upgrade item properties
    internal var changedByPropertyChange: Boolean = false

    init {
        // Backpack slots
        for (i in 0 until backpackWrapper.backpackInventorySize()) {
            syncManager.itemSlot(
                "backpack",
                i,
                BackpackSlot(
                    backpackWrapper::getTotalStackMultiplier,
                    backpackWrapper::canNestBackpack,
                    backpackWrapper.backpackItemStackHandler,
                    i
                ).slotGroup("backpack_inventory")
            )
        }

        syncManager.registerSlotGroup(SlotGroup("backpack_inventory", rowSize, 100, true))

        // Upgrade slots
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val upgradeSlot = UpgradeSlot(
                backpackWrapper.upgradeItemStackHandler,
                i,
                backpackWrapper::canAddStackUpgrade,
                backpackWrapper::canRemoveStackUpgrade,
                backpackWrapper::canReplaceStackUpgrade,
                backpackWrapper::canRemoveInceptionUpgrade
            ).slotGroup("upgrade_inventory")

            upgradeSlot.changeListener { lastStack, _, isClient, init ->
                if (isClient)
                    updateUpgradeWidgets()
            }

            syncManager.syncValue("upgrades", i, UpgradeSlotSH(upgradeSlot))
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
            val itemSlot = ItemSlot().syncHandler("backpack", i).pos(i % rowSize * SLOT_SIZE, i / rowSize * SLOT_SIZE)
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

    internal fun addUpgradeTabs() {
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val tab = TabWidget(i, this).debugName("upgrade_tab_${i}")

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
        if (changedByPropertyChange) {
            changedByPropertyChange = false
            return
        }

        resetTabState()

        var tabIndex = 0

        // Sync all tabs to their corresponding upgrade
        for (slotIndex in 0 until backpackWrapper.upgradeSlotsSize()) {
            val slot = upgradeSlotWidgets[slotIndex]
            val stack = slot.slot.stack
            val item = stack.item

            if (!(item is UpgradeItem && item.hasTab))
                continue

            val tabWidget = tabWidgets[tabIndex]
            tabWidget.isEnabled = true
            tabWidget.tabIcon = ItemDrawable(slot.slot.stack)
                .asWidget()
                .tooltipBuilder {
                    it.addLine(IKey.str(item.getItemStackDisplayName(stack)))
                    it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
                }

            when (item) {
                is CraftingUpgradeItem -> {
                    tabWidget.expandedWidget = CraftingUpgradeWidget()
                }

                is PickupUpgradeItem -> {
                    val wrapper = stack.getCapability(Capabilities.IPICKUP_UPGRADE_CAPABILITY, null)!!

                    tabWidget.expandedWidget = when (wrapper) {
                        is AdvancedPickupUpgradeWrapper -> {
                            upgradeSlotGroups[slotIndex].updateAdvancedFilterDelegate(wrapper)
                            AdvancedPickupUpgradeWidget(syncManager, slotIndex, wrapper)
                        }

                        is PickupUpgradeWrapper -> {
                            upgradeSlotGroups[slotIndex].updateFilterDelegate(wrapper)
                            PickupUpgradeWidget(slotIndex, wrapper)
                        }
                    }
                }

                is FeedingUpgradeItem -> {
                    val wrapper = stack.getCapability(Capabilities.IFEEDING_UPGRADE_CAPABILITY, null)!!

                    tabWidget.expandedWidget = when (wrapper) {
                        is AdvancedFeedingUpgradeWrapper -> {
                            upgradeSlotGroups[slotIndex].updateAdvancedFilterDelegate(wrapper)
                            AdvancedFeedingUpgradeWidget(syncManager, slotIndex, wrapper)
                        }

                        is FeedingUpgradeWrapper -> {
                            upgradeSlotGroups[slotIndex].updateFilterDelegate(wrapper)
                            FeedingUpgradeWidget(slotIndex, wrapper)
                        }
                    }
                }

                is DepositUpgradeItem -> {
                    val wrapper = stack.getCapability(Capabilities.IDEPOSIT_UPGRADE_CAPABILITY, null)!!

                    tabWidget.expandedWidget = when (wrapper) {
                        is AdvancedDepositUpgradeWrapper -> {
                            upgradeSlotGroups[slotIndex].updateAdvancedFilterDelegate(wrapper)
                            AdvancedDepositUpgradeWidget(syncManager, slotIndex, wrapper)
                        }

                        is DepositUpgradeWrapper -> {
                            upgradeSlotGroups[slotIndex].updateFilterDelegate(wrapper)
                            DepositUpgradeWidget(slotIndex, wrapper)
                        }
                    }
                }

                else -> {}
            }

            context.jeiSettings.addJeiExclusionArea(tabWidget.expandedWidget)
            tabIndex++
        }

        disableUnusedTabWidgets(tabIndex)
        syncToggles()
        WidgetTree.resize(this)
    }

    private fun resetTabState() {
        for (tabWidget in tabWidgets) {
            tabWidget.isEnabled = false

            if (tabWidget.expandedWidget != null) {
                context.jeiSettings.removeJeiExclusionArea(tabWidget.expandedWidget)
                tabWidget.expandedWidget = null
            }

            tabWidget.tabIcon = null
            tabWidget.showExpanded = false
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

    // Only call this when any tab widgets is clicked
    internal fun updateTabWidgets(triggeredTabIndex: Int, isExpanded: Boolean) {
        val updateUpperBound =
            min(triggeredTabIndex + tabWidgets[triggeredTabIndex].expandedWidget!!.coveredTabSize, tabWidgets.size - 1)

        // Disable all other tabs
        for (i in 0 until tabWidgets.size) {
            if (i == triggeredTabIndex)
                continue

            tabWidgets[i].showExpanded = false
        }

        // Refresh potentially covered tabs
        for (i in triggeredTabIndex + 1 until updateUpperBound) {
            val tabWidget = tabWidgets[i]

            if (tabWidget.expandedWidget == null)
                continue

            tabWidgets[i].isEnabled = !isExpanded
        }

        WidgetTree.resize(this)
    }

    override fun shouldAnimate(): Boolean =
        Config.clientConfig.enableAnimation

    override fun onClose() {
        super.onClose()
        tileEntity?.closeInventory(player)
    }

    override fun postDraw(context: ModularGuiContext, transformed: Boolean) {
        super.postDraw(context, transformed)

        // Nasty hack to draw over upgrade tabs
        LAYERED_TAB_TEXTURE.draw(context, flex.area.width - 6, 0, 6, flex.area.height, WidgetTheme.getDefault())
    }
}