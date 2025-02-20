package com.cleanroommc.retrosophisticatedbackpacks.network

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

typealias INoReplyMessageHandler<REQ> = IRefinedMessageHandler<REQ, IRefinedMessage>

interface IRefinedMessageHandler<REQ, REPLY> : IMessageHandler<REQ, REPLY>
        where REQ : IRefinedMessage, REPLY : IRefinedMessage {
    override fun onMessage(message: REQ, ctx: MessageContext): REPLY?

}