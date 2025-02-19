package com.cleanroommc.retrosophisticatedbackpacks.inventory

import com.cleanroommc.modularui.screen.ContainerCustomizer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.InventoryCraftResult

class BackpackContainer() : ContainerCustomizer() {
    internal val craftingInventory = BackpackInventoryCrafting(object : Container() {
        override fun canInteractWith(playerIn: EntityPlayer): Boolean =
            true
    }, 3, 3)
    internal val craftingResult = InventoryCraftResult()
}
