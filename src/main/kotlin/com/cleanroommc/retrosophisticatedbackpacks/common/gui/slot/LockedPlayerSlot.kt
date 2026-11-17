package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler

class LockedPlayerSlot(inv: IItemHandler, slotIndex: Int) : ModularSlot(inv, slotIndex) {
    override fun canTakeStack(playerIn: EntityPlayer): Boolean = false

    // Also closed to inserts: a slot that is insertable but not takeable still counts as sortable for
    // Inventory Bogosorter, which then replaces the open backpack with a copy
    override fun isItemValid(stack: ItemStack): Boolean = false
}