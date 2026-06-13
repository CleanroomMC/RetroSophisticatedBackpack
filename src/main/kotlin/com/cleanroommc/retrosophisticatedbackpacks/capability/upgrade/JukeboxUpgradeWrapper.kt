package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.JukeboxUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.IItemHandler
import java.util.Collections
import java.util.LinkedList

open class JukeboxUpgradeWrapper(private val slots: Int = 1) : UpgradeWrapper<JukeboxUpgradeItem>(), IJukeboxUpgrade {
    companion object {
        private const val INVENTORY_TAG = "Inventory"
        private const val IS_PLAYING_TAG = "IsPlaying"
        private const val ACTIVE_SLOT_TAG = "ActiveSlot"
        private const val SHUFFLE_TAG = "Shuffle"
        private const val REPEAT_MODE_TAG = "RepeatMode"
        private const val FINISH_TIME_TAG = "FinishTime"

        private const val DEFAULT_DISC_LENGTH = 3600L
    }

    override val settingsLangKey = "gui.jukebox_settings".asTranslationKey()
    private var isPlaying = false
    private var activeSlot = -1
    var shuffleEnabled = false
    var repeatMode = RepeatMode.NO
    private var finishTime = 0L
    private var worldPlaying: World? = null
    private var posPlaying: BlockPos? = null
    private val playlist = LinkedList<Int>()
    private val history = LinkedList<Int>()

