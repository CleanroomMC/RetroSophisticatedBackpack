package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

interface IToolSwapperUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    fun onBlockClick(player: EntityPlayer, wrapper: BackpackWrapper, pos: BlockPos, state: IBlockState): Boolean = false
    fun onAttackEntity(player: EntityPlayer, wrapper: BackpackWrapper): Boolean = false
    fun canProcessBlockInteract(): Boolean = false
    fun onBlockInteract(player: EntityPlayer, wrapper: BackpackWrapper, world: World, pos: BlockPos, state: IBlockState): Boolean = false
    fun canProcessEntityInteract(): Boolean = false
    fun onEntityInteract(player: EntityPlayer, wrapper: BackpackWrapper, entity: Entity): Boolean = false

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ITOOL_SWAPPER_UPGRADE_CAPABILITY

    object Impl : IToolSwapperUpgrade {
        override fun serializeNBT(): NBTTagCompound = NBTTagCompound()
        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }
}
