package com.cleanroommc.retrosophisticatedbackpack.inventory

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.drawable.text.StringKey
import com.cleanroommc.modularui.factory.HandGuiData
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.TextWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpack.inventory.slot.BackpackSlot
import com.cleanroommc.retrosophisticatedbackpack.inventory.slot.UpgradeSlot
import com.cleanroommc.retrosophisticatedbackpack.inventory.slot.UpgradeSlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.ceilDiv
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper

sealed class BackpackGuiHolder(protected val backpackWrapper: BackpackWrapper) {
    companion object {
        private const val SLOT_SIZE = 18
    }

    protected val rowSize = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
    protected val colSize = backpackWrapper.backpackInventorySize().ceilDiv(rowSize)

    protected fun createPanel(syncManager: PanelSyncManager): ModularPanel {
        val panel =
            ModularPanel("backpack_gui").size(14 + rowSize * SLOT_SIZE, 112 + colSize * SLOT_SIZE)
        syncManager.containerCustomizer = BackpackContainer()
        panel.bindPlayerInventory()

        return panel
    }

    protected fun modifyPlayerSlot(syncManager: PanelSyncManager, hand: EnumHand, player: EntityPlayer) {
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

    protected fun addBackpackInventorySlots(panel: ModularPanel, syncManager: PanelSyncManager) {
        for (i in 0 until backpackWrapper.backpackInventorySize()) {
            syncManager.itemSlot(
                "backpack",
                i,
                BackpackSlot(
                    backpackWrapper::getTotalStackMultiplier,
                    backpackWrapper.backpackItemStackHandler,
                    i
                ).slotGroup("backpack_inventory")
            )
        }

        syncManager.registerSlotGroup(SlotGroup("backpack_inventory", rowSize))

        val backpackWidget = SlotGroupWidget().debugName("backpack_inventory")
        backpackWidget.flex().coverChildren().leftRel(0.5F).top(17)

        for (i in 0 until backpackWrapper.backpackInventorySize()) {
            val itemSlot = ItemSlot().syncHandler("backpack", i).pos(i % rowSize * SLOT_SIZE, i / rowSize * SLOT_SIZE)
                .debugName("slot_${i}")

            backpackWidget.child(itemSlot)
        }

        panel.child(backpackWidget)
    }

    protected fun addUpgradeSlots(panel: ModularPanel, syncManager: PanelSyncManager) {
        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            syncManager.itemSlot(
                "upgrades",
                i,
                UpgradeSlot(
                    backpackWrapper.upgradeItemStackHandler,
                    i,
                    backpackWrapper::canRemoveStackUpgrade,
                    backpackWrapper::canReplaceStackUpgrade
                ).slotGroup("upgrade_inventory")
            )
        }

        syncManager.registerSlotGroup(SlotGroup("upgrade_inventory", 1))

        val upgradesWidget = UpgradeSlotGroupWidget(backpackWrapper.upgradeSlotsSize()).debugName("upgrade_inventory")
        upgradesWidget.flex().size(23, 10 + backpackWrapper.upgradeSlotsSize() * 18).left(-21)

        for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
            val itemSlot = ItemSlot().syncHandler("upgrades", i).pos(5, 5 + i * 18).debugName("slot_${i}")

            upgradesWidget.child(itemSlot)
        }

        panel.child(upgradesWidget)
    }

    protected fun addTexts(panel: ModularPanel, syncManager: PanelSyncManager, player: EntityPlayer) {
        panel.child(TextWidget(StringKey(backpackWrapper.getDisplayName().formattedText)).pos(8, 6))
        panel.child(
            TextWidget(StringKey(player.inventory.displayName.formattedText)).pos(
                8,
                18 + colSize * 18
            )
        )

//        panel.child(
//            TextWidget(DynamicKey { backpackWrapper.getTotalStackMultiplier().toString() }).leftRel(0.5F).top(18 + backpackWrapper.backpackInventorySize().ceilDiv(rowSize) * 18)
//        )
    }

    class TileEntityGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<PosGuiData> {
        override fun buildUI(
            data: PosGuiData,
            syncManager: PanelSyncManager
        ): ModularPanel {
            val panel = createPanel(syncManager)
            addBackpackInventorySlots(panel, syncManager)
            addUpgradeSlots(panel, syncManager)
            addTexts(panel, syncManager, data.player)
            return panel
        }

    }

    class ItemStackGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<HandGuiData> {
        override fun buildUI(
            data: HandGuiData,
            syncManager: PanelSyncManager
        ): ModularPanel {
            val panel = createPanel(syncManager)
            modifyPlayerSlot(syncManager, data.hand, data.player)
            addBackpackInventorySlots(panel, syncManager)
            addUpgradeSlots(panel, syncManager)
            addTexts(panel, syncManager, data.player)
            return panel
        }
    }
}