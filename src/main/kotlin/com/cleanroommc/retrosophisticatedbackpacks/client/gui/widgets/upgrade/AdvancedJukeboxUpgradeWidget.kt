package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.utils.Color
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.modularui.widgets.layout.Row
import com.cleanroommc.modularui.widgets.slot.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedJukeboxUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.sound.BackpackSoundManager
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.network.C2CJukeboxUpgradePacket
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemRecord
import net.minecraft.item.ItemStack

class AdvancedJukeboxUpgradeWidget(
    slotIndex: Int,
    private val backpackWrapper: BackpackWrapper,
    wrapper: AdvancedJukeboxUpgradeWrapper,
    private val playerEntity: EntityPlayer,
    private val tileEntity: BackpackTileEntity?
) :
    ExpandedUpgradeTabWidget<AdvancedJukeboxUpgradeWrapper>(
        slotIndex,
        wrapper,
        5,
        ItemStack(Items.advancedJukeboxUpgrade),
        wrapper.settingsLangKey
    ) {
    companion object {
        private val SHUFFLE_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.no_shuffle".asTranslationKey()),
                RSBTextures.NO_SHUFFLE_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.shuffle".asTranslationKey()),
                RSBTextures.SHUFFLE_ICON
            )
        )

        private val REPEAT_VARIANTS = listOf(
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.no_repeat".asTranslationKey()),
                RSBTextures.NO_REPEAT_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.repeat".asTranslationKey()),
                RSBTextures.REPEAT_ICON
            ),
            CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.repeat_one".asTranslationKey()),
                RSBTextures.REPEAT_ONE_ICON
            ),
        )

        private val SELECTED_SLOT_COLOR = Color.withAlpha(Color.LIGHT_GREEN.main, 0x60)
    }

    private val recordSlots: Array<JukeboxItemSlot>

    private val topButtonRow: Row
    private val previousButton: ButtonWidget<*>
    private val stopButton: ButtonWidget<*>
    private val playButton: ButtonWidget<*>
    private val nextButton: ButtonWidget<*>

    private val bottomButtonRow: Row
    private val shuffleButton: CyclicVariantButtonWidget
    private val repeatButton: CyclicVariantButtonWidget

    init {
        width(90)

        val JukeboxSlotGroupsWidget = SlotGroupWidget().name("jukebox_slots_$slotIndex").disableSortButtons()
        JukeboxSlotGroupsWidget.flex().coverChildren().leftRel(0.5F).top(28)

        recordSlots = Array(12) {
            val slot = JukeboxItemSlot(it)
                .syncHandler("jukebox_slot_$slotIndex", it)
                .pos(it % 4 * 18, it / 4 * 18)
                .name("jukebox_slot_$it") as JukeboxItemSlot

            JukeboxSlotGroupsWidget.child(slot)
            slot
        }

        topButtonRow = Row()
            .leftRel(0.5F)
            .top(30)
            .coverChildrenWidth()
            .childPadding(2) as Row

        previousButton = ButtonWidget()
            .onMousePressed {
                val record = wrapper.getPreviousRecordForPlaying()
                updateWrapper()

                if (BackpackSoundManager.isPlaying(backpackWrapper)) {
                    when (val item = record.item) {
                        is ItemRecord -> {
                            playRecord(item)
                            true
                        }

                        else -> false
                    }
                } else {
                    true
                }
            }
            .overlay(RSBTextures.SKIP_BACKWARD_ICON).name("previous_button")
            .tooltipStatic {
                it.addLine(IKey.lang("gui.previous".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
        stopButton = ButtonWidget()
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
            .overlay(RSBTextures.STOP_ICON).name("stop_button")
            .tooltipStatic {
                it.addLine(IKey.lang("gui.stop".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
        playButton = ButtonWidget()
            .onMousePressed {
                var record = wrapper.records.getStackInSlot(wrapper.currentPlayingIndex)

                // Check if the current playing index points to an empty slot
                // if so, set it to the first non-empty slot
                if (record.isEmpty) {
                    for (i in 0 until wrapper.records.slots) {
                        record = wrapper.records.getStackInSlot(i)

                        if (!record.isEmpty) {
                            wrapper.currentPlayingIndex = i
                            break
                        }
                    }

                    if (record.isEmpty) {
                        // Fallback: if there are no records left, reset the index to 0
                        wrapper.currentPlayingIndex = 0
                    }

                    updateWrapper()
                }

                when (val item = record.item) {
                    is ItemRecord -> {
                        playRecord(item)
                        true
                    }

                    else -> false
                }
            }
            .overlay(RSBTextures.PLAY_ICON).name("play_button")
            .tooltipStatic {
                it.addLine(IKey.lang("gui.play".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
        nextButton = ButtonWidget()
            .onMousePressed {
                val record = wrapper.getNextRecordForPlaying()
                updateWrapper()

                if (BackpackSoundManager.isPlaying(backpackWrapper)) {
                    when (val item = record.item) {
                        is ItemRecord -> {
                            playRecord(item)
                            true
                        }

                        else -> false
                    }
                } else {
                    true
                }
            }
            .overlay(RSBTextures.SKIP_FORWARD_ICON).name("next_button")
            .tooltipStatic {
                it.addLine(IKey.lang("gui.next".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

        topButtonRow.child(previousButton)
            .child(stopButton)
            .child(playButton)
            .child(nextButton)

        bottomButtonRow = Row()
            .leftRel(0.5F)
            .top(56)
            .coverChildrenWidth()
            .childPadding(8) as Row

        shuffleButton = CyclicVariantButtonWidget(SHUFFLE_VARIANTS, if (wrapper.playByShuffle) 1 else 0) {
            wrapper.playByShuffle = it == 1
            updateWrapper()
        }.name("shuffle_button")
        repeatButton = CyclicVariantButtonWidget(REPEAT_VARIANTS, wrapper.playRepeat.ordinal) {
            wrapper.playRepeat = AdvancedJukeboxUpgradeWrapper.RepeatType.entries[it]
            updateWrapper()
        }.name("repeat_button")

        bottomButtonRow.child(shuffleButton)
            .child(repeatButton)

        child(JukeboxSlotGroupsWidget)
            .child(topButtonRow)
            .child(bottomButtonRow)
    }

    private fun playRecord(item: ItemRecord) {
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
    }

    private fun updateWrapper() {
        slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ADVANCED_JUKEBOX) {
            it.writeInt(wrapper.currentPlayingIndex)
            it.writeBoolean(wrapper.playByShuffle)
            it.writeEnumValue(wrapper.playRepeat)
        }
    }

    private inner class JukeboxItemSlot(private val slotIndex: Int) : ItemSlot() {
        override fun drawOverlay() {
            super.drawOverlay()
            if (BackpackSoundManager.isPlaying(backpackWrapper) && slotIndex == wrapper.currentPlayingIndex) {
                GuiDraw.drawRect(1f, 1f, 16f, 16f, SELECTED_SLOT_COLOR)
            }
        }
    }
}
