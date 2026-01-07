package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot
import net.minecraftforge.items.IItemHandler

class IndexedModularCraftingSlot(val upgradeSlotIndex: Int, inv: IItemHandler, invIndex: Int): ModularCraftingSlot(inv, invIndex) {
}