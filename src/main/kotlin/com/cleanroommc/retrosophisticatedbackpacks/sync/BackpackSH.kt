package com.cleanroommc.retrosophisticatedbackpacks.sync

import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.retrosophisticatedbackpacks.backpack.DisplaySide
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import net.minecraft.item.EnumDyeColor
import net.minecraft.network.PacketBuffer
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper

class BackpackSH(
    private val playerInv: PlayerMainInvWrapper,
    private val wrapper: BackpackWrapper,
    private val tileEntity: BackpackTileEntity? = null
) : SyncHandler() {
    companion object {
        const val UPDATE_SET_SORT_TYPE = 0
        const val UPDATE_SORT_INV = 1
        const val UPDATE_TRANSFER_TO_BACKPACK_INV = 2
        const val UPDATE_TRANSFER_TO_PLAYER_INV = 3
        const val UPDATE_TOGGLE_SETTINGS_CONTEXT = 4
        const val UPDATE_TOGGLE_SHIFT_CLICK_INTO_OPEN_TAB = 5
        const val UPDATE_TOGGLE_KEEP_TAB_OPEN = 6
        const val UPDATE_TOGGLE_KEEP_SEARCH_PHRASE = 7
        const val UPDATE_TOGGLE_ANOTHER_PLAYER_CAN_OPEN = 8
        const val UPDATE_ITEM_DISPLAY_SLOT = 9
        const val UPDATE_ITEM_DISPLAY_ROTATION = 10
        const val UPDATE_ITEM_DISPLAY_COLOR = 11
        const val UPDATE_ITEM_DISPLAY_SIDE = 12
        const val UPDATE_SEARCH_PHRASE = 13
    }

    override fun readOnClient(id: Int, buf: PacketBuffer) {}

    override fun readOnServer(id: Int, buf: PacketBuffer) {
        when (id) {
            UPDATE_SET_SORT_TYPE -> setSortType(buf)
            UPDATE_SORT_INV -> sortInventory(buf)
            UPDATE_TRANSFER_TO_BACKPACK_INV -> transferToBackpack(buf)
            UPDATE_TRANSFER_TO_PLAYER_INV -> transferToPlayerInventory(buf)
            UPDATE_TOGGLE_SETTINGS_CONTEXT -> wrapper.toggleSettingsContext()
            UPDATE_TOGGLE_SHIFT_CLICK_INTO_OPEN_TAB -> wrapper.toggleShiftClickIntoOpenTab()
            UPDATE_TOGGLE_KEEP_TAB_OPEN -> wrapper.toggleKeepTabOpen()
            UPDATE_TOGGLE_KEEP_SEARCH_PHRASE -> wrapper.toggleKeepSearchPhrase()
            UPDATE_TOGGLE_ANOTHER_PLAYER_CAN_OPEN -> wrapper.toggleAnotherPlayerCanOpen()
            UPDATE_ITEM_DISPLAY_SLOT -> updateItemDisplaySlot(buf)
            UPDATE_ITEM_DISPLAY_ROTATION -> updateItemDisplayRotation(buf)
            UPDATE_ITEM_DISPLAY_COLOR -> {
                if (Config.itemDisplayDisabled) {
                    return
                }
                wrapper.itemDisplayColor = buf.readEnumValue(EnumDyeColor::class.java)
                syncRenderState()
            }
            UPDATE_ITEM_DISPLAY_SIDE -> {
                if (Config.itemDisplayDisabled) {
                    return
                }
                wrapper.itemDisplaySide = buf.readEnumValue(DisplaySide::class.java)
                syncRenderState()
            }
            UPDATE_SEARCH_PHRASE -> {
                val phrase = buf.readString(50)
                wrapper.searchPhrase = phrase
            }
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

    private fun updateItemDisplaySlot(buf: PacketBuffer) {
        if (Config.itemDisplayDisabled) {
            return
        }
        val slotIndex = buf.readInt()
        val selected = buf.readBoolean()
        if (selected) {
            wrapper.selectItemDisplaySlot(slotIndex)
        } else {
            wrapper.unselectItemDisplaySlot(slotIndex)
        }
        syncRenderState()
    }

    private fun updateItemDisplayRotation(buf: PacketBuffer) {
        if (Config.itemDisplayDisabled) {
            return
        }
        wrapper.rotateItemDisplaySlot(buf.readInt(), buf.readBoolean())
        syncRenderState()
    }

    private fun syncRenderState() {
        tileEntity?.syncRenderState()
    }
}
