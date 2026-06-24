package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.JukeboxUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.ItemStackHandler

class JukeboxUpgradeWrapper : UpgradeWrapper<JukeboxUpgradeItem>(), IJukeboxUpgrade {
    override val settingsLangKey: String = "gui.jukebox_settings".asTranslationKey()
    override val records: ItemStackHandler = ItemStackHandler(12)
    override var currentPlayingIndex: Int = 0

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.JUKEBOX_UPGRADE_CAPABILITY ||
                super<IJukeboxUpgrade>.hasCapability(capability, facing) ||
                super<UpgradeWrapper>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        val recordsNBT = records.serializeNBT()
        nbt.setTag(IJukeboxUpgrade.RECORDS_TAG, recordsNBT)
        nbt.setInteger(IJukeboxUpgrade.CURRENT_PLAYING_INDEX_TAG, currentPlayingIndex)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        records.deserializeNBT(nbt.getCompoundTag(IJukeboxUpgrade.RECORDS_TAG))
        currentPlayingIndex = nbt.getInteger(IJukeboxUpgrade.CURRENT_PLAYING_INDEX_TAG)
    }
}
