package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.drawable.TabTexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.SingleChildWidget

class TabWidget(tabIndex: Int) : SingleChildWidget<TabWidget>(), Interactable {
    companion object {
        val TAB_TEXTURE: TabTexture = GuiTextures.TAB_RIGHT
    }

    var showExpanded = false
    var expandedWidget: ExpandedTabWidget<*>? = null
        set(value) {
            if (value != null)
                child(value.setEnabledIf { showExpanded })

            field = value
        }
    var tabIcon: ItemDrawable? = null

    init {
        size(TAB_TEXTURE.width, TAB_TEXTURE.height)
            .right(-TAB_TEXTURE.width + 4)
            .top((tabIndex + 1) * 30)
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (!isEnabled || expandedWidget == null)
            return Interactable.Result.STOP

        if (mouseButton == 0) {
            expandedWidget?.updateTabState()
            Interactable.playButtonClickSound()
            return Interactable.Result.SUCCESS
        }

        return Interactable.Result.STOP
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)

        if (showExpanded)
            return

        tabIcon?.draw(context, 8, 6, 16, 16, widgetTheme)
    }

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawBackground(context, widgetTheme)

        if (showExpanded)
            return

        TAB_TEXTURE.get(0, false).drawAtZero(context, TAB_TEXTURE.width, TAB_TEXTURE.height, widgetTheme)
    }
}