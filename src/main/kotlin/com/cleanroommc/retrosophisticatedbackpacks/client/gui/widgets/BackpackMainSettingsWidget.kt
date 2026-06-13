package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.sync.BackpackSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.I18n

class BackpackMainSettingsWidget(
    private val panel: BackpackPanel,
    private val parentTabWidget: TabWidget
) : ExpandedTabWidget(
    3,
    RSBTextures.BACKPACK_SETTINGS_ICON,
    "gui.backpack_settings".asTranslationKey(),
    width = if (Config.allowOpeningOtherPlayerBackpacks) 81 else 75,
    tabHeight = 70,
    expandDirection = ExpandDirection.RIGHT
) {
    private val contextButton = ContextButtonWidget(panel.backpackWrapper)
        .pos(4, 24)
        .onMousePressed {
            if (it == 0) {
                panel.backpackWrapper.toggleSettingsContext()
                panel.backpackSyncHandler.syncToServer(BackpackSH.UPDATE_TOGGLE_SETTINGS_CONTEXT) {}
                Interactable.playButtonClickSound()
                true
            } else false
        }
        .tooltipAutoUpdate(true)
        .tooltipDynamic {
            val key = if (panel.backpackWrapper.settingsContext == BackpackWrapper.SettingsContext.PLAYER)
                "gui.settings_button.context_player.tooltip"
            else "gui.settings_button.context_backpack.tooltip"
            it.addLine(IKey.lang(key.asTranslationKey()))
                .addLine(IKey.lang("${key}_detail".asTranslationKey()).style(IKey.GRAY))
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }

    private val shiftClickButton = toggleButton(
        4,
        46,
        { panel.backpackWrapper.shiftClickIntoOpenTab },
        RSBTextures.SHIFT_CLICK_OPEN_TAB_ON,
        RSBTextures.SHIFT_CLICK_OPEN_TAB_OFF,
        "shift_click_open_tab",
        BackpackSH.UPDATE_TOGGLE_SHIFT_CLICK_INTO_OPEN_TAB,
        panel.backpackWrapper::toggleShiftClickIntoOpenTab
    )
    private val keepTabOpenButton = toggleButton(
        22,
        46,
        { panel.backpackWrapper.keepTabOpen },
        RSBTextures.KEEP_TAB_OPEN_ON,
        RSBTextures.KEEP_TAB_OPEN_OFF,
        "keep_tab_open",
        BackpackSH.UPDATE_TOGGLE_KEEP_TAB_OPEN,
        panel.backpackWrapper::toggleKeepTabOpen
    )
    private val keepSearchPhraseButton = toggleButton(
        40,
        46,
        { panel.backpackWrapper.keepSearchPhrase },
        RSBTextures.KEEP_SEARCH_PHRASE_ON,
        RSBTextures.KEEP_SEARCH_PHRASE_OFF,
        "keep_search_phrase",
        BackpackSH.UPDATE_TOGGLE_KEEP_SEARCH_PHRASE,
        panel.backpackWrapper::toggleKeepSearchPhrase
    )
    private val otherPlayerButton = toggleButton(
        58,
        46,
        { panel.backpackWrapper.anotherPlayerCanOpen },
        RSBTextures.ANOTHER_PLAYER_CAN_OPEN_ON,
        RSBTextures.ANOTHER_PLAYER_CAN_OPEN_OFF,
        "another_player_can_open",
        BackpackSH.UPDATE_TOGGLE_ANOTHER_PLAYER_CAN_OPEN,
        panel.backpackWrapper::toggleAnotherPlayerCanOpen
    )

    init {
        child(contextButton)
            .child(shiftClickButton)
            .child(keepTabOpenButton)
            .child(keepSearchPhraseButton)
        if (Config.allowOpeningOtherPlayerBackpacks) {
            child(otherPlayerButton)
        }
    }

    override fun updateTabState() {
        panel.openBackpackSettings(parentTabWidget, !parentTabWidget.showExpanded)
    }

    private fun toggleButton(
        x: Int,
        y: Int,
        state: () -> Boolean,
        onIcon: IDrawable,
        offIcon: IDrawable,
        tooltipName: String,
        syncId: Int,
        toggle: () -> Unit
    ): DynamicIconButtonWidget =
        DynamicIconButtonWidget({ if (state()) onIcon else offIcon })
            .pos(x, y)
            .size(18)
            .onMousePressed {
                if (it == 0) {
                    toggle()
                    panel.backpackSyncHandler.syncToServer(syncId) {}
                    Interactable.playButtonClickSound()
                    true
                } else false
            }
            .tooltipAutoUpdate(true)
            .tooltipDynamic {
                val stateName = if (state()) "on" else "off"
                it.addLine(
                    IKey.lang("gui.settings_button.$tooltipName.$stateName".asTranslationKey())
                ).addLine(
                    IKey.lang("gui.settings_button.$tooltipName.$stateName.tooltip".asTranslationKey()).style(IKey.GRAY)
                ).pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }

    private class ContextButtonWidget(private val wrapper: BackpackWrapper) : ButtonWidget<ContextButtonWidget>() {
        init {
            size(62, 18)
        }

        override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
            val theme = widgetTheme.getThemeOrDefault()
            val left = if (isHovering) RSBTextures.CONTEXT_BUTTON_LEFT_HOVERED else RSBTextures.CONTEXT_BUTTON_LEFT
            val middle =
                if (isHovering) RSBTextures.CONTEXT_BUTTON_MIDDLE_HOVERED else RSBTextures.CONTEXT_BUTTON_MIDDLE
            val right = if (isHovering) RSBTextures.CONTEXT_BUTTON_RIGHT_HOVERED else RSBTextures.CONTEXT_BUTTON_RIGHT
            left.draw(context, 0, 0, 16, 18, theme)
            middle.draw(context, 16, 0, 14, 18, theme)
            middle.draw(context, 30, 0, 14, 18, theme)
            right.draw(context, 44, 0, 16, 18, theme)
        }

        override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
            super.drawOverlay(context, widgetTheme)
            val textKey = if (wrapper.settingsContext == BackpackWrapper.SettingsContext.PLAYER)
                "gui.settings_button.context_player"
            else "gui.settings_button.context_backpack"
            val text = I18n.format(textKey.asTranslationKey())
            val font = Minecraft.getMinecraft().fontRenderer
            font.drawStringWithShadow(text, (area.width - font.getStringWidth(text)) / 2f, 5f, 0xFFFFFF)
        }
    }
}
