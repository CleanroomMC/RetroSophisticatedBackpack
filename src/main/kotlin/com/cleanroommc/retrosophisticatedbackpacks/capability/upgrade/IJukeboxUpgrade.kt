package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.IItemHandler

interface IJukeboxUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    val discInventory: IItemHandler

    fun play(world: World, pos: BlockPos)
    fun play(entity: Entity)
    fun stop(world: World, pos: BlockPos)
    fun next()
    fun previous()
    fun tick(world: World, pos: BlockPos?)

    object Impl : IJukeboxUpgrade {
        override val discInventory: IItemHandler = net.minecraftforge.items.wrapper.EmptyHandler.INSTANCE
        override fun play(world: World, pos: BlockPos) {}
        override fun play(entity: Entity) {}
        override fun stop(world: World, pos: BlockPos) {}
        override fun next() {}
        override fun previous() {}
        override fun tick(world: World, pos: BlockPos?) {}
        override fun serializeNBT() = NBTTagCompound()
        override fun deserializeNBT(nbt: NBTTagCompound) {}
        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) = false
        override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? = null
    }
}
