package com.cleanroommc.retrosophisticatedbackpacks.backpack.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.backpack.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.backpack.ExposedItemStackHandler
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.capabilities.Capability

class AdvancedPickupUpgradeWrapper : PickupUpgradeWrapper() {
    companion object {
        private const val MATCH_TYPE_TAG = "MatchType"
        private const val IGNORE_DURABILITY_TAG = "IgnoreDurability"
        private const val IGNORE_NBT_TAG = "IgnoreNbt"
    }

    override val filterItems: ExposedItemStackHandler = ExposedItemStackHandler(16)
    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(Capabilities.ADVANCED_PICKUP_UPGRADE_CAPABILITY, *super.acceptableCapabilities.toTypedArray())

    var matchType = MatchType.ITEM
    var oreDictEntries = mutableListOf<String>()
    var ignoreDurability = true
    var ignoreNBT = true

    override fun canPickup(stack: ItemStack): Boolean {
        if (!enabled)
            return false

        return when (matchType) {
            MatchType.ITEM -> matchItem(stack)
            MatchType.MOD -> matchMod(stack)
            MatchType.ORE_DICT -> true
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
        val nbt = super.serializeNBT()
        nbt.setByte(MATCH_TYPE_TAG, matchType.ordinal.toByte())
        nbt.setBoolean(IGNORE_DURABILITY_TAG, ignoreDurability)
        nbt.setBoolean(IGNORE_NBT_TAG, ignoreNBT)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        matchType = MatchType.entries[nbt.getByte(MATCH_TYPE_TAG).toInt()]
        ignoreDurability = nbt.getBoolean(IGNORE_DURABILITY_TAG)
        ignoreNBT = nbt.getBoolean(IGNORE_NBT_TAG)
    }

    enum class MatchType {
        ITEM,
        MOD,
        ORE_DICT;
    }
}