package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.JukeboxUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class AdvancedJukeboxUpgradeWrapper : UpgradeWrapper<JukeboxUpgradeItem>(), IJukeboxUpgrade {
    companion object {
        private const val SHUFFLE_TAG = "Shuffle"
        private const val REPEAT_TAG = "Repeat"
    }

    override val settingsLangKey: String = "gui.jukebox_settings".asTranslationKey()
    override val records: ExposedItemStackHandler = ExposedItemStackHandler(12)
    override var currentPlayingIndex: Int = 0

    var playByShuffle: Boolean = false
    var playRepeat: RepeatType = RepeatType.NONE

    // Gets next record to play and respects shuffle and repeat settings, calling this method will advance the playing index
    fun getNextRecordForPlaying(): ItemStack {
        val records = records.inventory
        val indices = records.indices
        val predicate = { i: Int -> !records[i].isEmpty }

        val nextIndex = if (playByShuffle && playRepeat != RepeatType.ONE) {
            indices.filter(predicate).randomOrNull() ?: -1
        } else {
            when (playRepeat) {
                RepeatType.NONE -> {
                    indices.drop(currentPlayingIndex + 1).find(predicate) ?: -1
                }

                RepeatType.ALL -> {
                    indices.drop(currentPlayingIndex + 1).find(predicate)
                        ?: indices.take(currentPlayingIndex + 1).find(predicate) ?: -1
                }

                RepeatType.ONE -> {
                    if (!records[currentPlayingIndex].isEmpty) currentPlayingIndex
                    else indices.drop(currentPlayingIndex + 1).find(predicate)
                        ?: indices.take(currentPlayingIndex + 1).find(predicate) ?: -1
                }
            }
        }

        if (nextIndex == -1) {
            currentPlayingIndex = 0
            return ItemStack.EMPTY
        }

        val record = records[nextIndex].copy()
        currentPlayingIndex = nextIndex
        return record
    }

    // Gets previous record to play and respects shuffle and repeat settings, calling this method will advance the playing index
    fun getPreviousRecordForPlaying(): ItemStack {
        val records = records.inventory
        val predicate = { i: Int -> !records[i].isEmpty }

        val prevIndex = when (playRepeat) {
            RepeatType.NONE -> {
                (currentPlayingIndex - 1 downTo 0).find(predicate) ?: -1
            }

            RepeatType.ALL -> {
                (currentPlayingIndex - 1 downTo 0).find(predicate)
                    ?: (records.size - 1 downTo 0).find(predicate) ?: -1
            }

            RepeatType.ONE -> {
                if (!records[currentPlayingIndex].isEmpty) currentPlayingIndex
                else (currentPlayingIndex - 2 downTo 0).find(predicate)
                    ?: (records.size - 1 downTo currentPlayingIndex).find(predicate) ?: -1
            }
        }

        if (prevIndex == -1) {
            currentPlayingIndex = 0
            return ItemStack.EMPTY
        }

        val record = records[prevIndex].copy()
        currentPlayingIndex = prevIndex

        return record
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_JUKEBOX_UPGRADE_CAPABILITY ||
                super<UpgradeWrapper>.hasCapability(capability, facing) ||
                super<IJukeboxUpgrade>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        val recordsNBT = records.serializeNBT()
        nbt.setTag(IJukeboxUpgrade.RECORDS_TAG, recordsNBT)
        nbt.setInteger(IJukeboxUpgrade.CURRENT_PLAYING_INDEX_TAG, currentPlayingIndex)
        nbt.setBoolean(SHUFFLE_TAG, playByShuffle)
        nbt.setByte(REPEAT_TAG, playRepeat.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        records.deserializeNBT(nbt.getCompoundTag(IJukeboxUpgrade.RECORDS_TAG))
        currentPlayingIndex = nbt.getInteger(IJukeboxUpgrade.CURRENT_PLAYING_INDEX_TAG)
        playByShuffle = nbt.getBoolean(SHUFFLE_TAG)
        playRepeat = RepeatType.entries[nbt.getByte(REPEAT_TAG).toInt()]
    }

    enum class RepeatType {
        NONE,
        ALL,
        ONE
    }
}
