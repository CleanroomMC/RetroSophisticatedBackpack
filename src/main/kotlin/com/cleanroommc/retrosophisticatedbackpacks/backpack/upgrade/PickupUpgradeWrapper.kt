package com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.backpack.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.backpack.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.backpack.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.items.PickupUpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

class PickupUpgradeWrapper : UpgradeWrapper<PickupUpgradeItem>(), IToggleable, ISidelessCapabilityProvider,
    INBTSerializable<NBTTagCompound> {
    companion object {
        private const val FILTER_ITEMS_TAG = "FilterItems"
        private const val FILTER_TYPE_TAG = "FilterType"
    }

    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(Capabilities.PICKUP_UPGRADE_CAPABILITY, Capabilities.TOGGLEABLE_CAPABILITY)

    override var enabled = true
    var filterType = FilterType.WHITELIST
    val filterItems = ExposedItemStackHandler(9)

    fun canPickup(stack: ItemStack): Boolean {
        if (!enabled)
            return false

        return when (filterType) {
            FilterType.WHITELIST -> filterItems.inventory.any { it.isItemEqual(stack) }
            FilterType.BLACKLIST -> filterItems.inventory.none { it.isItemEqual(stack) }
        }
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setBoolean(IToggleable.ENABLED_TAG, enabled)
        nbt.setTag(FILTER_ITEMS_TAG, filterItems.serializeNBT())
        nbt.setByte(FILTER_TYPE_TAG, filterType.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        enabled = nbt.getBoolean(IToggleable.ENABLED_TAG)
        filterItems.deserializeNBT(nbt.getCompoundTag(FILTER_ITEMS_TAG))
        filterType = FilterType.entries[nbt.getByte(FILTER_TYPE_TAG).toInt()]
    }

    enum class FilterType {
        WHITELIST,
        BLACKLIST;
    }
}