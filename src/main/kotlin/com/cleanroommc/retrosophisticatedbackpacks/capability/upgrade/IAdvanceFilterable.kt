package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import net.minecraft.item.ItemStack

interface IAdvanceFilterable : IBasicFilterable {
    companion object {
        const val MATCH_TYPE_TAG = "MatchType"
        const val IGNORE_DURABILITY_TAG = "IgnoreDurability"
        const val IGNORE_NBT_TAG = "IgnoreNbt"
        const val ORE_DICT_LIST_TAG = "OreDict"
    }

    var matchType: MatchType
    var oreDictEntries: MutableList<String>
    var ignoreDurability: Boolean
    var ignoreNBT: Boolean

    enum class MatchType {
        ITEM,
        MOD,
        ORE_DICT;
    }

    object Impl : IAdvanceFilterable {
        override val filterItems: ExposedItemStackHandler
            get() = ExposedItemStackHandler(0)
        override var filterType: IBasicFilterable.FilterType
            get() = IBasicFilterable.Impl.filterType
            set(_) {}

        override fun checkFilter(itemStack: ItemStack): Boolean =
            false

        override var matchType: MatchType
            get() = MatchType.ITEM
            set(_) {}
        override var oreDictEntries: MutableList<String>
            get() = mutableListOf()
            set(_) {}
        override var ignoreNBT: Boolean
            get() = false
            set(_) {}
        override var ignoreDurability: Boolean
            get() = false
            set(_) {}
    }
}