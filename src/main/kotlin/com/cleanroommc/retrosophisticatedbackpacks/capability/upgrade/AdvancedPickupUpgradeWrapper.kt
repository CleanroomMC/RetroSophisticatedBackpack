package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.PickupUpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants
import net.minecraftforge.oredict.OreDictionary

class AdvancedPickupUpgradeWrapper : UpgradeWrapper<PickupUpgradeItem>(),
    ISidelessCapabilityProvider,
    IAdvanceFilterable,
    IToggleable {
    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(
            Capabilities.ADVANCED_PICKUP_UPGRADE_CAPABILITY, Capabilities.ADVANCE_FILTERABLE_CAPABILITY,
            Capabilities.BASIC_FILTERABLE_CAPABILITY, Capabilities.TOGGLEABLE_CAPABILITY
        )

    override var enabled = true
    override var filterType = IBasicFilterable.FilterType.WHITELIST
    override val filterItems: ExposedItemStackHandler = ExposedItemStackHandler(16)
    override var matchType = IAdvanceFilterable.MatchType.ITEM
    override var oreDictEntries = mutableListOf<String>()
    override var ignoreDurability = true
    override var ignoreNBT = true

    override fun checkFilter(stack: ItemStack): Boolean {
        if (!enabled)
            return false

        return when (matchType) {
            IAdvanceFilterable.MatchType.ITEM -> matchItem(stack)
            IAdvanceFilterable.MatchType.MOD -> matchMod(stack)
            IAdvanceFilterable.MatchType.ORE_DICT -> matchOreDict(stack)
        }
    }

    private fun matchItem(stack: ItemStack): Boolean {
        val filterResult = BooleanArray(16)

        for ((i, filterStack) in filterItems.inventory.withIndex()) {
            if (filterStack.item != stack.item)
                continue

            filterResult[i] = matchItemInfo(stack, filterStack)
        }

        return when (filterType) {
            IBasicFilterable.FilterType.WHITELIST -> filterResult.any { it }
            IBasicFilterable.FilterType.BLACKLIST -> filterResult.none { it }
        }
    }

    private fun matchMod(stack: ItemStack): Boolean {
        val filterResult = BooleanArray(16)

        for ((i, filterStack) in filterItems.inventory.withIndex()) {
            filterResult[i] = stack.item.registryName?.namespace == filterStack.item.registryName?.namespace
        }

        return when (filterType) {
            IBasicFilterable.FilterType.WHITELIST -> filterResult.any { it }
            IBasicFilterable.FilterType.BLACKLIST -> filterResult.none { it }
        }
    }

    private fun matchOreDict(stack: ItemStack): Boolean {
        val stackOreDictionaries = OreDictionary.getOreIDs(stack).map { OreDictionary.getOreName(it) }

        for (oreDictEntry in oreDictEntries) {
            val regex = Regex(oreDictEntry)
            val matchResult = stackOreDictionaries.any { regex.matches(it) }

            if (filterType == IBasicFilterable.FilterType.WHITELIST && matchResult)
                return true
            if (filterType == IBasicFilterable.FilterType.BLACKLIST && matchResult)
                return false
        }

        return false
    }

    private fun matchItemInfo(stack: ItemStack, filterStack: ItemStack): Boolean {
        if (filterStack.isEmpty)
            return false

        var flag = if (ignoreDurability) {
            ItemStack.areItemsEqualIgnoreDurability(filterStack, stack)
        } else {
            filterStack.isItemEqual(stack)
        }

        flag = flag && if (ignoreNBT) {
            true
        } else {
            filterStack.tagCompound == stack.tagCompound
        }

        return flag
    }

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