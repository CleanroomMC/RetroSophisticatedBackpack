package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class ModularUpgradeSlot(
    itemHandler: IItemHandler,
    index: Int,
    private val canAddStackUpgrade: (Int) -> Boolean,
    private val canRemoveStackUpgrade: (Int) -> Boolean,
    private val canReplaceStackUpgrade: (Int, Int) -> Boolean,
    private val canRemoveInceptionUpgrade: () -> Boolean,
) : ModularSlot(itemHandler, index) {
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