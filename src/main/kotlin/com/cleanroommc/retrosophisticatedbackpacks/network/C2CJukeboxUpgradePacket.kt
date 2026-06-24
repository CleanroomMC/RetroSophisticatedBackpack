package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.client.sound.BackpackSoundManager
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.*

sealed class C2CJukeboxUpgradePacket : IRefinedMessage {
    protected var action: PlayingAction = PlayingAction.PLAY
    protected var soundRegistryName: String = ""

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(action.ordinal)
        ByteBufUtils.writeUTF8String(buf, soundRegistryName)
    }

    override fun fromBytes(buf: ByteBuf) {
        action = PlayingAction.entries[buf.readInt()]
        soundRegistryName = ByteBufUtils.readUTF8String(buf)
    }

    class Moving() : C2CJukeboxUpgradePacket() {
        private var backpackUUID: UUID = UUID.randomUUID()
        private var entityID: Int = 0

        companion object {
            fun stopPlaying(wrapper: BackpackWrapper): Moving =
                Moving(
                    PlayingAction.STOP,
                    null,
                    wrapper,
                    0
                )
        }

        constructor(
            action: PlayingAction,
            soundRegistryName: String?,
            wrapper: BackpackWrapper,
            entityID: Int
        ) : this() {
            this.action = action
            this.soundRegistryName = soundRegistryName ?: ""
            this.backpackUUID = wrapper.uuid!!
            this.entityID = entityID
        }

        override fun toBytes(buf: ByteBuf) {
            super.toBytes(buf)
            buf.writeLong(backpackUUID.mostSignificantBits)
            buf.writeLong(backpackUUID.leastSignificantBits)
            buf.writeInt(entityID)
        }

        override fun fromBytes(buf: ByteBuf) {
            super.fromBytes(buf)
            val most = buf.readLong()
            val least = buf.readLong()
            backpackUUID = UUID(most, least)
            entityID = buf.readInt()
        }

        class ServerHandler : IRefinedMessageHandler<Moving, Moving> {
            override fun onMessage(
                message: Moving,
                ctx: MessageContext
            ): Moving? {
                NetworkHandler.INSTANCE.sendToDimension(message, ctx.serverHandler.player.world.provider.dimension)
                return null
            }
        }

        class ClientHandler : INoReplyMessageHandler<Moving> {
            override fun onMessage(
                message: Moving,
                ctx: MessageContext
            ): IRefinedMessage? {
                val mc = Minecraft.getMinecraft()

                mc.addScheduledTask {
                    val world = mc.player.world ?: return@addScheduledTask
                    val wrapper =
                        CapabilityHandler.BACKPACK_INVENTORY_CACHE[message.backpackUUID] ?: return@addScheduledTask

                    when (message.action) {
                        PlayingAction.PLAY -> {
                            val entity = world.getEntityByID(message.entityID) ?: return@addScheduledTask

                            BackpackSoundManager.playRecordFromPlayer(entity, wrapper, message.soundRegistryName)
                        }

                        PlayingAction.STOP ->
                            BackpackSoundManager.stopRecord(wrapper)

                        PlayingAction.TRANSFER -> {
                            val entity = world.getEntityByID(message.entityID) ?: return@addScheduledTask

                            BackpackSoundManager.transferPlayingOwnership(entity, null, wrapper)
                        }
                    }
                }

                return null
            }
        }
    }

    class Stationary() : C2CJukeboxUpgradePacket() {
        private var pos: BlockPos = BlockPos.ORIGIN

        companion object {
            fun stopPlaying(pos: BlockPos): Stationary =
                Stationary(
                    PlayingAction.STOP,
                    null,
                    pos,
                )
        }

        constructor(
            action: PlayingAction,
            soundRegistryName: String?,
            pos: BlockPos
        ) : this() {
            this.action = action
            this.soundRegistryName = soundRegistryName ?: ""
            this.pos = pos
        }

        override fun toBytes(buf: ByteBuf) {
            super.toBytes(buf)
            buf.writeLong(pos.toLong())
        }

        override fun fromBytes(buf: ByteBuf) {
            super.fromBytes(buf)
            pos = BlockPos.fromLong(buf.readLong())
        }

        class ServerHandler : IRefinedMessageHandler<Stationary, Stationary> {
            override fun onMessage(
                message: Stationary,
                ctx: MessageContext
            ): Stationary? {
                NetworkHandler.INSTANCE.sendToDimension(message, ctx.serverHandler.player.world.provider.dimension)
                return null
            }
        }

        class ClientHandler : INoReplyMessageHandler<Stationary> {
            override fun onMessage(
                message: Stationary,
                ctx: MessageContext
            ): IRefinedMessage? {
                val mc = Minecraft.getMinecraft()

                mc.addScheduledTask {
                    val world = mc.player.world ?: return@addScheduledTask
                    val tileEntity = world.getTileEntity(message.pos) as? BackpackTileEntity? ?: return@addScheduledTask
                    val wrapper =
                        tileEntity.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return@addScheduledTask

                    when (message.action) {
                        PlayingAction.PLAY ->
                            BackpackSoundManager.playRecordFromTileEntity(
                                tileEntity,
                                wrapper,
                                message.soundRegistryName
                            )

                        PlayingAction.STOP ->
                            BackpackSoundManager.stopRecord(wrapper)

                        PlayingAction.TRANSFER ->
                            BackpackSoundManager.transferPlayingOwnership(null, tileEntity, wrapper)
                    }
                }

                return null
            }
        }
    }

    enum class PlayingAction {
        PLAY, STOP, TRANSFER
    }
}
