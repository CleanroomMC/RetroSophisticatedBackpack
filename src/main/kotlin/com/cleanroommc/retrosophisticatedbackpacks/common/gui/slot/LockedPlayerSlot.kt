package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.items.IItemHandler

class LockedPlayerSlot(inv: IItemHandler, slotIndex: Int) : ModularSlot(inv, slotIndex) {
    override fun canTakeStack(playerIn: EntityPlayer): Boolean = false
}