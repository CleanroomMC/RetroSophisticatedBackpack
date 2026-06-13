package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack

interface IPumpUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    var enabled: Boolean
    var isInput: Boolean
    var interactWithHand: Boolean
    var interactWithWorld: Boolean
    var interactWithFluidHandlers: Boolean
    val fluidFilters: List<FluidStack?>

    fun tick(player: EntityPlayer?, wrapper: BackpackWrapper, world: World, pos: BlockPos)
    fun setFluidFilter(slot: Int, fluid: FluidStack?)
    fun fluidMatches(fluid: FluidStack): Boolean

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IPUMP_UPGRADE_CAPABILITY

    object Impl : IPumpUpgrade {
        override var enabled = false
        override var isInput = true
        override var interactWithHand = false
        override var interactWithWorld = false
        override var interactWithFluidHandlers = false
        override val fluidFilters: List<FluidStack?> = emptyList()
        override fun tick(player: EntityPlayer?, wrapper: BackpackWrapper, world: World, pos: BlockPos) {}
        override fun setFluidFilter(slot: Int, fluid: FluidStack?) {}
        override fun fluidMatches(fluid: FluidStack): Boolean = false
        override fun serializeNBT(): NBTTagCompound = NBTTagCompound()
        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }
}
