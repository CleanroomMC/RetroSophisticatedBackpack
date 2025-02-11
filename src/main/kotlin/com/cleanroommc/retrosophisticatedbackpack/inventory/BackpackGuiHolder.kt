package com.cleanroommc.retrosophisticatedbackpack.inventory

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.factory.HandGuiData
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.ceilDiv
import net.minecraft.entity.player.EntityPlayer

sealed class BackpackGuiHolder(protected val backpackWrapper: BackpackWrapper) {
    companion object {
        private const val SLOT_SIZE = 18
    }

    protected val rowSize = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
    protected val colSize = backpackWrapper.backpackInventorySize().ceilDiv(rowSize)

    protected fun createPanel(syncManager: PanelSyncManager, player: EntityPlayer): BackpackPanel =
        BackpackPanel.defaultPanel(
            syncManager,
            player,
            backpackWrapper,
            14 + rowSize * SLOT_SIZE,
            112 + colSize * SLOT_SIZE
        )

    protected fun addCommonWidgets(panel: BackpackPanel, syncManager: PanelSyncManager, player: EntityPlayer) {
        panel.registerSlots(syncManager)
        panel.addBackpackInventorySlots()
        panel.addUpgradeSlots()
        panel.addUpgradeTabs()
        panel.addTexts(player)
    }

    class TileEntityGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<PosGuiData> {
        override fun buildUI(
            data: PosGuiData,
            syncManager: PanelSyncManager
        ): ModularPanel {
            val panel = createPanel(syncManager, data.player)
            addCommonWidgets(panel, syncManager, data.player)
            return panel
        }
    }

    class ItemStackGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<HandGuiData> {
        override fun buildUI(
            data: HandGuiData,
            syncManager: PanelSyncManager
        ): ModularPanel {
            val panel = createPanel(syncManager, data.player)
            addCommonWidgets(panel, syncManager, data.player)
            panel.modifyPlayerSlot(syncManager, data.hand, data.player)
            return panel
        }
    }
}