    override val discInventory: IItemHandler = object : ExposedItemStackHandler(slots) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.isEmpty || DiscHandlerRegistry.isSupported(stack)

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            if (isPlaying && slot == activeSlot) {
                requestStop()
            }
            initPlaylist(excludeActive = true)
        }
    }

    override fun play(world: World, pos: BlockPos) {
        if (world.isRemote || isPlaying) {
            return
        }
        worldPlaying = world
        posPlaying = pos
        playNext()
    }

    override fun play(entity: Entity) {
        play(entity.world, BlockPos(entity))
    }

    override fun stop(world: World, pos: BlockPos) {
        if (world.isRemote) {
            return
        }
        DiscHandlerRegistry.stopDisc(world, pos, getDisc())
        setPlaying(false)
        playlist.clear()
        history.clear()
    }

    override fun next() {
        if (!isPlaying) {
            return
        }
        playNext()
    }

    override fun previous() {
        if (!isPlaying || history.isEmpty()) {
            return
        }
        if (activeSlot >= 0) {
            playlist.addFirst(activeSlot)
        }
        activeSlot = history.pollLast()
        playDisc()
    }

    override fun tick(world: World, pos: BlockPos?) {
        if (world.isRemote || !isPlaying) {
            return
        }
        if (pos != null && (activeSlot == -1 || worldPlaying == null || posPlaying == null)) {
            worldPlaying = world
            posPlaying = pos
        }
        if (isPlaying && activeSlot == -1) {
            playNext()
            return
        }
        if (finishTime > 0L && world.totalWorldTime >= finishTime) {
            onDiscFinished()
        }
    }

    fun toggleShuffle() {
        shuffleEnabled = !shuffleEnabled
        initPlaylist(excludeActive = true)
    }

    fun cycleRepeatMode() {
        repeatMode = repeatMode.next()
    }

    fun isPlaying(): Boolean = isPlaying

    fun requestPlay() {
        if (isPlaying) {
            return
        }
        val world = worldPlaying
        val pos = posPlaying
        if (world != null && pos != null) {
            play(world, pos)
        } else {
            setPlaying(true)
        }
    }

    fun requestStop() {
        worldPlaying?.let { world ->
            posPlaying?.let { pos -> stop(world, pos) }
        } ?: run {
            setPlaying(false)
            playlist.clear()
            history.clear()
        }
    }

    override fun onBeforeRemoved() {
        requestStop()
    }

    private fun playNext(startOverIfAtEnd: Boolean = true) {
        if (playlist.isEmpty() && startOverIfAtEnd) {
            initPlaylist(excludeActive = false)
        }
        val nextSlot = playlist.poll() ?: return
        if (activeSlot >= 0) {
            history.add(activeSlot)
            while (history.size > slots) {
                history.poll()
            }
        }
        activeSlot = nextSlot
        playDisc()
    }

    private fun onDiscFinished() {
        when (repeatMode) {
            RepeatMode.ONE -> playDisc()
            RepeatMode.ALL -> playNext()
            RepeatMode.NO -> playNext(startOverIfAtEnd = false)
        }
    }

    private fun playDisc() {
        val world = worldPlaying ?: return
        val pos = posPlaying ?: return
        val disc = getDisc()
        if (world.isRemote || disc.isEmpty || !DiscHandlerRegistry.isSupported(disc)) {
            setPlaying(false)
            return
        }
        DiscHandlerRegistry.stopDisc(world, pos, disc)
        if (!DiscHandlerRegistry.playDisc(world, pos, disc)) {
            setPlaying(false)
            return
        }
        finishTime = world.totalWorldTime + getDiscLength(disc, world)
        setPlaying(true)
    }

    fun getDisc(): ItemStack =
        if (activeSlot in 0 until slots) discInventory.getStackInSlot(activeSlot) else ItemStack.EMPTY

    fun getDiscSlotActive(): Int = activeSlot

    fun getDiscFinishTime(): Long = finishTime

    private fun setPlaying(playing: Boolean) {
        isPlaying = playing
        if (!playing) {
            activeSlot = -1
            finishTime = 0L
        }
    }

    private fun initPlaylist(excludeActive: Boolean) {
        playlist.clear()
        for (slot in 0 until slots) {
            val disc = discInventory.getStackInSlot(slot)
            if (DiscHandlerRegistry.isSupported(disc) && (!excludeActive || !isPlaying || slot != activeSlot)) {
                playlist.add(slot)
            }
        }
        if (shuffleEnabled) {
            Collections.shuffle(playlist)
        }
    }

    private fun getDiscLength(stack: ItemStack, world: World): Long =
        DiscHandlerRegistry.getMusicLengthInTicks(stack, world) ?: DEFAULT_DISC_LENGTH

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setTag(INVENTORY_TAG, (discInventory as ExposedItemStackHandler).serializeNBT())
        nbt.setBoolean(IS_PLAYING_TAG, isPlaying)
        nbt.setInteger(ACTIVE_SLOT_TAG, activeSlot)
        nbt.setBoolean(SHUFFLE_TAG, shuffleEnabled)
        nbt.setString(REPEAT_MODE_TAG, repeatMode.name)
        nbt.setLong(FINISH_TIME_TAG, finishTime)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        if (nbt.hasKey(INVENTORY_TAG))
            (discInventory as ExposedItemStackHandler).deserializeNBT(nbt.getCompoundTag(INVENTORY_TAG))
        if (nbt.hasKey(IS_PLAYING_TAG))
            isPlaying = nbt.getBoolean(IS_PLAYING_TAG)
        if (nbt.hasKey(ACTIVE_SLOT_TAG))
            activeSlot = nbt.getInteger(ACTIVE_SLOT_TAG)
        if (nbt.hasKey(SHUFFLE_TAG))
            shuffleEnabled = nbt.getBoolean(SHUFFLE_TAG)
        if (nbt.hasKey(REPEAT_MODE_TAG))
            repeatMode = runCatching { RepeatMode.valueOf(nbt.getString(REPEAT_MODE_TAG)) }.getOrDefault(repeatMode)
        if (nbt.hasKey(FINISH_TIME_TAG))
            finishTime = nbt.getLong(FINISH_TIME_TAG)
        initPlaylist(excludeActive = true)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.JUKEBOX_UPGRADE_CAPABILITY ||
                capability == Capabilities.IJUKEBOX_UPGRADE_CAPABILITY ||
                super<UpgradeWrapper>.hasCapability(capability, facing)
}

class AdvancedJukeboxUpgradeWrapper : JukeboxUpgradeWrapper(Config.advancedJukeboxUpgrade.numberOfSlots) {
    override val settingsLangKey = "gui.advanced_jukebox_settings".asTranslationKey()

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_JUKEBOX_UPGRADE_CAPABILITY ||
                super.hasCapability(capability, facing)
}

enum class RepeatMode {
    ALL,
    ONE,
    NO;

    fun next(): RepeatMode = entries[(ordinal + 1) % entries.size]
}
