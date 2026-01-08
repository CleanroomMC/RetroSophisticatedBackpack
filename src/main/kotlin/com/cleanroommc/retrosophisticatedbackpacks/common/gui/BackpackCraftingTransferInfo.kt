package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.CraftingSlotInfo
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.LockedPlayerSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo
import net.minecraft.inventory.Slot

class BackpackCraftingTransferInfo : IRecipeTransferInfo<BackpackContainer> {

    override fun getContainerClass(): Class<BackpackContainer> {
        return BackpackContainer::class.java
    }

    override fun getRecipeCategoryUid(): String {
        return "minecraft.crafting" // Ensure it diverts to this for the recipe handler
    }

    override fun canHandle(container: BackpackContainer): Boolean {
        return getCraftingInterfaceIndex(container) != null
    }

    private fun getPanel(container: BackpackContainer): BackpackPanel? {
        val screen = container.getScreen()
        if (!container.isInitialized || screen.panelManager.mainPanel !is BackpackPanel) return null
        return screen.panelManager.mainPanel as BackpackPanel
    }

    private fun getCraftingInterfaceIndex(container: BackpackContainer): Int? {
        val panel: BackpackPanel = getPanel(container) ?: return null
        return panel.getOpenCraftingUpgradeSlot()
    }

    private fun getCraftingInterfaceInfo(container: BackpackContainer): CraftingSlotInfo? {
        val panel: BackpackPanel = getPanel(container) ?: return null
        val index = panel.getOpenCraftingUpgradeSlot() ?: return null
        return panel.getCraftingInfo(index)
    }

    override fun getRecipeSlots(container: BackpackContainer): MutableList<Slot> {
        return getCraftingInterfaceInfo(container)?.craftingMatrixSlots?.toMutableList() ?: mutableListOf()
    }

    override fun getInventorySlots(container: BackpackContainer): MutableList<Slot> {
        val list = mutableListOf<Slot>()
        for (slot in container.inventorySlots) {
            // Non-modular slots or the slot for the backpack itself are off limits
            if (slot !is ModularSlot || slot is LockedPlayerSlot) continue
            if (slot is ModularBackpackSlot || slot.slotGroupName == "player_inventory") {
                list.add(slot)
            }
        }
        return list
    }
}