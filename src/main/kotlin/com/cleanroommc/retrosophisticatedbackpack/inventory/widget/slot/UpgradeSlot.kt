package com.cleanroommc.retrosophisticatedbackpack.inventory.widget.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpack.items.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpack.items.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpack.items.UpgradeItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class UpgradeSlot(
    itemHandler: IItemHandler,
    index: Int,
    private val canAddStackUpgrade: (Int) -> Boolean,
    private val canRemoveStackUpgrade: (Int) -> Boolean,
    private val canReplaceStackUpgrade: (Int, Int) -> Boolean,
    private val canRemoveInceptionUpgrade: () -> Boolean,
) : ModularSlot(itemHandler, index) {
    override fun onSlotChangedReal(itemStack: ItemStack?, onlyChangedAmount: Boolean, client: Boolean, init: Boolean) {
        super.onSlotChangedReal(itemStack, onlyChangedAmount, client, init)
    }

    override fun canTakeStack(playerIn: EntityPlayer): Boolean {
        val originalUpgradeItem = stack.item

        if (originalUpgradeItem is StackUpgradeItem) {
            val newUpgradeItem = playerIn.inventory.itemStack.item

            return if (newUpgradeItem is StackUpgradeItem) canReplaceStackUpgrade(
                originalUpgradeItem.multiplier(),
                newUpgradeItem.multiplier()
            )
            else canRemoveStackUpgrade(originalUpgradeItem.multiplier())
        }

        if (originalUpgradeItem is InceptionUpgradeItem) {
            val newUpgradeItem = playerIn.inventory.itemStack.item

            return if (newUpgradeItem !is InceptionUpgradeItem) canRemoveInceptionUpgrade()
            else true
        }

        return true
    }

    override fun getItemStackLimit(stack: ItemStack): Int =
        1

    override fun isItemValid(stack: ItemStack): Boolean = when (val item = stack.item) {
        is StackUpgradeItem -> {
            canAddStackUpgrade(item.multiplier())
        }

        else -> item is UpgradeItem
    }
}