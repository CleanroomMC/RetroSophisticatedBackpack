package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

sealed interface IFilterUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        const val FILTER_WAY_TAG = "FilterWay"
    }

    var filterWay: FilterWayType

    fun canInsert(stack: ItemStack): Boolean

    fun canExtract(stack: ItemStack): Boolean

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IFILTER_UPGRADE_CAPABILITY


    enum class FilterWayType {
        IN_OUT,
        IN,
        OUT;
    }
}