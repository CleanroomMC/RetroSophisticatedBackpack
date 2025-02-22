package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable

abstract class UpgradeWrapper<T> : INBTSerializable<NBTTagCompound>, ISidelessCapabilityProvider where T : UpgradeItem {
    companion object {
        private const val TAB_STATE_TAG = "TabState"
    }

    var isTabOpened = false

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.UPGRADE_CAPABILITY

    abstract val settingsLangKey: String

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        nbt.setBoolean(TAB_STATE_TAG, isTabOpened)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        isTabOpened = nbt.getBoolean(TAB_STATE_TAG)
    }

    object Impl : UpgradeWrapper<UpgradeItem>() {
        override val settingsLangKey: String = ""

        override fun serializeNBT(): NBTTagCompound = NBTTagCompound()

        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }
}
