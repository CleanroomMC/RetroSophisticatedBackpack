package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.IItemHandler

interface IBatteryUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    val energyStored: Int
    val canExtractEnergy: Boolean
    val canReceiveEnergy: Boolean

    fun getMaxEnergyStored(wrapper: BackpackWrapper): Int
    fun getMaxEnergyStored(wrapper: BackpackWrapper, stackMultiplier: Int): Int
    fun receiveEnergy(wrapper: BackpackWrapper, maxReceive: Int, simulate: Boolean): Int
    fun extractEnergy(wrapper: BackpackWrapper, maxExtract: Int, simulate: Boolean): Int
    fun tick(wrapper: BackpackWrapper, world: World)
    fun getInventory(): IItemHandler

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IBATTERY_UPGRADE_CAPABILITY

    object Impl : IBatteryUpgrade {
        override val energyStored = 0
        override val canExtractEnergy = false
        override val canReceiveEnergy = false
        override fun getMaxEnergyStored(wrapper: BackpackWrapper): Int = 0
        override fun getMaxEnergyStored(wrapper: BackpackWrapper, stackMultiplier: Int): Int = 0
        override fun receiveEnergy(wrapper: BackpackWrapper, maxReceive: Int, simulate: Boolean): Int = 0
        override fun extractEnergy(wrapper: BackpackWrapper, maxExtract: Int, simulate: Boolean): Int = 0
        override fun tick(wrapper: BackpackWrapper, world: World) {}
        override fun getInventory(): IItemHandler = net.minecraftforge.items.ItemStackHandler(0)
        override fun serializeNBT(): NBTTagCompound = NBTTagCompound()
        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }
}
