package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.TabTexture
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey

class SettingTabWidget : Widget<SettingTabWidget>(), Interactable {
    companion object {
        val TAB_TEXTURE: TabTexture = GuiTextures.TAB_RIGHT
    }

    init {
        size(TAB_TEXTURE.width, TAB_TEXTURE.height)
            .right(-TAB_TEXTURE.width + 4)
            .top(0)
            .background(TAB_TEXTURE.get(-1, false))
            .tooltipStatic {
                it.addLine(IKey.lang("gui.settings".asTranslationKey()))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
    }

    override fun onInit() {
        context.recipeViewerSettings.addExclusionArea(this)
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (mouseButton == 0) {
            val panel = panel as BackpackPanel

            Interactable.playButtonClickSound()
            if (panel.settingPanel.isPanelOpen) {
                panel.settingPanel.closePanel()
            } else {
                panel.settingPanel.openPanel()
            }

            return Interactable.Result.SUCCESS
        }

        return Interactable.Result.IGNORE
    }

    override fun draw(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.draw(context, widgetTheme)

        RSBTextures.SETTING_ICON.draw(context, 8, 6, 16, 16, widgetTheme?.theme ?: WidgetTheme.getDefault().theme)
    }
}