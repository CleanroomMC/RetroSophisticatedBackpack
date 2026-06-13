package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedJukeboxUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.DiscHandlerRegistry
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.JukeboxUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.RepeatMode
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.CyclicVariantButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.DynamicIconButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.NoBackgroundItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack

class JukeboxUpgradeWidget(
    slotIndex: Int,
    wrapper: JukeboxUpgradeWrapper,
    stack: ItemStack
) : BaseJukeboxUpgradeWidget<JukeboxUpgradeWrapper>(slotIndex, wrapper, stack, 1, 4, 3) {
    init {
        size(TAB_WIDTH, 70)
        addDiscSlots(slotIndex)
        val bottomSlotY = getBottomSlotY()
        child(commandButton(UpgradeSlotSH.UPDATE_JUKEBOX_STOP, "gui.jukebox_stop", RSBTextures.JUKEBOX_STOP_ICON).pos(3, bottomSlotY + BUTTON_PADDING))
        child(commandButton(UpgradeSlotSH.UPDATE_JUKEBOX_PLAY, "gui.jukebox_play", RSBTextures.JUKEBOX_PLAY_ICON).pos(21, bottomSlotY + BUTTON_PADDING))
    }
}

class AdvancedJukeboxUpgradeWidget(
    slotIndex: Int,
    wrapper: AdvancedJukeboxUpgradeWrapper,
    stack: ItemStack
) : BaseJukeboxUpgradeWidget<AdvancedJukeboxUpgradeWrapper>(
    slotIndex,
    wrapper,
    stack,
    wrapper.discInventory.slots,
    Config.advancedJukeboxUpgrade.slotsInRow.coerceIn(1, wrapper.discInventory.slots),
    5
) {
    init {
        size(TAB_WIDTH, 124)
        addDiscSlots(slotIndex)
        val bottomSlotY = getBottomSlotY()
        child(commandButton(UpgradeSlotSH.UPDATE_JUKEBOX_PREVIOUS, "gui.jukebox_previous", RSBTextures.JUKEBOX_PREVIOUS_ICON).pos(3, bottomSlotY + BUTTON_PADDING))
        child(commandButton(UpgradeSlotSH.UPDATE_JUKEBOX_STOP, "gui.jukebox_stop", RSBTextures.JUKEBOX_STOP_ICON).pos(21, bottomSlotY + BUTTON_PADDING))
        child(commandButton(UpgradeSlotSH.UPDATE_JUKEBOX_PLAY, "gui.jukebox_play", RSBTextures.JUKEBOX_PLAY_ICON).pos(39, bottomSlotY + BUTTON_PADDING))
        child(commandButton(UpgradeSlotSH.UPDATE_JUKEBOX_NEXT, "gui.jukebox_next", RSBTextures.JUKEBOX_NEXT_ICON).pos(57, bottomSlotY + BUTTON_PADDING))
        child(shuffleButton().pos(12, bottomSlotY + BUTTON_PADDING + 20))
        child(repeatButton().pos(48, bottomSlotY + BUTTON_PADDING + 20))
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)
        val activeSlot = wrapper.getDiscSlotActive()
        if (context == null || widgetTheme == null || activeSlot !in 0 until slotCount) {
            return
        }
        val remainingProgress = getPlaybackRemainingProgress()
        if (remainingProgress <= 0f) {
            return
        }
        val slotX = 4 + activeSlot % slotsInRow * SLOT_SIZE
        val slotY = TOP_Y + 1 + activeSlot / slotsInRow * SLOT_SIZE
        val progressOver = 16 - (16 * remainingProgress).toInt()
        GlStateManager.disableDepth()
        Gui.drawRect(slotX + progressOver, slotY, slotX + 16, slotY + 16, PLAYBACK_OVERLAY_COLOR)
        GlStateManager.enableDepth()
    }

    private fun getPlaybackRemainingProgress(): Float {
        val world = Minecraft.getMinecraft().world ?: return 0f
        val length = DiscHandlerRegistry.getMusicLengthInTicks(wrapper.getDisc(), world) ?: return 0f
        if (length <= 0L) {
            return 0f
        }
        return ((wrapper.getDiscFinishTime() - world.totalWorldTime).toFloat() / length).coerceIn(0f, 1f)
    }
}

