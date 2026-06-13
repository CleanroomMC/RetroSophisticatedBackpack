package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import java.util.UUID

data class CapturedMob(
    val id: UUID,
    val entityType: ResourceLocation,
    val entityNbt: NBTTagCompound,
    val slot: Int,
    val width: Int,
    val height: Int,
    val slotCost: Int,
    val hostile: Boolean,
    val displayName: String,
    val currentHealth: Int,
    val maxHealth: Int
) {
    fun occupiesSlot(inventorySlot: Int, columns: Int): Boolean {
        if (columns <= 0 || inventorySlot < 0) {
            return false
        }
        val originX = slot % columns
        val originY = slot / columns
        val slotX = inventorySlot % columns
        val slotY = inventorySlot / columns
        return slotX >= originX && slotX < originX + width && slotY >= originY && slotY < originY + height
    }
}
