package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.backpack.DisplaySide
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSH
import com.cleanroommc.retrosophisticatedbackpacks.util.DyeColorUtils
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.gui.Gui
import net.minecraft.item.EnumDyeColor

class ItemDisplaySettingsWidget(
    private val panel: BackpackPanel,
    private val parentTabWidget: TabWidget
) : ExpandedTabWidget(
    2,
    RSBTextures.ITEM_DISPLAY_SETTINGS_ICON,
    "gui.item_display_settings".asTranslationKey(),
    width = 75,
    tabHeight = 48,
    expandDirection = ExpandDirection.RIGHT
) {
    private val rotateButton = DynamicIconButtonWidget({ RSBTextures.ITEM_DISPLAY_ROTATE_ICON })
        .pos(3, 24)
        .size(18)
        .onMousePressed {
            val slot = panel.currentItemDisplaySelectedSlot
            if (slot < 0) {
                return@onMousePressed false
            }
            val clockwise = it != 1
            panel.backpackWrapper.rotateItemDisplaySlot(slot, clockwise)
            panel.backpackSyncHandler.syncToServer(BackpackSH.UPDATE_ITEM_DISPLAY_ROTATION) { buf ->
                buf.writeInt(slot)
                buf.writeBoolean(clockwise)
            }
            Interactable.playButtonClickSound()
            true
        }
        .tooltipStatic {
            it.addLine(IKey.lang("gui.settings_button.rotate".asTranslationKey()))
                .addLine(IKey.lang("gui.settings_button.rotate_detail".asTranslationKey()).style(IKey.GRAY))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    private val colorButton = ColorToggleButton()
        .pos(21, 24)
        .onMousePressed {
            if (it != 0 && it != 1) {
                return@onMousePressed false
            }
            val colors = EnumDyeColor.entries
            val next = colors[(panel.backpackWrapper.itemDisplayColor.ordinal + if (it == 0) 1 else colors.size - 1) % colors.size]
            panel.backpackWrapper.itemDisplayColor = next
            panel.backpackSyncHandler.syncToServer(BackpackSH.UPDATE_ITEM_DISPLAY_COLOR) { buf ->
                buf.writeEnumValue(next)
            }
            Interactable.playButtonClickSound()
            true
        }
        .tooltipAutoUpdate(true)
        .tooltipDynamic {
            it.addLine(IKey.lang("gui.settings_button.item_display_color".asTranslationKey()))
                .addLine(IKey.lang("gui.settings_button.item_display_color_detail".asTranslationKey()).style(IKey.GRAY))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    private val sideButton = DynamicIconButtonWidget({
        when (panel.backpackWrapper.itemDisplaySide) {
            DisplaySide.FRONT -> RSBTextures.DISPLAY_SIDE_FRONT_ICON
            DisplaySide.LEFT -> RSBTextures.DISPLAY_SIDE_LEFT_ICON
            DisplaySide.RIGHT -> RSBTextures.DISPLAY_SIDE_RIGHT_ICON
        }
    })
        .pos(39, 24)
        .size(18)
        .onMousePressed {
            if (it != 0 && it != 1) {
                return@onMousePressed false
            }
            val next = if (it == 0) panel.backpackWrapper.itemDisplaySide.next()
            else panel.backpackWrapper.itemDisplaySide.previous()
            panel.backpackWrapper.itemDisplaySide = next
            panel.backpackSyncHandler.syncToServer(BackpackSH.UPDATE_ITEM_DISPLAY_SIDE) { buf ->
                buf.writeEnumValue(next)
            }
            Interactable.playButtonClickSound()
            true
        }
        .tooltipAutoUpdate(true)
        .tooltipDynamic {
            it.addLine(
                IKey.lang("gui.settings_button.display_side_${panel.backpackWrapper.itemDisplaySide.serializedName}".asTranslationKey())
            ).addLine(IKey.lang("gui.settings_button.display_side_detail".asTranslationKey()).style(IKey.GRAY))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    init {
        child(rotateButton)
            .child(colorButton)
            .child(sideButton)
    }

    override fun updateTabState() {
        panel.openItemDisplaySettings(parentTabWidget, !parentTabWidget.showExpanded)
    }

    private inner class ColorToggleButton : ButtonWidget<ColorToggleButton>() {
        init {
            size(18, 18)
        }

        override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
            super.drawOverlay(context, widgetTheme)
            val color = DyeColorUtils.colorValue(this@ItemDisplaySettingsWidget.panel.backpackWrapper.itemDisplayColor)
            Gui.drawRect(4, 4, 14, 14, color or -0x1000000)
        }
    }
}
