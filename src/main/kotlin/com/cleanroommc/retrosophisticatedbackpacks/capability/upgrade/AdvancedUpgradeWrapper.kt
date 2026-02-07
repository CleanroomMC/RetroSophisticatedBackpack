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
import net.minecraftforge.oredict.OreDictionary

abstract class AdvancedUpgradeWrapper<T> : BasicUpgradeWrapper<T>(), IToggleable where T : UpgradeItem {
    companion object {
        const val MATCH_TYPE_TAG = "MatchType"
        const val IGNORE_DURABILITY_TAG = "IgnoreDurability"
        const val IGNORE_NBT_TAG = "IgnoreNbt"
        const val ORE_DICT_LIST_TAG = "OreDict"
    }
    
    override var enabled = true
    override val filterItems: ExposedItemStackHandler = ExposedItemStackHandler(16)
    var matchType = MatchType.ITEM
    var oreDictEntries = mutableListOf<String>()
    var ignoreDurability = true
    var ignoreNBT = true

    override fun checkFilter(stack: ItemStack): Boolean =
        enabled && when (matchType) {
            MatchType.ITEM -> matchItem(stack)
            MatchType.MOD -> matchMod(stack)
            MatchType.ORE_DICT -> matchOreDict(stack)
        }

    private fun matchItem(stack: ItemStack): Boolean {
        val filterResult = BooleanArray(16)

        for ((i, filterStack) in filterItems.inventory.withIndex()) {
            if (filterStack.item != stack.item)
                continue

            filterResult[i] = matchItemInfo(stack, filterStack)
        }

        return when (filterType) {
            FilterType.WHITELIST -> filterResult.any { it }
            FilterType.BLACKLIST -> filterResult.none { it }
        }
    }

    private fun matchMod(stack: ItemStack): Boolean {
        val filterResult = BooleanArray(16)

        for ((i, filterStack) in filterItems.inventory.withIndex()) {
            filterResult[i] = stack.item.registryName?.namespace == filterStack.item.registryName?.namespace
        }

        return when (filterType) {
            FilterType.WHITELIST -> filterResult.any { it }
            FilterType.BLACKLIST -> filterResult.none { it }
        }
    }

    private fun matchOreDict(stack: ItemStack): Boolean {
        if (stack.isEmpty)
            return false

        val stackOreDictionaries = OreDictionary.getOreIDs(stack).map { OreDictionary.getOreName(it) }

        for (oreDictEntry in oreDictEntries) {
            val regex = Regex(oreDictEntry)
            val matchResult = stackOreDictionaries.any { regex.matches(it) }

            if (filterType == FilterType.WHITELIST && matchResult)
                return true
            if (filterType == FilterType.BLACKLIST && matchResult)
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

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        super<IToggleable>.hasCapability(capability, facing) ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(IToggleable.ENABLED_TAG, enabled)
        nbt.setTag(FILTER_ITEMS_TAG, filterItems.serializeNBT())
        nbt.setByte(FILTER_TYPE_TAG, filterType.ordinal.toByte())
        nbt.setByte(MATCH_TYPE_TAG, matchType.ordinal.toByte())
        nbt.setBoolean(IGNORE_DURABILITY_TAG, ignoreDurability)
        nbt.setBoolean(IGNORE_NBT_TAG, ignoreNBT)

        val oreDictList = NBTTagList()

        for (entry in oreDictEntries)
            oreDictList.appendTag(NBTTagString(entry))

        nbt.setTag(ORE_DICT_LIST_TAG, oreDictList)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        enabled = nbt.getBoolean(IToggleable.ENABLED_TAG)
        filterItems.deserializeNBT(nbt.getCompoundTag(FILTER_ITEMS_TAG))
        filterType = FilterType.entries[nbt.getByte(FILTER_TYPE_TAG).toInt()]
        matchType = MatchType.entries[nbt.getByte(MATCH_TYPE_TAG).toInt()]
        ignoreDurability = nbt.getBoolean(IGNORE_DURABILITY_TAG)
        ignoreNBT = nbt.getBoolean(IGNORE_NBT_TAG)

        val oreDictList = nbt.getTagList(ORE_DICT_LIST_TAG, Constants.NBT.TAG_STRING)

        for (stringNBT in oreDictList)
            oreDictEntries.add((stringNBT as NBTTagString).string)
    }

    enum class MatchType {
        ITEM,
        MOD,
        ORE_DICT;
    }
    
    object Impl : AdvancedUpgradeWrapper<UpgradeItem>() {
        override val settingsLangKey: String = ""
    }
}
