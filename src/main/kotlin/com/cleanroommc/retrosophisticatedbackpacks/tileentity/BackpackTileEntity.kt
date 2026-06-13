package com.cleanroommc.retrosophisticatedbackpacks.tileentity

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.screen.UISettings
import com.cleanroommc.modularui.value.sync.PanelSyncManager
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.block.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackFluidHandler
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackEnergyStorage
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHolder
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.util.ITickable
import net.minecraft.tileentity.TileEntityLockableLoot
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.items.IItemHandler

class BackpackTileEntity(val wrapper: BackpackWrapper = BackpackWrapper()) :
    TileEntityLockableLoot(),
    IItemHandler,
    ITickable,
    IGuiHolder<PosGuiData> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "backpackInventory"
    }

    fun openGui(player: EntityPlayer) {
        TileEntityGuiFactory.INSTANCE.open(player, pos)
    }

    override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState): Boolean =
        oldState.block != newSate.block

    override fun getUpdatePacket(): SPacketUpdateTileEntity =
        SPacketUpdateTileEntity(pos, 3, updateTag)

    override fun getUpdateTag(): NBTTagCompound =
        writeToNBT(NBTTagCompound())

    override fun onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity) {
        super.onDataPacket(net, pkt)
        handleUpdateTag(pkt.nbtCompound)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        when {
            capability == Capabilities.BACKPACK_CAPABILITY -> wrapper as T
            isExternalConnectionBlocked(facing) -> null
            capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> this as T
            capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && wrapper.hasTankUpgrade() -> BackpackFluidHandler(wrapper) as T
            capability == CapabilityEnergy.ENERGY && wrapper.hasBatteryUpgrade() -> BackpackEnergyStorage(wrapper) as T
            else -> null
        }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.BACKPACK_CAPABILITY ||
                !isExternalConnectionBlocked(facing) &&
                (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
                        capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && wrapper.hasTankUpgrade() ||
                        capability == CapabilityEnergy.ENERGY && wrapper.hasBatteryUpgrade())

    private fun isExternalConnectionBlocked(facing: EnumFacing?): Boolean {
        if (facing == null || !hasWorld()) {
            return false
        }
        return Config.isConnectionBlockDisallowed(world.getBlockState(pos.offset(facing)).block)
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag(BACKPACK_INVENTORY_TAG, wrapper.serializeNBT())
        return super.writeToNBT(compound)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        if (compound.hasKey(BACKPACK_INVENTORY_TAG)) {
            wrapper.deserializeNBT(compound.getCompoundTag(BACKPACK_INVENTORY_TAG))
        } else {
            RetroSophisticatedBackpacks.LOGGER.warn("Backpack tile entity's NBT does not have backpack wrapper info")
        }
    }

    override fun buildUI(
        data: PosGuiData,
        syncManager: PanelSyncManager,
        uiSettings: UISettings
    ): ModularPanel {
        val backpackInv = getCapability(Capabilities.BACKPACK_CAPABILITY, null)!!
        val containerSupplier = { BackpackContainer(backpackInv, null, tilePos = data.blockPos) }
        uiSettings.customContainer(containerSupplier)
        uiSettings.canInteractWith(::isUsableByPlayer)
        val holder: BackpackGuiHolder.TileEntityGuiHolder = BackpackGuiHolder.TileEntityGuiHolder(backpackInv)
        return holder.buildUI(data, syncManager, uiSettings)
    }

    fun syncRenderState() {
        if (!hasWorld()) {
            return
        }
        markDirty()
        val state = world.getBlockState(pos)
        world.notifyBlockUpdate(pos, state, state, 3)
    }

    override fun hasCustomName(): Boolean =
        wrapper.customName != null

    override fun getName(): String =
        if (hasCustomName()) wrapper.customName!! else "container.backpack".asTranslationKey()

    override fun getDisplayName(): ITextComponent =
        if (hasCustomName()) TextComponentString(wrapper.customName!!)
        else TextComponentTranslation("container.backpack".asTranslationKey())

    override fun update() {
        if (!world.isRemote) {
            wrapper.tickUpgrades(null, world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, pos)
            syncTankState()
            syncBatteryState()
        }
    }

    override fun getSlots(): Int =
        wrapper.slots

    override fun getSizeInventory(): Int =
        wrapper.backpackInventorySize()

    override fun isEmpty(): Boolean =
        wrapper.backpackItemStackHandler.inventory.all(ItemStack::isEmpty)

    override fun getStackInSlot(slot: Int): ItemStack =
        wrapper.getStackInSlot(slot)

    override fun getInventoryStackLimit(): Int =
        wrapper.getTotalStackMultiplier() * 64

    override fun getItems(): NonNullList<ItemStack> =
        wrapper.backpackItemStackHandler.inventory

    override fun insertItem(
        slot: Int,
        stack: ItemStack,
        simulate: Boolean
    ): ItemStack {
        if (!wrapper.canInsert(stack)) {
            return stack
        }

        val stack = wrapper.onBeforeInsert(stack)
        if (stack.isEmpty) {
            return ItemStack.EMPTY
        }
        val remaining = wrapper.backpackItemStackHandler.prioritizedInsertion(slot, stack, simulate)
        return wrapper.onInsertRemainder(remaining)
    }

    override fun extractItem(
        slot: Int,
        amount: Int,
        simulate: Boolean
    ): ItemStack =
        if (wrapper.canExtract(slot)) wrapper.extractItem(slot, amount, simulate)
        else ItemStack.EMPTY

    override fun getSlotLimit(slot: Int): Int =
        wrapper.getSlotLimit(slot)

    private fun syncTankState() {
        val state = world.getBlockState(pos)
        if (state.block !is BackpackBlock) {
            return
        }
        val (leftTank, rightTank) = wrapper.tankRenderSides()
        if (state.getValue(BackpackBlock.LEFT_TANK) != leftTank || state.getValue(BackpackBlock.RIGHT_TANK) != rightTank) {
            world.setBlockState(
                pos,
                state.withProperty(BackpackBlock.LEFT_TANK, leftTank).withProperty(BackpackBlock.RIGHT_TANK, rightTank),
                3
            )
        }
    }

    private fun syncBatteryState() {
        val state = world.getBlockState(pos)
        if (state.block !is BackpackBlock) {
            return
        }
        val battery = wrapper.hasBatteryUpgrade()
        if (state.getValue(BackpackBlock.BATTERY) != battery) {
            world.setBlockState(pos, state.withProperty(BackpackBlock.BATTERY, battery), 3)
        }
    }

    override fun createContainer(
        playerInventory: InventoryPlayer,
        playerIn: EntityPlayer
    ): Container {
        throw UnsupportedOperationException("Backpack tile entities do not have a vanilla GUI, if you're attempting to open a GUI, use BackpackTileEntity.openGui(EntityPlayer) instead")
    }

    override fun getGuiID(): String {
        throw UnsupportedOperationException("Backpack tile entities do not have a vanilla GUI, if you're attempting to open a GUI, use BackpackTileEntity.openGui(EntityPlayer) instead")
    }
}
