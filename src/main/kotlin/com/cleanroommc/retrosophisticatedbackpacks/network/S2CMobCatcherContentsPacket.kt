package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherStorage
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class S2CMobCatcherContentsPacket() : IRefinedMessage {
    private var capturedMobsTag = NBTTagCompound()

    constructor(backpackWrapper: BackpackWrapper) : this() {
        capturedMobsTag = MobCatcherStorage.getCapturedMobsTag(backpackWrapper)
    }

    override fun toBytes(buf: ByteBuf) {
        ByteBufUtils.writeTag(buf, capturedMobsTag)
    }

    override fun fromBytes(buf: ByteBuf) {
        capturedMobsTag = ByteBufUtils.readTag(buf) ?: NBTTagCompound()
    }

    class Handler : INoReplyMessageHandler<S2CMobCatcherContentsPacket> {
        override fun onMessage(message: S2CMobCatcherContentsPacket, ctx: MessageContext): IRefinedMessage? {
            RetroSophisticatedBackpacks.proxy.applyMobCatcherContentsSync(message.capturedMobsTag)
            return null
        }
    }
}
