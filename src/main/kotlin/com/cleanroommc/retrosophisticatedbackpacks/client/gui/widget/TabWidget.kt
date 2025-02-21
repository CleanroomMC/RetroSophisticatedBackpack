package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.TabTexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel

class TabWidget(
    val tabIndex: Int,
    private val panel: BackpackPanel,
) : ParentWidget<TabWidget>(), Interactable {
    companion object {
        val TAB_TEXTURE: TabTexture = GuiTextures.TAB_RIGHT
    }

    var showExpanded = false
    var expandedWidget: ExpandedTabWidget? = null
        set(value) {
            if (value != null) {
                value.parentTabWidget = this
                child(value.setEnabledIf { showExpanded })
            } else {
                remove(value)
            }

            field = value
        }
    var tabIcon: Widget<*>? = null
        set(value) {
            if (value != null) {
                child(value.size(16).pos(8, 6).setEnabledIf { isEnabled })
            } else {
                remove(value)
            }

            field = value
        }

    init {
        size(TAB_TEXTURE.width, TAB_TEXTURE.height).right(-TAB_TEXTURE.width + 4).top(tabIndex * 30)
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (!isEnabled || expandedWidget == null)
            return Interactable.Result.STOP

        if (mouseButton == 0) {
            onTabClick()
            Interactable.playButtonClickSound()
            return Interactable.Result.SUCCESS
        }

        return Interactable.Result.STOP
    }

    fun onTabClick() {
        showExpanded = !showExpanded

        if (showExpanded) {
            context.jeiSettings.addJeiExclusionArea(expandedWidget)
        } else {
            context.jeiSettings.removeJeiExclusionArea(expandedWidget)
        }

        panel.updateTabWidgets(tabIndex, showExpanded)
    }

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawBackground(context, widgetTheme)

        if (showExpanded)
            return

        val index = if (tabIndex == 0) -1 else 0
        val rightTab = TAB_TEXTURE.get(index, false)

        rightTab.draw(context, 0, 0, TAB_TEXTURE.width, TAB_TEXTURE.height, widgetTheme)
    }
}