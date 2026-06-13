package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherHandler
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import java.util.UUID

class C2SMobCatcherReleasePacket() : IRefinedMessage {
    private var capturedMobId: UUID = UUID(0L, 0L)

    constructor(capturedMobId: UUID) : this() {
        this.capturedMobId = capturedMobId
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeLong(capturedMobId.mostSignificantBits)
        buf.writeLong(capturedMobId.leastSignificantBits)
    }

    override fun fromBytes(buf: ByteBuf) {
        capturedMobId = UUID(buf.readLong(), buf.readLong())
    }

    class Handler : INoReplyMessageHandler<C2SMobCatcherReleasePacket> {
        override fun onMessage(message: C2SMobCatcherReleasePacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.player
            player.serverWorld.addScheduledTask {
                MobCatcherHandler.release(player, message.capturedMobId)
            }
            return null
        }
    }
}
