package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.inventory.Container
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack

class BackpackInventoryCrafting(
    eventHandlerIn: Container,
    width: Int,
    height: Int
) : InventoryCrafting(eventHandlerIn, width, height) {
    var onCraftingMatrixChanged: () -> Unit = {}

    override fun decrStackSize(index: Int, count: Int): ItemStack {
        val stack = super.decrStackSize(index, count)

        if (!stack.isEmpty) {
            onCraftingMatrixChanged()
        }

        return stack
    }

    override fun setInventorySlotContents(index: Int, stack: ItemStack) {
        super.setInventorySlotContents(index, stack)
        onCraftingMatrixChanged()
    }
}