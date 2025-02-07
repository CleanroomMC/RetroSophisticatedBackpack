package com.cleanroommc.retrosophisticatedbackpack.tileentity

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpack.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpack.backpack.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpack.handlers.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpack.inventory.BackpackGuiHolder
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class BackpackTileEntity(val backpackWrapper: BackpackWrapper = BackpackWrapper()) : TileEntity(), IGuiHolder<PosGuiData> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "backpackInventory"
    }
    
    fun openGui(player: EntityPlayer) {
        TileEntityGuiFactory.INSTANCE.open(player, pos)
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity =
        SPacketUpdateTileEntity(pos, 3, updateTag)

    override fun getUpdateTag(): NBTTagCompound =
        writeToNBT(NBTTagCompound())

    override fun onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity) {
        super.onDataPacket(net, pkt)
        handleUpdateTag(pkt.nbtCompound)
    }

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        backpackWrapper.getCapability(capability, facing)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        backpackWrapper.hasCapability(capability, facing)

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag(BACKPACK_INVENTORY_TAG, backpackWrapper.serializeNBT())
        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        if (compound.hasKey(BACKPACK_INVENTORY_TAG)) {
            backpackWrapper.deserializeNBT(compound.getCompoundTag(BACKPACK_INVENTORY_TAG))
        } else {
            RetroSophisticatedBackpacks.LOGGER.warn("Backpack tile entity's NBT does not have backpack wrapper info")
        }
    }

    override fun buildUI(
        data: PosGuiData,
        syncManager: PanelSyncManager
    ): ModularPanel {
        val backpackInv = getCapability(CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY!!, null)!!
        return BackpackGuiHolder.TileEntityGuiHolder(backpackInv).buildUI(data, syncManager)
    }
}