package com.cleanroommc.retrosophisticatedbackpacks.network

import baubles.api.BaublesApi
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToolSwapperUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

class C2SToolSwapBlockPacket() : IRefinedMessage {
    private var pos = BlockPos.ORIGIN

    constructor(pos: BlockPos) : this() {
        this.pos = pos.toImmutable()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeLong(pos.toLong())
    }

    override fun fromBytes(buf: ByteBuf) {
        pos = BlockPos.fromLong(buf.readLong())
    }

    class Handler : INoReplyMessageHandler<C2SToolSwapBlockPacket> {
        override fun onMessage(message: C2SToolSwapBlockPacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.player
            player.serverWorld.addScheduledTask {
                val state = player.world.getBlockState(message.pos)
                var anyUpgradeCanInteract = false
                var result = false

                ToolSwapPacketHelper.runOnBackpacks(player) { wrapper ->
                    for (upgrade in wrapper.gatherCapabilityUpgrades(Capabilities.ITOOL_SWAPPER_UPGRADE_CAPABILITY).filterIsInstance<IToolSwapperUpgrade>()) {
                        if (!upgrade.canProcessBlockInteract() || result) continue
                        anyUpgradeCanInteract = true
                        result = upgrade.onBlockInteract(player, wrapper, player.world, message.pos, state)
                    }
                    result
                }

                when {
                    !anyUpgradeCanInteract -> ToolSwapPacketHelper.sendStatus(player, "gui.status.no_tool_swap_upgrade_present")
                    !result -> ToolSwapPacketHelper.sendStatus(player, "gui.status.no_tool_found_for_block")
                    else -> player.inventoryContainer.detectAndSendChanges()
                }
            }
            return null
        }
    }
}

internal object ToolSwapPacketHelper {
    fun runOnBackpacks(player: EntityPlayerMP, action: (BackpackWrapper) -> Boolean): Boolean {
        if (runOnBackpacksIn(InvWrapper(player.inventory), action)) return true
        return RetroSophisticatedBackpacks.baublesLoaded && runOnBackpacksIn(BaublesApi.getBaublesHandler(player), action)
    }

    fun sendStatus(player: EntityPlayerMP, langKey: String) {
        player.sendStatusMessage(TextComponentTranslation(langKey.asTranslationKey()), true)
    }

    private fun runOnBackpacksIn(inventory: IItemHandler, action: (BackpackWrapper) -> Boolean): Boolean {
        for (slot in 0 until inventory.slots) {
            val wrapper = inventory.getStackInSlot(slot).getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: continue
            if (action(wrapper)) return true
        }
        return false
    }
}
