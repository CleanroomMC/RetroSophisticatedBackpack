package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.IItemHandler

interface IAnvilUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    var shouldShiftClickIntoStorage: Boolean
    var itemName: String
    val maximumCost: Int
    val materialCost: Int

    fun getInventory(): IItemHandler
    fun updateRepairOutput(player: EntityPlayer, world: World): ItemStack
    fun canTakeResult(player: EntityPlayer): Boolean
    fun takeResult(player: EntityPlayer, world: World): ItemStack

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IANVIL_UPGRADE_CAPABILITY

    object Impl : IAnvilUpgrade {
        override var shouldShiftClickIntoStorage = false
        override var itemName = ""
        override val maximumCost = 0
        override val materialCost = 0
        override fun getInventory(): IItemHandler = net.minecraftforge.items.ItemStackHandler(0)
        override fun updateRepairOutput(player: EntityPlayer, world: World): ItemStack = ItemStack.EMPTY
        override fun canTakeResult(player: EntityPlayer): Boolean = false
        override fun takeResult(player: EntityPlayer, world: World): ItemStack = ItemStack.EMPTY
        override fun serializeNBT(): NBTTagCompound = NBTTagCompound()
        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }
}
