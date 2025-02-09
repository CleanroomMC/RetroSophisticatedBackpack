package com.cleanroommc.retrosophisticatedbackpack.inventory.widget.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryCrafting
import net.minecraftforge.items.wrapper.InvWrapper

class ModularCraftingSlot(
    private val player: EntityPlayer,
    private val craftingInventory: InventoryCrafting,
    inventory: IInventory,
    index: Int
) : ModularSlot(InvWrapper(inventory), index) {
    
}