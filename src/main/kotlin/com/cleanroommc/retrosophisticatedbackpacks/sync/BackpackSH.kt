package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper

class BackpackSH(private val playerInv: PlayerMainInvWrapper, private val wrapper: BackpackWrapper) : SyncHandler() {
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
            UPDATE_SORT_INV -> sortInventory()
            UPDATE_TRANSFER_TO_BACKPACK_INV -> transferToBackpack(buf)
            UPDATE_TRANSFER_TO_PLAYER_INV -> transferToPlayerInventory(buf)
            else -> {}
        }
    }

    fun setSortType(buf: PacketBuffer) {
        setSortType(buf.readEnumValue(SortType::class.java))
    }

    fun setSortType(sortType: SortType) {
        wrapper.sortType = sortType
    }

    // Sorted on the server, from the server's own contents. The client's copy of the backpack can be out of
    // date (or tampered with), so it must never be written into the backpack; the slots sync the result back.
    fun sortInventory() {
        BackpackInventoryHelper.sortInventory(wrapper)
    }

    fun transferToBackpack(transferMatched: Boolean) {
        BackpackInventoryHelper.transferPlayerInventoryToBackpack(wrapper, playerInv, transferMatched)
    }

    fun transferToBackpack(buf: PacketBuffer) {
        val transferMatched = buf.readBoolean()

        BackpackInventoryHelper.transferPlayerInventoryToBackpack(wrapper, playerInv, transferMatched)
    }

    fun transferToPlayerInventory(transferMatched: Boolean) {
        BackpackInventoryHelper.transferBackpackToPlayerInventory(wrapper, playerInv, transferMatched)
    }

    fun transferToPlayerInventory(buf: PacketBuffer) {
        val transferMatched = buf.readBoolean()

        BackpackInventoryHelper.transferBackpackToPlayerInventory(wrapper, playerInv, transferMatched)
    }
}