package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import baubles.api.BaublesApi
import com.cleanroommc.modularui.factory.GuiData
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class PlayerInventoryGuiData(
    player: EntityPlayer,
    val inventoryType: InventoryType,
    val slotIndex: Int
) : GuiData(player) {
    val usedItemStack: ItemStack = when (inventoryType) {
        InventoryType.PLAYER_INVENTORY -> player.inventory.getStackInSlot(slotIndex)
        InventoryType.PLAYER_BAUBLES -> {
            if (RetroSophisticatedBackpacks.baublesLoaded)
                BaublesApi.getBaublesHandler(player).getStackInSlot(slotIndex)
            else ItemStack.EMPTY
        }
    }

    enum class InventoryType {
        PLAYER_INVENTORY,
        PLAYER_BAUBLES
    }
}