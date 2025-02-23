package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.TabTexture
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures

class SettingTabWidget : Widget<SettingTabWidget>() {
    companion object {
        val TAB_TEXTURE: TabTexture = GuiTextures.TAB_RIGHT
    }

    init {
        size(TAB_TEXTURE.width, TAB_TEXTURE.height)
            .right(-TAB_TEXTURE.width + 4)
            .top(0)
            .background(TAB_TEXTURE.get(-1, false))
            .tooltipStatic {
                it.addLine(IKey.str("Settings"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)

        RSBTextures.SETTING_ICON.draw(context, 8, 6, 16, 16, widgetTheme)
    }
}