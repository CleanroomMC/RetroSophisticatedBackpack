package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.item.CraftingUpgradeItem
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

class CraftingUpgradeWrapper() : UpgradeWrapper<CraftingUpgradeItem>(), ISidelessCapabilityProvider,
    INBTSerializable<NBTTagCompound> {
    companion object {
        private const val MATRIX_TAG = "Matrix"
        private const val CRAFTING_DEST_TAG = "CraftingDest"
    }

    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf()
    var craftingDestination = CraftingDestination.INVENTORY

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setByte(CRAFTING_DEST_TAG, craftingDestination.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        craftingDestination = CraftingDestination.entries[nbt.getByte(CRAFTING_DEST_TAG).toInt()]
    }

    enum class CraftingDestination {
        BACKPACK,
        INVENTORY;
    }
}