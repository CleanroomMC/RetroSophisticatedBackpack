package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.inventory.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.inventory.PlayerInventoryGuiFactory
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class C2SOpenBackpackPacket() : IRefinedMessage {
    private var inventoryType = PlayerInventoryGuiData.InventoryType.PLAYER_INVENTORY
    private var slotIndex = 0

    constructor(inventoryType: PlayerInventoryGuiData.InventoryType, slotIndex: Int) : this() {
        this.inventoryType = inventoryType
        this.slotIndex = slotIndex
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(inventoryType.ordinal)
        buf.writeInt(slotIndex)
    }

    override fun fromBytes(buf: ByteBuf) {
        inventoryType = PlayerInventoryGuiData.InventoryType.entries[buf.readInt()]
        slotIndex = buf.readInt()
    }

    class Handler() : INoReplyMessageHandler<C2SOpenBackpackPacket> {
        override fun onMessage(
            message: C2SOpenBackpackPacket,
            ctx: MessageContext
        ): IRefinedMessage? {
            val player = ctx.serverHandler.player
            val world = player.serverWorld

            world.addScheduledTask {
                PlayerInventoryGuiFactory.open(player, message.inventoryType, message.slotIndex)
            }

            return null
        }
    }
}