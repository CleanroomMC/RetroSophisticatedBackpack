package com.cleanroommc.retrosophisticatedbackpacks.client.sound

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import net.minecraft.client.audio.MovingSound
import net.minecraft.entity.Entity
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class JukeboxUpgradeSound(var hostEntity: Entity?, var hostTileEntity: BackpackTileEntity?, recordSound: SoundEvent) :
    MovingSound(recordSound, SoundCategory.RECORDS) {

    init {
        repeat = false
        volume = 4f
        pitch = 1f
    }

    override fun update() {
        val hostEntity = this.hostEntity
        val hostTileEntity = this.hostTileEntity
        
        when {
            hostEntity != null -> {
                if (hostEntity.isDead) {
                    stopPlaying()
                    return
                }
                
                xPosF = hostEntity.posX.toFloat()
                yPosF = hostEntity.posY.toFloat()
                zPosF = hostEntity.posZ.toFloat()
            }
            hostTileEntity != null -> {
                val pos = hostTileEntity.pos
                
                if (hostTileEntity.isInvalid) {
                    stopPlaying()
                    return
                }
                
                xPosF = pos.x.toFloat()
                yPosF = pos.y.toFloat()
                zPosF = pos.z.toFloat()
            }
            else -> {
                RetroSophisticatedBackpacks.LOGGER.error("JukeboxUpgradeSound has no host entity or tile entity")
                stopPlaying()
                return
            }
        }
    }

    fun stopPlaying() {
        donePlaying = true
    }
}
