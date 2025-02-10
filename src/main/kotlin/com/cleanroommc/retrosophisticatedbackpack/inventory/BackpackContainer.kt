package com.cleanroommc.retrosophisticatedbackpack.inventory

import com.cleanroommc.modularui.screen.ContainerCustomizer
import net.minecraft.inventory.InventoryCrafting

class BackpackContainer() : ContainerCustomizer() {
    val craftingInventory: InventoryCrafting by lazy {
        InventoryCrafting(container, 3, 3)
    }

    override fun onContainerClosed() {
        val syncManager = container.syncManager

        if (!syncManager.isClient)
            container.superClearContainer(syncManager.player, syncManager.player.world, craftingInventory)
    }
}
