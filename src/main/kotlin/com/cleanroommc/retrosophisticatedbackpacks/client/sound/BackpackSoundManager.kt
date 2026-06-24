package com.cleanroommc.retrosophisticatedbackpacks.client.sound

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.ISound
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.*

@SideOnly(Side.CLIENT)
object BackpackSoundManager {
    private val activeSounds = Object2ObjectOpenHashMap<UUID, ISound>()

    fun playRecordFromPlayer(entity: Entity, wrapper: BackpackWrapper, soundRegistryName: String) {
        val soundEvent = SoundEvent.REGISTRY.getObject(ResourceLocation(soundRegistryName)) ?: return

        stopRecord(wrapper)

        val soundToPlay = JukeboxUpgradeSound(entity, null, soundEvent)
        registerAndPlay(wrapper, soundToPlay)
    }

    fun playRecordFromTileEntity(tileEntity: BackpackTileEntity, wrapper: BackpackWrapper, soundRegistryName: String) {
        val soundEvent = SoundEvent.REGISTRY.getObject(ResourceLocation(soundRegistryName)) ?: return

        stopRecord(wrapper)

        val soundToPlay = JukeboxUpgradeSound(null, tileEntity, soundEvent)
        registerAndPlay(wrapper, soundToPlay)
    }

    fun transferPlayingOwnership(entity: Entity?, tileEntity: BackpackTileEntity?, wrapper: BackpackWrapper) {
        val sound = activeSounds[wrapper.uuid] as? JukeboxUpgradeSound? ?: return

        if (entity == null && tileEntity == null ||
            entity != null && tileEntity != null
        ) {
            RetroSophisticatedBackpacks.LOGGER.error("Unable to transfer playing ownership of backpack")
            return
        }

        sound.hostEntity = entity
        sound.hostTileEntity = tileEntity
    }

    private fun registerAndPlay(wrapper: BackpackWrapper, sound: ISound) {
        val previousSound = activeSounds[wrapper.uuid]

        if (previousSound != null)
            Minecraft.getMinecraft().soundHandler.stopSound(previousSound)

        activeSounds[wrapper.uuid] = sound
        Minecraft.getMinecraft().soundHandler.playSound(sound)
    }

    fun stopRecord(wrapper: BackpackWrapper) {
        val sound = activeSounds.remove(wrapper.uuid) ?: return

        Minecraft.getMinecraft().soundHandler.stopSound(sound)
    }

    fun isPlaying(wrapper: BackpackWrapper): Boolean {
        val sound = activeSounds[wrapper.uuid] ?: return false

        return Minecraft.getMinecraft().soundHandler.isSoundPlaying(sound)
    }

    fun clear() {
        activeSounds.clear()
    }
}
