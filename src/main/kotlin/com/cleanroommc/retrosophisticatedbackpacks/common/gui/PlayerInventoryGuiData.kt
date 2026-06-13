package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import baubles.api.BaublesApi
import com.cleanroommc.modularui.factory.GuiData
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack

class PlayerInventoryGuiData(
    val targetEntity: EntityLivingBase,
    fromPlayer: EntityPlayer,
    val inventoryType: InventoryType,
    val slotIndex: Int
) : GuiData(fromPlayer) {
    val usedItemStack: ItemStack = if (targetEntity is EntityPlayer) {
        when (inventoryType) {
            InventoryType.PLAYER_INVENTORY -> targetEntity.inventory.getStackInSlot(slotIndex)
            InventoryType.PLAYER_BAUBLES -> {
                if (RetroSophisticatedBackpacks.baublesLoaded)
                    BaublesApi.getBaublesHandler(targetEntity).getStackInSlot(slotIndex)
                else ItemStack.EMPTY
            }
        }
    } else {
        val chestStack = targetEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST)

        if (chestStack.item !is BackpackItem) ItemStack.EMPTY
        else chestStack
    }

    enum class InventoryType {
        PLAYER_INVENTORY,
        PLAYER_BAUBLES
    }
}
