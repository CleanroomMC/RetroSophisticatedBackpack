package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.JukeboxUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.network.C2CJukeboxUpgradePacket
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemRecord
import net.minecraft.item.ItemStack

class JukeboxUpgradeWidget(
    slotIndex: Int,
    backpackWrapper: BackpackWrapper,
    wrapper: JukeboxUpgradeWrapper,
    playerEntity: EntityPlayer,
    tileEntity: BackpackTileEntity?
) :
    ExpandedUpgradeTabWidget<JukeboxUpgradeWrapper>(
        slotIndex,
        wrapper,
        3,
        ItemStack(Items.jukeboxUpgrade),
        wrapper.settingsLangKey
    ) {
    private val recordSlot: ItemSlot
    private val buttonRow: Row
    private val stopButton: ButtonWidget<*>
    private val playButton: ButtonWidget<*>

    init {
        val JukeboxSlotGroupsWidget = SlotGroupWidget().name("jukebox_slots_$slotIndex").disableSortButtons()
        JukeboxSlotGroupsWidget.flex().coverChildren().leftRel(0.5F).top(32)

        recordSlot =
            ItemSlot().syncHandler("jukebox_slot_$slotIndex", 0)
                .name("jukebox_slot_0")

        JukeboxSlotGroupsWidget.child(recordSlot)

        buttonRow = Row()
            .leftRel(0.5F)
            .top(28)
            .coverChildrenWidth()
            .childPadding(2) as Row

        stopButton = ButtonWidget().name("stop_button")
            .onMousePressed {
                if (tileEntity != null) {
                    NetworkHandler.INSTANCE.sendToServer(
                        C2CJukeboxUpgradePacket.Stationary.stopPlaying(tileEntity.pos)
                    )
                } else {
                    NetworkHandler.INSTANCE.sendToServer(
                        C2CJukeboxUpgradePacket.Moving.stopPlaying(backpackWrapper)
                    )
                }

                true
            }
            .overlay(RSBTextures.STOP_ICON)
            .tooltipStatic {
                it.addLine(IKey.lang("gui.stop".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
        playButton = ButtonWidget().name("play_button")
            .onMousePressed {
                val record = wrapper.records.getStackInSlot(0)
                val item = record.item

                if (record.isEmpty || item !is ItemRecord)
                    return@onMousePressed false

                if (tileEntity != null) {
                    NetworkHandler.INSTANCE.sendToServer(
                        C2CJukeboxUpgradePacket.Stationary(
                            C2CJukeboxUpgradePacket.PlayingAction.PLAY,
                            item.sound.soundName.toString(),
                            tileEntity.pos
                        )
                    )
                } else {
                    NetworkHandler.INSTANCE.sendToServer(
                        C2CJukeboxUpgradePacket.Moving(
                            C2CJukeboxUpgradePacket.PlayingAction.PLAY,
                            item.sound.soundName.toString(),
                            backpackWrapper,
                            playerEntity.entityId
                        )
                    )
                }

                true
            }
            .overlay(RSBTextures.PLAY_ICON)
            .tooltipStatic {
                it.addLine(IKey.lang("gui.play".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

        buttonRow.child(stopButton)
            .child(playButton)

        child(JukeboxSlotGroupsWidget)
            .child(buttonRow)
    }
}
