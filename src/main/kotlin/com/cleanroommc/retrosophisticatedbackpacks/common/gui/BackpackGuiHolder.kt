package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.ceilDiv
import net.minecraft.entity.player.EntityPlayer

sealed class BackpackGuiHolder(protected val backpackWrapper: BackpackWrapper) {
    companion object {
        private const val SLOT_SIZE = 18
    }

    protected val rowSize = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
    protected val colSize = backpackWrapper.backpackInventorySize().ceilDiv(rowSize)

    protected fun createPanel(
        syncManager: PanelSyncManager,
        player: EntityPlayer,
        tileEntity: BackpackTileEntity?
    ): BackpackPanel =
        BackpackPanel.defaultPanel(
            syncManager,
            player,
            tileEntity,
            backpackWrapper,
            14 + rowSize * SLOT_SIZE,
            112 + colSize * SLOT_SIZE
        )

    protected fun addCommonWidgets(panel: BackpackPanel, player: EntityPlayer) {
        panel.addSortingButtons()
        panel.addTransferButtons()
        panel.addBackpackInventorySlots()
        panel.addUpgradeSlots()
        panel.addSettingTab()
        panel.addUpgradeTabs()
        panel.addTexts(player)
    }

    class TileEntityGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<PosGuiData> {
        override fun buildUI(
            data: PosGuiData,
            syncManager: PanelSyncManager
        ): ModularPanel {
            val tileEntity = data.world.getTileEntity(data.blockPos) as BackpackTileEntity
            val panel = createPanel(syncManager, data.player, tileEntity)
            addCommonWidgets(panel, data.player)
            return panel
        }
    }

    class ItemStackGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<PlayerInventoryGuiData> {
        override fun buildUI(
            data: PlayerInventoryGuiData,
            syncManager: PanelSyncManager
        ): ModularPanel {
            val panel = createPanel(syncManager, data.player, null)
            addCommonWidgets(panel, data.player)
            panel.modifyPlayerSlot(syncManager, data.inventoryType, data.slotIndex, data.player)
            return panel
        }
    }
}