package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.FilterUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class AdvancedFilterUpgradeWrapper : AdvancedUpgradeWrapper<FilterUpgradeItem>(), IFilterUpgrade {
    override val settingsLangKey: String = "gui.advanced_filter_upgrade".asTranslationKey()
    override var filterWay: IFilterUpgrade.FilterWayType = IFilterUpgrade.FilterWayType.IN_OUT

    override fun canInsert(stack: ItemStack): Boolean {
        if (filterWay == IFilterUpgrade.FilterWayType.OUT)
            return true

        return checkFilter(stack)
    }

    override fun canExtract(stack: ItemStack): Boolean {
        if (filterWay == IFilterUpgrade.FilterWayType.IN)
            return true

        return checkFilter(stack)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_FILTER_UPGRADE_WRAPPER_CAPABILITY ||
                super<AdvancedUpgradeWrapper>.hasCapability(capability, facing) ||
                super<IFilterUpgrade>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setByte(IFilterUpgrade.FILTER_WAY_TAG, filterWay.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        filterWay = IFilterUpgrade.FilterWayType.entries[nbt.getByte(IFilterUpgrade.FILTER_WAY_TAG).toInt()]
    }
}