package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.wrapper.PlayerInvWrapper

class BackpackSH(private val playerInv: PlayerInvWrapper, private val wrapper: BackpackWrapper) : SyncHandler() {
    companion object {
        const val UPDATE_SET_SORT_TYPE = 0
        const val UPDATE_SORT_INV = 1
        const val UPDATE_TRANSFER_TO_BACKPACK_INV = 2
        const val UPDATE_TRANSFER_TO_PLAYER_INV = 3
    }

    override fun readOnClient(id: Int, buf: PacketBuffer) {}

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        when (id) {
            UPDATE_SET_SORT_TYPE -> setSortType(buf)
            UPDATE_SORT_INV -> sortInventory(buf)
            UPDATE_TRANSFER_TO_BACKPACK_INV -> transferToBackpack()
            UPDATE_TRANSFER_TO_PLAYER_INV -> transferToPlayerInventory()
            else -> {}
        }
    }

    fun setSortType(buf: PacketBuffer) {
        setSortType(buf.readEnumValue(SortType::class.java))
    }

    fun setSortType(sortType: SortType) {
        wrapper.sortType = sortType
    }

    // Must sort on client then send sort result to server side
    fun sortInventory(buf: PacketBuffer) {
        val size = wrapper.backpackInventorySize()
        
        for (i in 0 until size) {
            val stack = buf.readItemStack()
            
            wrapper.backpackItemStackHandler.setStackInSlot(i, stack)
        }
    }

    fun transferToBackpack() {
        BackpackInventoryHelper.transferPlayerInventoryToBackpack(wrapper, playerInv)
    }

    fun transferToPlayerInventory() {
        BackpackInventoryHelper.transferBackpackToPlayerInventory(wrapper, playerInv)
    }
}