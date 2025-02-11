package com.cleanroommc.retrosophisticatedbackpack.inventory

import com.cleanroommc.modularui.screen.ContainerCustomizer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.InventoryCrafting

class BackpackContainer() : ContainerCustomizer() {
    internal val craftingInventory: InventoryCrafting = InventoryCrafting(object : Container() {
        override fun canInteractWith(playerIn: EntityPlayer): Boolean =
            true
    }, 3, 3)

    override fun onContainerClosed() {
        super.onContainerClosed()

        container.superClearContainer(container.player, container.player.world, craftingInventory)
    }
}
