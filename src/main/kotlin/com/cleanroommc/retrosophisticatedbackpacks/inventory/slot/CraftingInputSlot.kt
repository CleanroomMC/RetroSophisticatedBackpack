package com.cleanroommc.retrosophisticatedbackpacks.inventory.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraftforge.items.IItemHandler

class CraftingInputSlot(
    private val onCraftingMatrixChanged: () -> Unit,
    itemHandler: IItemHandler,
    index: Int
) : ModularSlot(itemHandler, index) {
    init {
        changeListener { _, _, isClient, _ ->
            if (!isClient) {
                onCraftingMatrixChanged()
            }
        }
    }
}