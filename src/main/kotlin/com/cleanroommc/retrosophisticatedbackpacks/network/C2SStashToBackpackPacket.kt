package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackStashHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.SPacketSetSlot
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.items.ItemHandlerHelper

class C2SStashToBackpackPacket() : IRefinedMessage {
    enum class Action {
        CARRIED_TO_SLOT_BACKPACK,
        SLOT_TO_CARRIED_BACKPACK
    }

    private var slotNumber = -1
    private var action = Action.CARRIED_TO_SLOT_BACKPACK

    constructor(slotNumber: Int, action: Action) : this() {
        this.slotNumber = slotNumber
        this.action = action
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(slotNumber)
        buf.writeByte(action.ordinal)
    }

    override fun fromBytes(buf: ByteBuf) {
        slotNumber = buf.readInt()
        action = Action.entries[buf.readUnsignedByte().toInt()]
    }

    class Handler : INoReplyMessageHandler<C2SStashToBackpackPacket> {
        override fun onMessage(message: C2SStashToBackpackPacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.player
            player.serverWorld.addScheduledTask {
                val container = player.openContainer ?: return@addScheduledTask
                if (message.slotNumber !in 0 until container.inventorySlots.size) {
                    return@addScheduledTask
                }

                when (message.action) {
                    Action.CARRIED_TO_SLOT_BACKPACK -> stashCarriedToSlotBackpack(player, container, message.slotNumber)
                    Action.SLOT_TO_CARRIED_BACKPACK -> stashSlotToCarriedBackpack(player, container, message.slotNumber)
                }
            }
            return null
        }

        private fun stashCarriedToSlotBackpack(player: EntityPlayerMP, container: net.minecraft.inventory.Container, slotNumber: Int) {
            val carried = player.inventory.itemStack
            if (carried.isEmpty) {
                return
            }

            val slot = container.getSlot(slotNumber)
            val backpackStack = slot.stack
            if (backpackStack.isEmpty || backpackStack.count != 1 || !slot.canTakeStack(player)) {
                return
            }

            val wrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return
            val remaining = BackpackStashHelper.stash(wrapper, carried, false)
            if (remaining.count == carried.count) {
                return
            }

            player.inventory.itemStack = if (remaining.isEmpty) ItemStack.EMPTY else remaining
            syncCarriedStack(player)
            slot.putStack(backpackStack)
            slot.onSlotChanged()
            container.detectAndSendChanges()
        }

        private fun stashSlotToCarriedBackpack(player: EntityPlayerMP, container: net.minecraft.inventory.Container, slotNumber: Int) {
            val backpackStack = player.inventory.itemStack
            if (backpackStack.isEmpty || backpackStack.count != 1) {
                return
            }

            val wrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return
            val slot = container.getSlot(slotNumber)
            val slotStack = slot.stack
            if (slotStack.isEmpty || !slot.canTakeStack(player)) {
                return
            }

            val remaining = BackpackStashHelper.stash(wrapper, slotStack, true)
            val moved = slotStack.count - remaining.count
            if (moved <= 0) {
                return
            }

            val taken = slot.decrStackSize(moved)
            val remainder = BackpackStashHelper.stash(wrapper, taken, false)
            if (!remainder.isEmpty) {
                ItemHandlerHelper.giveItemToPlayer(player, remainder)
            }
            slot.onTake(player, taken)
            slot.onSlotChanged()
            syncCarriedStack(player)
            container.detectAndSendChanges()
        }

        private fun syncCarriedStack(player: EntityPlayerMP) {
            player.connection.sendPacket(SPacketSetSlot(-1, -1, player.inventory.itemStack))
        }
    }
}
