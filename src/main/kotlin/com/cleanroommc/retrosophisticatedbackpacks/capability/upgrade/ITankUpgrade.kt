package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.items.IItemHandler

interface ITankUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    val tankCapacity: Int
    fun getTankCapacity(wrapper: BackpackWrapper): Int = tankCapacity
    fun getFluid(): FluidStack?
    fun fill(wrapper: BackpackWrapper, resource: FluidStack, doFill: Boolean, ignoreInOutLimit: Boolean = false): Int
    fun drain(wrapper: BackpackWrapper, maxDrain: Int, doDrain: Boolean, ignoreInOutLimit: Boolean = false): FluidStack?
    fun drain(wrapper: BackpackWrapper, resource: FluidStack, doDrain: Boolean, ignoreInOutLimit: Boolean = false): FluidStack?
    fun tick(wrapper: BackpackWrapper, world: World)
    fun getInventory(): IItemHandler
    fun interactWithCursorStack(player: EntityPlayer, wrapper: BackpackWrapper)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ITANK_UPGRADE_CAPABILITY

    object Impl : ITankUpgrade {
        override val tankCapacity = 0
        override fun getFluid(): FluidStack? = null
        override fun fill(wrapper: BackpackWrapper, resource: FluidStack, doFill: Boolean, ignoreInOutLimit: Boolean): Int = 0
        override fun drain(wrapper: BackpackWrapper, maxDrain: Int, doDrain: Boolean, ignoreInOutLimit: Boolean): FluidStack? = null
        override fun drain(wrapper: BackpackWrapper, resource: FluidStack, doDrain: Boolean, ignoreInOutLimit: Boolean): FluidStack? = null
        override fun tick(wrapper: BackpackWrapper, world: World) {}
        override fun getInventory(): IItemHandler = net.minecraftforge.items.ItemStackHandler(0)
        override fun interactWithCursorStack(player: EntityPlayer, wrapper: BackpackWrapper) {}
        override fun serializeNBT(): NBTTagCompound = NBTTagCompound()
        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }
}
