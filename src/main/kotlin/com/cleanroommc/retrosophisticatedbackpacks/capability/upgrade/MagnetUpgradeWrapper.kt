package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeFilterUtils.matchesAllowEmpty
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.item.MagnetUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

open class MagnetUpgradeWrapper(
    filterSlots: Int = Config.magnetUpgrade.filterSlots,
    slotsInRow: Int = Config.magnetUpgrade.slotsInRow,
    override val range: Double = Config.magnetUpgrade.magnetRange.toDouble()
) :
    BasicUpgradeWrapper<MagnetUpgradeItem>(filterSlots, slotsInRow), IMagnetUpgrade, IContentsFilterable {
    companion object {
        private const val PICKUP_ITEMS_TAG = "PickupItems"
    }

    override val settingsLangKey = "gui.magnet_settings".asTranslationKey()
    override var pickupItems = true
    private var filterByStorage = false
    override var contentsFilterType: IContentsFilterable.ContentsFilterType
        get() = when {
            filterByStorage -> IContentsFilterable.ContentsFilterType.STORAGE
            filterType == IBasicFilterable.FilterType.WHITELIST -> IContentsFilterable.ContentsFilterType.ALLOW
            else -> IContentsFilterable.ContentsFilterType.BLOCK
        }
        set(value) {
            filterByStorage = value == IContentsFilterable.ContentsFilterType.STORAGE
            when (value) {
                IContentsFilterable.ContentsFilterType.ALLOW -> filterType = IBasicFilterable.FilterType.WHITELIST
                IContentsFilterable.ContentsFilterType.BLOCK -> filterType = IBasicFilterable.FilterType.BLACKLIST
                IContentsFilterable.ContentsFilterType.STORAGE -> {}
            }
        }

    init {
        filterType = IBasicFilterable.FilterType.BLACKLIST
    }

    override fun canPickup(stack: ItemStack, backpackWrapper: BackpackWrapper): Boolean =
        enabled && pickupItems && when (contentsFilterType) {
            IContentsFilterable.ContentsFilterType.STORAGE ->
                backpackWrapper.matchesStorageContents(stack) { candidate, stored ->
                    ItemStack.areItemsEqualIgnoreDurability(candidate, stored)
                }
            else -> matchesAllowEmpty(stack)
        }

    fun togglePickupItems() {
        pickupItems = !pickupItems
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(PICKUP_ITEMS_TAG, pickupItems)
        nbt.setBoolean(IContentsFilterable.FILTER_BY_STORAGE_TAG, filterByStorage)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        pickupItems = !nbt.hasKey(PICKUP_ITEMS_TAG) || nbt.getBoolean(PICKUP_ITEMS_TAG)
        if (nbt.hasKey(IContentsFilterable.FILTER_BY_STORAGE_TAG))
            filterByStorage = nbt.getBoolean(IContentsFilterable.FILTER_BY_STORAGE_TAG)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.MAGNET_UPGRADE_CAPABILITY ||
                super<IMagnetUpgrade>.hasCapability(capability, facing) ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing)
}

class AdvancedMagnetUpgradeWrapper :
    AdvancedUpgradeWrapper<MagnetUpgradeItem>(Config.advancedMagnetUpgrade.filterSlots, Config.advancedMagnetUpgrade.slotsInRow),
    IMagnetUpgrade, IContentsFilterable {
    companion object {
        private const val PICKUP_ITEMS_TAG = "PickupItems"
    }

    override val settingsLangKey = "gui.advanced_magnet_settings".asTranslationKey()
    override val range = Config.advancedMagnetUpgrade.magnetRange.toDouble()
    override var pickupItems = true
    private var filterByStorage = false
    override var contentsFilterType: IContentsFilterable.ContentsFilterType
        get() = when {
            filterByStorage -> IContentsFilterable.ContentsFilterType.STORAGE
            filterType == IBasicFilterable.FilterType.WHITELIST -> IContentsFilterable.ContentsFilterType.ALLOW
            else -> IContentsFilterable.ContentsFilterType.BLOCK
        }
        set(value) {
            val normalized = if (value == IContentsFilterable.ContentsFilterType.STORAGE &&
                matchType == IAdvancedFilterable.MatchType.ORE_DICT
            ) IContentsFilterable.ContentsFilterType.ALLOW else value
            filterByStorage = normalized == IContentsFilterable.ContentsFilterType.STORAGE
            when (normalized) {
                IContentsFilterable.ContentsFilterType.ALLOW -> filterType = IBasicFilterable.FilterType.WHITELIST
                IContentsFilterable.ContentsFilterType.BLOCK -> filterType = IBasicFilterable.FilterType.BLACKLIST
                IContentsFilterable.ContentsFilterType.STORAGE -> {}
            }
        }

    init {
        filterType = IBasicFilterable.FilterType.BLACKLIST
    }

    override fun canPickup(stack: ItemStack, backpackWrapper: BackpackWrapper): Boolean =
        enabled && pickupItems && when (contentsFilterType) {
            IContentsFilterable.ContentsFilterType.STORAGE ->
                backpackWrapper.matchesStorageContents(stack, ::matchesStorageStack)
            else -> matchesAllowEmpty(stack)
        }

    private fun matchesStorageStack(candidate: ItemStack, stored: ItemStack): Boolean =
        when (matchType) {
            IAdvancedFilterable.MatchType.ITEM -> {
                val itemMatches = if (ignoreDurability) ItemStack.areItemsEqualIgnoreDurability(candidate, stored)
                else candidate.isItemEqual(stored)
                itemMatches && (ignoreNBT || candidate.tagCompound == stored.tagCompound)
            }
            IAdvancedFilterable.MatchType.MOD ->
                candidate.item.registryName?.namespace == stored.item.registryName?.namespace &&
                        (ignoreDurability || candidate.itemDamage == stored.itemDamage) &&
                        (ignoreNBT || candidate.tagCompound == stored.tagCompound)
            IAdvancedFilterable.MatchType.ORE_DICT -> false
        }

    fun togglePickupItems() {
        pickupItems = !pickupItems
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(PICKUP_ITEMS_TAG, pickupItems)
        nbt.setBoolean(IContentsFilterable.FILTER_BY_STORAGE_TAG, filterByStorage)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        pickupItems = !nbt.hasKey(PICKUP_ITEMS_TAG) || nbt.getBoolean(PICKUP_ITEMS_TAG)
        if (nbt.hasKey(IContentsFilterable.FILTER_BY_STORAGE_TAG))
            filterByStorage = nbt.getBoolean(IContentsFilterable.FILTER_BY_STORAGE_TAG)
        if (matchType == IAdvancedFilterable.MatchType.ORE_DICT && filterByStorage) {
            contentsFilterType = IContentsFilterable.ContentsFilterType.ALLOW
        }
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_MAGNET_UPGRADE_CAPABILITY ||
                super<IMagnetUpgrade>.hasCapability(capability, facing) ||
                super<AdvancedUpgradeWrapper>.hasCapability(capability, facing)
}
