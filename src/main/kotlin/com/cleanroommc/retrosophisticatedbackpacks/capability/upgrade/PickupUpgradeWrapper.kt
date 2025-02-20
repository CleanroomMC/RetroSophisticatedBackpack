package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.PickupUpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.Capability

class PickupUpgradeWrapper : UpgradeWrapper<PickupUpgradeItem>(),
    IToggleable,
    IBasicFilterable,
    ISidelessCapabilityProvider {
    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(
            Capabilities.PICKUP_UPGRADE_CAPABILITY,
            Capabilities.BASIC_FILTERABLE_CAPABILITY,
            Capabilities.TOGGLEABLE_CAPABILITY
        )

    override var enabled = true
    override var filterType = IBasicFilterable.FilterType.WHITELIST
    override val filterItems = ExposedItemStackHandler(9)

    override fun checkFilter(stack: ItemStack): Boolean {
        if (!enabled)
            return false

        return when (filterType) {
            IBasicFilterable.FilterType.WHITELIST ->
                filterItems.inventory.any { ItemStack.areItemsEqualIgnoreDurability(it, stack) }

            IBasicFilterable.FilterType.BLACKLIST ->
                filterItems.inventory.none { ItemStack.areItemsEqualIgnoreDurability(it, stack) }
        }
    }

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