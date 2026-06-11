package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.ItemStackHandler

sealed interface IJukeboxUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        const val RECORDS_TAG = "Records"
        const val CURRENT_PLAYING_INDEX_TAG = "CurrentPlayingIndex"
    }

    val records: ItemStackHandler
    var currentPlayingIndex: Int

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.IJUKEBOX_UPGRADE_CAPABILITY
}
