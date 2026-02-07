package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

abstract class BasicUpgradeWrapper<T> : UpgradeWrapper<T>(), IToggleable where T : UpgradeItem {
    companion object {
        const val FILTER_ITEMS_TAG = "FilterItems"
        const val FILTER_TYPE_TAG = "FilterType"
    }
    
    override var enabled = true
    var filterType = FilterType.WHITELIST
    open val filterItems = ExposedItemStackHandler(9)

    open fun checkFilter(stack: ItemStack): Boolean =
        enabled && when (filterType) {
            FilterType.WHITELIST -> filterItems.inventory.any { ItemStack.areItemsEqualIgnoreDurability(it, stack) }
            FilterType.BLACKLIST -> filterItems.inventory.none { ItemStack.areItemsEqualIgnoreDurability(it, stack) }
        }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        super<IToggleable>.hasCapability(capability, facing) ||
                super<UpgradeWrapper>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(IToggleable.ENABLED_TAG, enabled)
        nbt.setTag(FILTER_ITEMS_TAG, filterItems.serializeNBT())
        nbt.setByte(FILTER_TYPE_TAG, filterType.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        enabled = nbt.getBoolean(IToggleable.ENABLED_TAG)
        filterItems.deserializeNBT(nbt.getCompoundTag(FILTER_ITEMS_TAG))
        filterType = FilterType.entries[nbt.getByte(FILTER_TYPE_TAG).toInt()]
    }

    enum class FilterType {
        WHITELIST,
        BLACKLIST;
    }
    
    object Impl : BasicUpgradeWrapper<UpgradeItem>() {
        override val settingsLangKey: String = ""
    }
}
