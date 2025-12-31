package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.TabTexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.SingleChildWidget

class TabWidget(
    private val tabIndex: Int,
    top: Int = tabIndex * 30,
    private val expandDirection: ExpandDirection = ExpandDirection.RIGHT
) :
    SingleChildWidget<TabWidget>(), Interactable {
    companion object {
        val TAB_TEXTURE: TabTexture = GuiTextures.TAB_RIGHT
    }

    var showExpanded = false
    var expandedWidget: ExpandedTabWidget? = null
        set(value) {
            if (value != null) {
                if (expandDirection == ExpandDirection.LEFT)
                    value.right(0)

                child(value.setEnabledIf { showExpanded })
            }

            field = value
        }
    var tabIcon: IDrawable? = null

    init {
        size(TAB_TEXTURE.width, TAB_TEXTURE.height).top(top)

        when (expandDirection) {
            ExpandDirection.LEFT -> left(-TAB_TEXTURE.width + 4)
            ExpandDirection.RIGHT -> right(-TAB_TEXTURE.width + 4)
        }
    }

    override fun onInit() {
        context.recipeViewerSettings.addRecipeViewerExclusionArea(this)
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

    override fun draw(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.draw(context, widgetTheme)

        if (showExpanded)
            return

        tabIcon?.draw(context, 8, 6, 16, 16, widgetTheme?.theme ?: WidgetTheme.getDefault().theme)
    }

    override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawBackground(context, widgetTheme)

        if (showExpanded)
            return

        val index = if (tabIndex == 0) -1 else 0

        when (expandDirection) {
            ExpandDirection.LEFT -> GuiTextures.TAB_LEFT.get(index, false)
                .drawAtZero(context, TAB_TEXTURE.width, TAB_TEXTURE.height, widgetTheme?.theme ?: WidgetTheme.getDefault().theme)

            ExpandDirection.RIGHT -> GuiTextures.TAB_RIGHT.get(index, false)
                .drawAtZero(context, TAB_TEXTURE.width, TAB_TEXTURE.height, widgetTheme?.theme ?: WidgetTheme.getDefault().theme)
        }
    }
}