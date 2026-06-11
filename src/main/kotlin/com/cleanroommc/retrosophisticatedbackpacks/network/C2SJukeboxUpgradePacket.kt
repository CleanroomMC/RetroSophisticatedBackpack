package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.client.sound.BackpackSoundManager
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import io.netty.buffer.ByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

sealed class C2SJukeboxUpgradePacket() : IRefinedMessage {
    protected var action: PlayingAction = PlayingAction.PLAY
    protected var soundRegistryName: String? = null

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(action.ordinal)
        ByteBufUtils.writeUTF8String(buf, soundRegistryName)
    }

    override fun fromBytes(buf: ByteBuf) {
        action = PlayingAction.entries[buf.readInt()]
        soundRegistryName = ByteBufUtils.readUTF8String(buf)
    }

    class Moving() : C2SJukeboxUpgradePacket() {
        private var inventoryType: PlayerInventoryGuiData.InventoryType =
            PlayerInventoryGuiData.InventoryType.PLAYER_INVENTORY
        private var slotIndex: Int = 0

        constructor(
            action: PlayingAction,
            soundRegistryName: String,
            inventoryType: PlayerInventoryGuiData.InventoryType,
            slotIndex: Int
        ) : this() {
            this.action = action
            this.soundRegistryName = soundRegistryName
            this.inventoryType = inventoryType
            this.slotIndex = slotIndex
        }

        override fun toBytes(buf: ByteBuf) {
            super.toBytes(buf)
            buf.writeInt(inventoryType.ordinal)
            buf.writeInt(slotIndex)
        }

        override fun fromBytes(buf: ByteBuf) {
            super.fromBytes(buf)
            inventoryType = PlayerInventoryGuiData.InventoryType.entries[buf.readInt()]
            slotIndex = buf.readInt()
        }

        class Handler : INoReplyMessageHandler<Moving> {
            override fun onMessage(
                message: Moving,
                ctx: MessageContext
            ): IRefinedMessage? {
                val player = ctx.serverHandler.player
                val world = player.serverWorld
                val soundRegistryName = message.soundRegistryName ?: return null

                world.addScheduledTask {
                    val stack = when (message.inventoryType) {
                        PlayerInventoryGuiData.InventoryType.PLAYER_INVENTORY ->
                            player.inventory.getStackInSlot(message.slotIndex)

                        PlayerInventoryGuiData.InventoryType.PLAYER_BAUBLES ->
                            player.inventoryContainer.getSlot(message.slotIndex).stack
                    }
                    val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return@addScheduledTask

                    when (message.action) {
                        PlayingAction.PLAY ->
                            BackpackSoundManager.playRecordFromPlayer(player, wrapper, soundRegistryName)

                        PlayingAction.STOP ->
                            BackpackSoundManager.stopRecord(wrapper)
                    }
                }

                return null
            }
        }
    }

    class Stationary() : C2SJukeboxUpgradePacket() {
        private var pos: BlockPos = BlockPos.ORIGIN

        constructor(
            action: PlayingAction,
            soundRegistryName: String,
            pos: BlockPos
        ) : this() {
            this.action = action
            this.soundRegistryName = soundRegistryName
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

        class Handler : INoReplyMessageHandler<Stationary> {
            override fun onMessage(
                message: Stationary,
                ctx: MessageContext
            ): IRefinedMessage? {
                val player = ctx.serverHandler.player
                val world = player.serverWorld
                val tileEntity = world.getTileEntity(message.pos) as? BackpackTileEntity? ?: return null
                val soundRegistryName = message.soundRegistryName ?: return null

                world.addScheduledTask {
                    val wrapper =
                        tileEntity.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return@addScheduledTask

                    when (message.action) {
                        PlayingAction.PLAY ->
                            BackpackSoundManager.playRecordFromTileEntity(tileEntity, wrapper, soundRegistryName)

                        PlayingAction.STOP ->
                            BackpackSoundManager.stopRecord(wrapper)
                    }
                }
                
                return null
            }
        }
    }

    enum class PlayingAction {
        PLAY, STOP
    }
}
