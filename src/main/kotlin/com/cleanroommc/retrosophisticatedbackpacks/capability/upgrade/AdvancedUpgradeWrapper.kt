package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants

abstract class AdvancedUpgradeWrapper<T>(filterSlots: Int = 16, override val slotsInRow: Int = 4) :
    UpgradeWrapper<T>(), IToggleable, IAdvancedFilterable where T : UpgradeItem {
    override var enabled = true
    override var filterType = IBasicFilterable.FilterType.WHITELIST
    override val filterItems: ExposedItemStackHandler = ExposedItemStackHandler(filterSlots)
    override var matchType = IAdvancedFilterable.MatchType.ITEM
    override var oreDictEntries = mutableListOf<String>()
    override var ignoreDurability = true
    override var ignoreNBT = true

    override fun checkFilter(stack: ItemStack): Boolean =
        enabled && super.checkFilter(stack)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        super<IToggleable>.hasCapability(capability, facing) ||
                super<IAdvancedFilterable>.hasCapability(capability, facing) ||
                super<UpgradeWrapper>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(IToggleable.ENABLED_TAG, enabled)
        nbt.setTag(IBasicFilterable.FILTER_ITEMS_TAG, filterItems.serializeNBT())
        nbt.setByte(IBasicFilterable.FILTER_TYPE_TAG, filterType.ordinal.toByte())
        nbt.setByte(IAdvancedFilterable.MATCH_TYPE_TAG, matchType.ordinal.toByte())
        nbt.setBoolean(IAdvancedFilterable.IGNORE_DURABILITY_TAG, ignoreDurability)
        nbt.setBoolean(IAdvancedFilterable.IGNORE_NBT_TAG, ignoreNBT)

        val oreDictList = NBTTagList()

        for (entry in oreDictEntries)
            oreDictList.appendTag(NBTTagString(entry))

        nbt.setTag(IAdvancedFilterable.ORE_DICT_LIST_TAG, oreDictList)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (nbt.hasKey(IToggleable.ENABLED_TAG))
            enabled = nbt.getBoolean(IToggleable.ENABLED_TAG)
        if (nbt.hasKey(IBasicFilterable.FILTER_ITEMS_TAG))
            filterItems.deserializeNBT(nbt.getCompoundTag(IBasicFilterable.FILTER_ITEMS_TAG))
        if (nbt.hasKey(IBasicFilterable.FILTER_TYPE_TAG))
            filterType = IBasicFilterable.FilterType.entries.getOrElse(nbt.getByte(IBasicFilterable.FILTER_TYPE_TAG).toInt()) { filterType }
        if (nbt.hasKey(IAdvancedFilterable.MATCH_TYPE_TAG))
            matchType = IAdvancedFilterable.MatchType.entries.getOrElse(nbt.getByte(IAdvancedFilterable.MATCH_TYPE_TAG).toInt()) { matchType }
        if (nbt.hasKey(IAdvancedFilterable.IGNORE_DURABILITY_TAG))
            ignoreDurability = nbt.getBoolean(IAdvancedFilterable.IGNORE_DURABILITY_TAG)
        if (nbt.hasKey(IAdvancedFilterable.IGNORE_NBT_TAG))
            ignoreNBT = nbt.getBoolean(IAdvancedFilterable.IGNORE_NBT_TAG)

        if (!nbt.hasKey(IAdvancedFilterable.ORE_DICT_LIST_TAG))
            return
        val oreDictList = nbt.getTagList(IAdvancedFilterable.ORE_DICT_LIST_TAG, Constants.NBT.TAG_STRING)

        oreDictEntries.clear()
        for (stringNBT in oreDictList)
            oreDictEntries.add((stringNBT as NBTTagString).string)
    }
}
