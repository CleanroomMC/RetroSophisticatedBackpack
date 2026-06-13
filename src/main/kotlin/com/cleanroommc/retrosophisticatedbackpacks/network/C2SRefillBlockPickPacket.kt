package com.cleanroommc.retrosophisticatedbackpacks.network

import baubles.api.BaublesApi
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedRefillUpgradeWrapper
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

class C2SRefillBlockPickPacket() : IRefinedMessage {
    private var pickedStack = ItemStack.EMPTY

    constructor(pickedStack: ItemStack) : this() {
        this.pickedStack = pickedStack.copy()
    }

    override fun toBytes(buf: ByteBuf) {
        ByteBufUtils.writeItemStack(buf, pickedStack)
    }

    override fun fromBytes(buf: ByteBuf) {
        pickedStack = ByteBufUtils.readItemStack(buf)
    }

    class Handler : INoReplyMessageHandler<C2SRefillBlockPickPacket> {
        override fun onMessage(message: C2SRefillBlockPickPacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.player
            val world = player.serverWorld

            world.addScheduledTask {
                if (!message.pickedStack.isEmpty) {
                    if (!attemptPick(player, InvWrapper(player.inventory), message.pickedStack) && RetroSophisticatedBackpacks.baublesLoaded) {
                        attemptPick(player, BaublesApi.getBaublesHandler(player), message.pickedStack)
                    }
                }
            }

            return null
        }

        private fun attemptPick(player: EntityPlayer, targetInventory: IItemHandler, pickedStack: ItemStack): Boolean {
            for (slot in 0 until targetInventory.slots) {
                val wrapper = targetInventory.getStackInSlot(slot)
                    .getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: continue
                if (tryRefill(player, wrapper, pickedStack)) {
                    return true
                }
            }
            return false
        }

        private fun tryRefill(player: EntityPlayer, wrapper: BackpackWrapper, pickedStack: ItemStack): Boolean =
            wrapper.gatherCapabilityUpgrades(Capabilities.ADVANCED_REFILL_UPGRADE_CAPABILITY)
                .filterIsInstance<AdvancedRefillUpgradeWrapper>()
                .any { it.pickBlock(player, wrapper, pickedStack) }
    }
}
