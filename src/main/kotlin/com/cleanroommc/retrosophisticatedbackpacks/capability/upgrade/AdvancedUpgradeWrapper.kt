package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants

sealed class AdvancedUpgradeWrapper<T> : UpgradeWrapper<T>(), IToggleable, IAdvanceFilterable where T : UpgradeItem {
    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(
            Capabilities.ADVANCE_FILTERABLE_CAPABILITY,
            Capabilities.BASIC_FILTERABLE_CAPABILITY,
            Capabilities.TOGGLEABLE_CAPABILITY
        )

    override var enabled = true
    override var filterType = IBasicFilterable.FilterType.WHITELIST
    override val filterItems: ExposedItemStackHandler = ExposedItemStackHandler(16)
    override var matchType = IAdvanceFilterable.MatchType.ITEM
    override var oreDictEntries = mutableListOf<String>()
    override var ignoreDurability = true
    override var ignoreNBT = true

    override fun checkFilter(stack: ItemStack): Boolean =
        enabled && super.checkFilter(stack)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setBoolean(IToggleable.ENABLED_TAG, enabled)
        nbt.setTag(IBasicFilterable.FILTER_ITEMS_TAG, filterItems.serializeNBT())
        nbt.setByte(IBasicFilterable.FILTER_TYPE_TAG, filterType.ordinal.toByte())
        nbt.setByte(IAdvanceFilterable.MATCH_TYPE_TAG, matchType.ordinal.toByte())
        nbt.setBoolean(IAdvanceFilterable.IGNORE_DURABILITY_TAG, ignoreDurability)
        nbt.setBoolean(IAdvanceFilterable.IGNORE_NBT_TAG, ignoreNBT)

        val oreDictList = NBTTagList()

        for (entry in oreDictEntries)
            oreDictList.appendTag(NBTTagString(entry))

        nbt.setTag(IAdvanceFilterable.ORE_DICT_LIST_TAG, oreDictList)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        enabled = nbt.getBoolean(IToggleable.ENABLED_TAG)
        filterItems.deserializeNBT(nbt.getCompoundTag(IBasicFilterable.FILTER_ITEMS_TAG))
        filterType = IBasicFilterable.FilterType.entries[nbt.getByte(IBasicFilterable.FILTER_TYPE_TAG).toInt()]
        matchType = IAdvanceFilterable.MatchType.entries[nbt.getByte(IAdvanceFilterable.MATCH_TYPE_TAG).toInt()]
        ignoreDurability = nbt.getBoolean(IAdvanceFilterable.IGNORE_DURABILITY_TAG)
        ignoreNBT = nbt.getBoolean(IAdvanceFilterable.IGNORE_NBT_TAG)

        val oreDictList = nbt.getTagList(IAdvanceFilterable.ORE_DICT_LIST_TAG, Constants.NBT.TAG_STRING)

        for (stringNBT in oreDictList)
            oreDictEntries.add((stringNBT as NBTTagString).string)
    }
}