abstract class BaseJukeboxUpgradeWidget<U : JukeboxUpgradeWrapper>(
    slotIndex: Int,
    wrapper: U,
    stack: ItemStack,
    protected val slotCount: Int,
    protected val slotsInRow: Int,
    coveredTabSize: Int
) : ExpandedUpgradeTabWidget<U>(slotIndex, wrapper, coveredTabSize, stack, wrapper.settingsLangKey, width = TAB_WIDTH) {
    protected fun addDiscSlots(slotIndex: Int) {
        val discs = SlotGroupWidget().name("jukebox_discs_$slotIndex").disableSortButtons()
        discs.size(slotsInRow * SLOT_SIZE, getSlotRows() * SLOT_SIZE).pos(3, TOP_Y)
        repeat(slotCount) {
            discs.child(NoBackgroundItemSlot().syncHandler("jukebox_discs_$slotIndex", it).pos(it % slotsInRow * SLOT_SIZE, it / slotsInRow * SLOT_SIZE))
        }
        child(discs)
    }

    protected fun commandButton(syncId: Int, langKey: String, icon: IDrawable): DynamicIconButtonWidget =
        DynamicIconButtonWidget({ icon })
            .size(BUTTON_SIZE)
            .onMousePressed {
                if (it != 0) {
                    false
                } else {
                    slotSyncHandler?.syncToServer(syncId) {}
                    Interactable.playButtonClickSound()
                    true
                }
            }
            .tooltipStatic {
                it.addLine(IKey.lang(langKey.asTranslationKey())).pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

    protected fun shuffleButton(): CyclicVariantButtonWidget =
        CyclicVariantButtonWidget(
            SHUFFLE_VARIANTS,
            if (wrapper.shuffleEnabled) 1 else 0,
            iconOffset = 1,
            iconSize = 16,
            buttonWidth = BUTTON_SIZE,
            buttonHeight = BUTTON_SIZE
        ) {
            wrapper.toggleShuffle()
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_JUKEBOX_SHUFFLE) {}
        }

    protected fun repeatButton(): CyclicVariantButtonWidget =
        CyclicVariantButtonWidget(
            REPEAT_VARIANTS,
            wrapper.repeatMode.ordinal,
            iconOffset = 1,
            iconSize = 16,
            buttonWidth = BUTTON_SIZE,
            buttonHeight = BUTTON_SIZE
        ) {
            wrapper.repeatMode = RepeatMode.entries[it]
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_JUKEBOX_REPEAT_MODE) { packet ->
                packet.writeEnumValue(wrapper.repeatMode)
            }
        }

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        super.drawBackground(context, widgetTheme)
        repeat(slotCount) {
            RSBTextures.SLOT_BACKGROUND.draw(
                context,
                3 + it % slotsInRow * SLOT_SIZE,
                TOP_Y + it / slotsInRow * SLOT_SIZE,
                SLOT_SIZE,
                SLOT_SIZE,
                widgetTheme.getThemeOrDefault()
            )
        }
    }

    protected fun getBottomSlotY(): Int = TOP_Y + getSlotRows() * SLOT_SIZE

    private fun getSlotRows(): Int = (slotCount + slotsInRow - 1) / slotsInRow
}

private const val TOP_Y = 24
private const val TAB_WIDTH = 80
private const val BUTTON_SIZE = 18
private const val SLOT_SIZE = 18
private const val BUTTON_PADDING = 3
private const val PLAYBACK_OVERLAY_COLOR = 0x5500CC00

private val SHUFFLE_VARIANTS = listOf(
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.jukebox_shuffle_disabled".asTranslationKey()), RSBTextures.JUKEBOX_SHUFFLE_OFF_ICON),
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.jukebox_shuffle_enabled".asTranslationKey()), RSBTextures.JUKEBOX_SHUFFLE_ON_ICON),
)

private val REPEAT_VARIANTS = listOf(
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.jukebox_repeat_all".asTranslationKey()), RSBTextures.JUKEBOX_REPEAT_ALL_ICON),
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.jukebox_repeat_one".asTranslationKey()), RSBTextures.JUKEBOX_REPEAT_ONE_ICON),
    CyclicVariantButtonWidget.Variant(IKey.lang("gui.jukebox_repeat_no".asTranslationKey()), RSBTextures.JUKEBOX_NO_REPEAT_ICON),
)
