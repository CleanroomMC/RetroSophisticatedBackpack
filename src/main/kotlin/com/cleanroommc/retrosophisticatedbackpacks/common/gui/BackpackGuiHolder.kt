package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.UISettings
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.BackpackInventoryScrollWidget
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData.InventoryType
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.ceilDiv
import kotlin.math.min
import net.minecraft.entity.player.EntityPlayer

sealed class BackpackGuiHolder(protected val backpackWrapper: BackpackWrapper) {
    companion object {
        private const val SLOT_SIZE = 18
        private const val HEIGHT_WITHOUT_STORAGE_SLOTS = 114
    }

    protected val tankInventoryControlCount = min(backpackWrapper.tankUpgradeSlots().size, 2)
    protected val batteryInventoryControlCount = min(backpackWrapper.batteryUpgradeSlots().size, 1)
    protected val inventoryColumnsTaken =
        (tankInventoryControlCount + batteryInventoryControlCount) * BackpackPanel.INVENTORY_CONTROL_COLUMNS
    protected val backgroundRowSize = if (backpackWrapper.backpackInventorySize() > 81) 12 else 9
    protected val rowSize = (backgroundRowSize - inventoryColumnsTaken).coerceAtLeast(1)
    protected val colSize = backpackWrapper.backpackInventorySize().ceilDiv(rowSize)
    protected val visibleColSize = min(colSize, BackpackPanel.VISIBLE_BACKPACK_ROWS)
    protected val scrollbarWidth = if (colSize > visibleColSize) BackpackInventoryScrollWidget.SCROLLBAR_WIDTH else 0

    protected fun createPanel(
        syncManager: PanelSyncManager,
        player: EntityPlayer,
        tileEntity: BackpackTileEntity?,
        inventoryType: InventoryType? = null,
        slotIndex: Int? = null,
        backpackName: String? = null
    ): BackpackPanel =
        BackpackPanel.defaultPanel(
            syncManager,
            player,
            tileEntity,
            backpackWrapper,
            14 + backgroundRowSize * SLOT_SIZE + scrollbarWidth,
            HEIGHT_WITHOUT_STORAGE_SLOTS + visibleColSize * SLOT_SIZE,
            inventoryType?.let { if (it == InventoryType.PLAYER_INVENTORY) slotIndex else null },
            backpackName,
        )

    protected fun addCommonWidgets(panel: BackpackPanel, player: EntityPlayer) {
        panel.rebuildWidgets()
    }

    class TileEntityGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<PosGuiData> {
        override fun buildUI(
            data: PosGuiData,
            syncManager: PanelSyncManager,
            uiSettings: UISettings
        ): ModularPanel {
            val tileEntity = data.world.getTileEntity(data.blockPos) as BackpackTileEntity
            val panel = createPanel(syncManager, data.player, tileEntity, backpackName = tileEntity.displayName.formattedText)
            addCommonWidgets(panel, data.player)
            return panel
        }
    }

    class ItemStackGuiHolder(backpackWrapper: BackpackWrapper) : BackpackGuiHolder(backpackWrapper),
        IGuiHolder<PlayerInventoryGuiData> {
        override fun buildUI(
            data: PlayerInventoryGuiData,
            syncManager: PanelSyncManager,
            uiSettings: UISettings
        ): ModularPanel {
            val panel = createPanel(syncManager, data.player, null, data.inventoryType, data.slotIndex, data.usedItemStack.displayName)
            panel.modifyPlayerSlot(syncManager, data.inventoryType, data.slotIndex, data.player)
            addCommonWidgets(panel, data.player)
            return panel
        }
    }
}
