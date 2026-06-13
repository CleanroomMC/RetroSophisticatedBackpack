package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToolSwapperUpgrade
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class C2SToolSwapEntityPacket() : IRefinedMessage {
    private var entityId = -1

    constructor(entityId: Int) : this() {
        this.entityId = entityId
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(entityId)
    }

    override fun fromBytes(buf: ByteBuf) {
        entityId = buf.readInt()
    }

    class Handler : INoReplyMessageHandler<C2SToolSwapEntityPacket> {
        override fun onMessage(message: C2SToolSwapEntityPacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.player
            player.serverWorld.addScheduledTask {
                val entity = player.world.getEntityByID(message.entityId) ?: return@addScheduledTask
                var anyUpgradeCanInteract = false
                var result = false

                ToolSwapPacketHelper.runOnBackpacks(player) { wrapper ->
                    for (upgrade in wrapper.gatherCapabilityUpgrades(Capabilities.ITOOL_SWAPPER_UPGRADE_CAPABILITY).filterIsInstance<IToolSwapperUpgrade>()) {
                        if (!upgrade.canProcessEntityInteract() || result) continue
                        anyUpgradeCanInteract = true
                        result = upgrade.onEntityInteract(player, wrapper, entity)
                    }
                    result
                }

                when {
                    !anyUpgradeCanInteract -> ToolSwapPacketHelper.sendStatus(player, "gui.status.no_tool_swap_upgrade_present")
                    !result -> ToolSwapPacketHelper.sendStatus(player, "gui.status.no_tool_found_for_entity")
                    else -> player.inventoryContainer.detectAndSendChanges()
                }
            }
            return null
        }
    }
}
