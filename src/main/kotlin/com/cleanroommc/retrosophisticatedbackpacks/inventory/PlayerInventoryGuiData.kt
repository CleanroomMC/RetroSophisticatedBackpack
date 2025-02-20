package com.cleanroommc.retrosophisticatedbackpacks.inventory

import baubles.api.BaublesApi
import com.cleanroommc.modularui.factory.GuiData
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand

class PlayerInventoryGuiData(
    player: EntityPlayer,
    val inventoryType: InventoryType,
    val slotIndex: Int
) : GuiData(player) {
    val hand: EnumHand? = when {
        inventoryType == InventoryType.PLAYER_INVENTORY && slotIndex == player.inventory.currentItem -> EnumHand.MAIN_HAND
        inventoryType == InventoryType.PLAYER_INVENTORY && slotIndex == 40 -> EnumHand.OFF_HAND
        else -> null
    }

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