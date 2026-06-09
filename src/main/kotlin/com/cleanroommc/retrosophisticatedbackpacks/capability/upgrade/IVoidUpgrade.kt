package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

sealed interface IVoidUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        const val TRANSFER_SOURCE_TAG = "TransferSource"

        const val VOID_TYPE_TAG = "VoidType"
    }

    var transferSource: TransferSource
    var voidType: VoidType

    fun canVoid(stack: ItemStack, transferSource: TransferSource, voidType: VoidType): Boolean

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IVOID_UPGRADE_CAPABILITY

    enum class TransferSource {
        UPGRADE_OR_WORLD_INTERACTION,
        ALL
    }

    enum class VoidType {
        ANY,
        OVERFLOW
    }
}
