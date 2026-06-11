package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.ItemSlotSH
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.sound.BackpackSoundManager
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemRecord
import net.minecraft.item.ItemStack

class JukeboxSlotSH(slot: ModularSlot, private val wrapper: BackpackWrapper) : ItemSlotSH(slot) {
    private var initialUpdated = false
    
    override fun isItemValid(itemStack: ItemStack): Boolean =
        itemStack.item is ItemRecord

    override fun onSlotUpdate(stack: ItemStack, onlyAmountChanged: Boolean, client: Boolean, init: Boolean) {
        // Special handling for the initial update of slot, since the update does not actually change the record in the
        // slot
        if (initialUpdated && (!isItemValid(stack) || Minecraft.getMinecraft().player.inventory.itemStack.item != stack.item))
            BackpackSoundManager.stopRecord(wrapper)
        else
            initialUpdated = true
        super.onSlotUpdate(stack, onlyAmountChanged, client, init)
    }
}
