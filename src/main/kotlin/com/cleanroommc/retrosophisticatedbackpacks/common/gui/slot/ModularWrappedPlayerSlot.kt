package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.items.IItemHandler

class ModularWrappedPlayerSlot(inv: IItemHandler, sindex: Int): ModularSlot(inv, sindex) {
    override fun canTakeStack(playerIn: EntityPlayer): Boolean = false
}