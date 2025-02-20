package com.cleanroommc.retrosophisticatedbackpacks.tileentity

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHolder
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability

class BackpackTileEntity(val backpackWrapper: BackpackWrapper = BackpackWrapper()) : TileEntity(),
    IInventory by backpackWrapper, IGuiHolder<PosGuiData> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "backpackInventory"
    }

    fun openGui(player: EntityPlayer) {
        TileEntityGuiFactory.INSTANCE.open(player, pos)
    }

    override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState): Boolean =
        oldState.block != newSate.block

    override fun closeInventory(player: EntityPlayer) {
        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).block, false)
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
        val backpackInv = getCapability(Capabilities.BACKPACK_CAPABILITY, null)!!
        return BackpackGuiHolder.TileEntityGuiHolder(backpackInv).buildUI(data, syncManager)
    }

    override fun markDirty() {
        backpackWrapper.markDirty()
    }

    override fun getDisplayName(): ITextComponent =
        backpackWrapper.getDisplayName()
}