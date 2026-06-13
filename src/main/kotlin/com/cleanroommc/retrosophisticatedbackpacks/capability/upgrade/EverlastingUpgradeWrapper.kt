package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.EverlastingUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class EverlastingUpgradeWrapper : UpgradeWrapper<EverlastingUpgradeItem>(), IEverlastingUpgrade {
    override val settingsLangKey = "gui.everlasting_settings".asTranslationKey()

    override fun serializeNBT(): NBTTagCompound =
        super.serializeNBT()

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.EVERLASTING_UPGRADE_CAPABILITY ||
                super<IEverlastingUpgrade>.hasCapability(capability, facing) ||
                super<UpgradeWrapper>.hasCapability(capability, facing)
}
