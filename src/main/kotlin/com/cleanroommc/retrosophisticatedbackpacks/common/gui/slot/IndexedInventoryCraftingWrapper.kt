package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.InventoryCraftingWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper.CraftingDestination
import net.minecraft.inventory.Container
import net.minecraftforge.items.IItemHandlerModifiable

class IndexedInventoryCraftingWrapper(val upgradeSlotIndex: Int, cont: Container, width: Int, height: Int, delegate: IItemHandlerModifiable, startIndex: Int): InventoryCraftingWrapper(cont, width, height, delegate, startIndex) {
    var shiftType: CraftingDestination = CraftingDestination.INVENTORY
}