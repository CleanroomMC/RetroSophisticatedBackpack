package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

sealed class BasicUpgradeWrapper<T> : UpgradeWrapper<T>(), IToggleable, IBasicFilterable where T : UpgradeItem {
    override var enabled = true
    override var filterType = IBasicFilterable.FilterType.WHITELIST
    override val filterItems = ExposedItemStackHandler(9)

    override fun checkFilter(stack: ItemStack): Boolean =
        enabled && super.checkFilter(stack)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        super<IToggleable>.hasCapability(capability, facing) ||
                super<IBasicFilterable>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setBoolean(IToggleable.ENABLED_TAG, enabled)
        nbt.setTag(IBasicFilterable.FILTER_ITEMS_TAG, filterItems.serializeNBT())
        nbt.setByte(IBasicFilterable.FILTER_TYPE_TAG, filterType.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        enabled = nbt.getBoolean(IToggleable.ENABLED_TAG)
        filterItems.deserializeNBT(nbt.getCompoundTag(IBasicFilterable.FILTER_ITEMS_TAG))
        filterType = IBasicFilterable.FilterType.entries[nbt.getByte(IBasicFilterable.FILTER_TYPE_TAG).toInt()]
    }
}