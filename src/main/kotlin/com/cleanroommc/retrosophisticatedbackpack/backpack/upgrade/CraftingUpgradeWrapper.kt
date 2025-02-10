package com.cleanroommc.retrosophisticatedbackpack.backpack.upgrade

import com.cleanroommc.retrosophisticatedbackpack.handlers.CapabilityHandler
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable

class CraftingUpgradeWrapper() : ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        private const val MATRIX_TAG = "Matrix"
        private const val CRAFTING_DEST_TAG = "CraftingDest"
    }

    var craftingDestination = CraftingDestination.INVENTORY

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setByte(CRAFTING_DEST_TAG, craftingDestination.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        craftingDestination = CraftingDestination.entries[nbt.getByte(CRAFTING_DEST_TAG).toInt()]
    }

    override fun hasCapability(
        capability: Capability<*>,
        facing: EnumFacing?
    ): Boolean =
        facing == null && capability == CapabilityHandler.CRAFTING_ITEM_HANDLER_CAPABILITY

    override fun <T> getCapability(
        capability: Capability<T>,
        facing: EnumFacing?
    ): T? =
        if (hasCapability(capability, null)) CapabilityHandler.CRAFTING_ITEM_HANDLER_CAPABILITY!!.cast(this) else null

    enum class CraftingDestination {
        BACKPACK,
        INVENTORY;
    }
}