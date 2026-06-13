package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeFilterUtils.matchesAllowEmpty
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.item.CompactingUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability

open class CompactingUpgradeWrapper :
    BasicUpgradeWrapper<CompactingUpgradeItem>(Config.compactingUpgrade.filterSlots, Config.compactingUpgrade.slotsInRow),
    ICompactingUpgrade {
    companion object {
        private const val COMPACT_NON_UNCRAFTABLE_TAG = "CompactNonUncraftable"
        private const val SHOULD_WORK_IN_GUI_TAG = "ShouldWorkInGui"
    }

    override val settingsLangKey = "gui.compacting_settings".asTranslationKey()
    var compactNonUncraftable = false
    var shouldWorkInGui = false

    override fun compact(wrapper: BackpackWrapper, world: World) {
        if (enabled) {
            wrapper.compactChangedSlots(world, this, false, compactNonUncraftable) { matchesAllowEmpty(it) }
        }
    }

    fun toggleCompactNonUncraftable() {
        compactNonUncraftable = !compactNonUncraftable
    }

    fun toggleWorkInGui() {
        shouldWorkInGui = !shouldWorkInGui
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(COMPACT_NON_UNCRAFTABLE_TAG, compactNonUncraftable)
        nbt.setBoolean(SHOULD_WORK_IN_GUI_TAG, shouldWorkInGui)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (nbt.hasKey(COMPACT_NON_UNCRAFTABLE_TAG))
            compactNonUncraftable = nbt.getBoolean(COMPACT_NON_UNCRAFTABLE_TAG)
        if (nbt.hasKey(SHOULD_WORK_IN_GUI_TAG))
            shouldWorkInGui = nbt.getBoolean(SHOULD_WORK_IN_GUI_TAG)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.COMPACTING_UPGRADE_CAPABILITY ||
                super<ICompactingUpgrade>.hasCapability(capability, facing) ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing)
}

class AdvancedCompactingUpgradeWrapper :
    AdvancedUpgradeWrapper<CompactingUpgradeItem>(Config.advancedCompactingUpgrade.filterSlots, Config.advancedCompactingUpgrade.slotsInRow),
    ICompactingUpgrade {
    companion object {
        private const val COMPACT_NON_UNCRAFTABLE_TAG = "CompactNonUncraftable"
        private const val SHOULD_WORK_IN_GUI_TAG = "ShouldWorkInGui"
    }

    override val settingsLangKey = "gui.advanced_compacting_settings".asTranslationKey()
    var compactNonUncraftable = false
    var shouldWorkInGui = false

    override fun compact(wrapper: BackpackWrapper, world: World) {
        if (enabled) {
            wrapper.compactChangedSlots(world, this, true, compactNonUncraftable) { matchesAllowEmpty(it) }
        }
    }

    fun toggleCompactNonUncraftable() {
        compactNonUncraftable = !compactNonUncraftable
    }

    fun toggleWorkInGui() {
        shouldWorkInGui = !shouldWorkInGui
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(COMPACT_NON_UNCRAFTABLE_TAG, compactNonUncraftable)
        nbt.setBoolean(SHOULD_WORK_IN_GUI_TAG, shouldWorkInGui)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (nbt.hasKey(COMPACT_NON_UNCRAFTABLE_TAG))
            compactNonUncraftable = nbt.getBoolean(COMPACT_NON_UNCRAFTABLE_TAG)
        if (nbt.hasKey(SHOULD_WORK_IN_GUI_TAG))
            shouldWorkInGui = nbt.getBoolean(SHOULD_WORK_IN_GUI_TAG)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_COMPACTING_UPGRADE_CAPABILITY ||
                super<ICompactingUpgrade>.hasCapability(capability, facing) ||
                super<AdvancedUpgradeWrapper>.hasCapability(capability, facing)
}
