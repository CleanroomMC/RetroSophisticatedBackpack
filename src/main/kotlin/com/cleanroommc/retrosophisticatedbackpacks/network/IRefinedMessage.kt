package com.cleanroommc.retrosophisticatedbackpacks.network

import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage

interface IRefinedMessage : IMessage {
    override fun toBytes(buf: ByteBuf)
    override fun fromBytes(buf: ByteBuf)
}