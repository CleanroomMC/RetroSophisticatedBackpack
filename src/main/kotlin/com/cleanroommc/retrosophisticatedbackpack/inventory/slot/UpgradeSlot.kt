package com.cleanroommc.retrosophisticatedbackpack.inventory.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpack.items.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpack.items.UpgradeItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class UpgradeSlot(
    itemHandler: IItemHandler,
    index: Int,
    private val canRemoveStack: (Int) -> Boolean,
    private val canReplaceStack: (Int, Int) -> Boolean
) : ModularSlot(itemHandler, index) {
    override fun canTakeStack(playerIn: EntityPlayer): Boolean {
        val originalUpgradeItem = stack.item

        if (originalUpgradeItem is StackUpgradeItem) {
            val newUpgradeItem = playerIn.inventory.itemStack.item

            return if (newUpgradeItem is StackUpgradeItem) canReplaceStack(
                originalUpgradeItem.multiplier(),
                newUpgradeItem.multiplier()
            )
            else canRemoveStack(originalUpgradeItem.multiplier())
        }

        return true
    }

    override fun getItemStackLimit(stack: ItemStack): Int =
        1

    override fun isItemValid(stack: ItemStack): Boolean =
        stack.item is UpgradeItem
}