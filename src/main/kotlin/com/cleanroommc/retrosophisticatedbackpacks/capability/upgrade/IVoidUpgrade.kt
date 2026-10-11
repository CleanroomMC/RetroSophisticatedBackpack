package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.ItemStackHandler

sealed interface IVoidUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        const val TRANSFER_SOURCE_TAG = "TransferSource"

        const val VOID_TYPE_TAG = "VoidType"
    }

    var transferSource: TransferSource
    var voidType: VoidType
    
    fun canVoid(stack: ItemStack, transferSource: TransferSource): Boolean

    fun tryVoid(
        wrapper: BackpackWrapper,
        inventory: ItemStackHandler,
        stack: ItemStack,
        transferSource: TransferSource
    ): ItemStack {
        if (!canVoid(stack, transferSource)) return stack

        return if (voidType == VoidType.OVERFLOW) {
            var totalCount = 0
            for (i in 0 until inventory.slots) {
                val s = inventory.getStackInSlot(i)
                if (ItemHandlerHelper.canItemStacksStack(stack, s)) {
                    totalCount += s.count
                }
            }
            
            val maxCapacity = stack.maxStackSize * wrapper.getTotalStackMultiplier()
            val canFit = maxCapacity - totalCount
            
            if (canFit <= 0) ItemStack.EMPTY
            else if (stack.count <= canFit) stack
            else ItemHandlerHelper.copyStackWithSize(stack, canFit)
        } else ItemStack.EMPTY
    }

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
