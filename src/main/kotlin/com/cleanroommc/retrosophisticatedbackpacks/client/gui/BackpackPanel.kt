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
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade.AdvancedPickupUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget.*
import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.inventory.slot.BackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.inventory.slot.UpgradeSlot
import com.cleanroommc.retrosophisticatedbackpacks.items.CraftingUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.items.PickupUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.items.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.ceilDiv
import com.cleanroommc.retrosophisticatedbackpacks.value.sync.UpgradeSlotSH
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper

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

            upgradeSlot.changeListener { _, _, isClient, _ ->
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

    internal fun modifyPlayerSlot(syncManager: PanelSyncManager, hand: EnumHand, player: EntityPlayer) {
        if (hand == EnumHand.MAIN_HAND) {
            val currentItemSlotIndex = player.inventory.currentItem

            syncManager.itemSlot(
                "player",
                currentItemSlotIndex,
                object : ModularSlot(PlayerMainInvWrapper(player.inventory), currentItemSlotIndex) {
                    override fun canTakeStack(playerIn: EntityPlayer): Boolean = false
                }.slotGroup("player_inventory")
            )
        }
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

    internal fun updateUpgradeWidgets() {
        var tabIndex = 0

        for (slotIndex in 0 until backpackWrapper.upgradeSlotsSize()) {
            val tabWidget = tabWidgets[slotIndex]

            if (!tabWidget.isEnabled)
                continue

            if (tabWidget.expandedWidget != null)
                context.jeiSettings.removeJeiExclusionArea(tabWidget.expandedWidget!!)
        }

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

            when (item) {
                is CraftingUpgradeItem -> {
                    tabWidget.expandedWidget = CraftingUpgradeWidget()
                }

                is PickupUpgradeItem -> {
                    val wrapper = stack.getCapability(Capabilities.PICKUP_UPGRADE_CAPABILITY, null)!!

                    tabWidget.expandedWidget = if (wrapper is AdvancedPickupUpgradeWrapper) {
                        upgradeSlotGroups[slotIndex].updateAdvancedPickupFilterDelegate(wrapper)
                        AdvancedPickupUpgradeWidget(slotIndex, wrapper)
                    } else {
                        upgradeSlotGroups[slotIndex].updatePickupFilterDelegate(wrapper)
                        PickupUpgradeWidget(slotIndex, wrapper)
                    }
                }

                else -> {}
            }


            tabIndex++
        }

        // Disable all unused tab widgets
        for (i in tabIndex until backpackWrapper.upgradeSlotsSize()) {
            val tabWidget = tabWidgets[i]
            tabWidget.isEnabled = false
        }

        // Sync all upgrade toggles
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

        WidgetTree.resize(this)
    }

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