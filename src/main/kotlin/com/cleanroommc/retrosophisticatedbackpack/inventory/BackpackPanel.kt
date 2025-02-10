package com.cleanroommc.retrosophisticatedbackpack.inventory

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
import com.cleanroommc.retrosophisticatedbackpack.Tags
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpack.inventory.widget.CraftingTableWidget
import com.cleanroommc.retrosophisticatedbackpack.inventory.widget.TabWidget
import com.cleanroommc.retrosophisticatedbackpack.inventory.widget.UpgradeSlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpack.inventory.widget.slot.BackpackSlot
import com.cleanroommc.retrosophisticatedbackpack.inventory.widget.slot.UpgradeSlot
import com.cleanroommc.retrosophisticatedbackpack.items.CraftingUpgradeItem
import com.cleanroommc.retrosophisticatedbackpack.items.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.ceilDiv
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper

class BackpackPanel(private val backpackWrapper: BackpackWrapper) : ModularPanel("backpack_gui") {
    companion object {
        private const val SLOT_SIZE = 18
        private val LAYERED_TAB_TEXTURE = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(132, 0, 124, 256)
            .adaptable(4)
            .tiled()
            .build() as AdaptableUITexture

        fun defaultPanel(backpackWrapper: BackpackWrapper, width: Int, height: Int): BackpackPanel =
            BackpackPanel(backpackWrapper).size(width, height) as BackpackPanel
    }

    val upgradeSlotWidgets = mutableListOf<ItemSlot>()
    val tabWidgets = mutableListOf<TabWidget>()
    val rowSize = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
    val colSize = backpackWrapper.backpackInventorySize().ceilDiv(rowSize)

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

    internal fun addBackpackInventorySlots(syncManager: PanelSyncManager) {
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

        syncManager.registerSlotGroup(SlotGroup("backpack_inventory", rowSize))

        val backpackSlotGroupWidget = SlotGroupWidget().debugName("backpack_inventory")
        backpackSlotGroupWidget.flex().coverChildren().leftRel(0.5F).top(17)

        for (i in 0 until backpackWrapper.backpackInventorySize()) {
            val itemSlot = ItemSlot().syncHandler("backpack", i).pos(i % rowSize * SLOT_SIZE, i / rowSize * SLOT_SIZE)
                .debugName("slot_${i}")

            backpackSlotGroupWidget.child(itemSlot)
        }

        child(backpackSlotGroupWidget)
    }

    internal fun addUpgradeSlots(syncManager: PanelSyncManager) {
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
                    updateUpgradeTabs(syncManager)
            }

            syncManager.itemSlot("upgrades", i, upgradeSlot)
        }

        syncManager.registerSlotGroup(SlotGroup("upgrade_inventory", 1))

        val upgradesSlotGroupWidget =
            UpgradeSlotGroupWidget(backpackWrapper.upgradeSlotsSize()).debugName("upgrade_inventory")
        upgradesSlotGroupWidget.flex().size(23, 10 + backpackWrapper.upgradeSlotsSize() * 18).left(-21)

        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val itemSlot = ItemSlot().syncHandler("upgrades", i).pos(5, 5 + i * 18).debugName("slot_${i}")

            upgradeSlotWidgets.add(itemSlot)
            upgradesSlotGroupWidget.child(itemSlot)
        }

        child(upgradesSlotGroupWidget)
    }

    internal fun addUpgradeTabs() {
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val tab = TabWidget(i) {
                for (tabWidget in tabWidgets) {
                    if (tabWidget != this) {
                        tabWidget.showExpanded = false
                    }
                }
            }.debugName("upgrade_tab_${i}")

            tab.isEnabled = false
            tabWidgets.add(tab)
            child(tab)
        }
    }

    internal fun addTexts(player: EntityPlayer) {
        // TODO: Delegates to itemstack or tileentity's display name
        child(TextWidget(StringKey(backpackWrapper.getDisplayName().formattedText)).pos(8, 6))
        child(TextWidget(StringKey(player.inventory.displayName.formattedText)).pos(8, 18 + colSize * 18))
    }

    internal fun updateUpgradeTabs(syncManager: PanelSyncManager) {
        var tabIndex = 0

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
                    tabWidget.expandedWidget = CraftingTableWidget(syncManager)
                }

                else -> {}
            }


            tabIndex++
        }

        for (i in tabIndex until backpackWrapper.upgradeSlotsSize()) {
            val tabWidget = tabWidgets[i]
            val expandedWidget = tabWidget.expandedWidget

            tabWidget.isEnabled = false
        }

        WidgetTree.resize(this)
    }

    override fun postDraw(context: ModularGuiContext, transformed: Boolean) {
        super.postDraw(context, transformed)

        // Nasty hack to draw over upgrade tabs
        LAYERED_TAB_TEXTURE.draw(context, flex.area.width - 6, 0, 6, flex.area.height, WidgetTheme.getDefault())
    }
}