package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.CraftingUpgradeItem
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class CraftingUpgradeWrapper() : UpgradeWrapper<CraftingUpgradeItem>() {
    companion object {
        private const val MATRIX_TAG = "Matrix"
        private const val CRAFTING_DEST_TAG = "CraftingDest"
    }

    override val settingsLangKey: String = ""

    var craftingDestination = CraftingDestination.INVENTORY

    var craftMatrix = ExposedItemStackHandler(10)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.CRAFTING_ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setByte(CRAFTING_DEST_TAG, craftingDestination.ordinal.toByte())
        nbt.setTag(MATRIX_TAG, craftMatrix.serializeNBT())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        craftingDestination = CraftingDestination.entries[nbt.getByte(CRAFTING_DEST_TAG).toInt()]
        craftMatrix.deserializeNBT(nbt.getCompoundTag(MATRIX_TAG))
    }

    enum class CraftingDestination {
        BACKPACK,
        INVENTORY;
    }
}