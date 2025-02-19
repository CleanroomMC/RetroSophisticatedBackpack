package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.SingleChildWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import kotlin.math.min

class TabWidget(
    val tabIndex: Int,
    private val panel: BackpackPanel,
) : SingleChildWidget<TabWidget>(), Interactable {
    companion object {
        private val TAB_TEXTURE = GuiTextures.TAB_RIGHT
    }

    var showExpanded = false
        set(value) {
            val startTabIndex = tabIndex + 1
            val endTabIndex = min(
                startTabIndex + (expandedWidget?.coveredTabSize ?: 0),
                panel.backpackWrapper.upgradeSlotsSize()
            )

            for (j in startTabIndex until endTabIndex) {
                panel.tabWidgets[j].clickable = !value
            }

            if (expandedWidget != null) {
                val expandedWidget = expandedWidget!!

                if (!value) {
                    expandedWidget.let(context.jeiSettings::removeJeiExclusionArea)

                    for (slot in expandedWidget.getIngredientSlots()) {
                        context.jeiSettings.removeJeiGhostIngredientSlot(slot)
                    }
                } else {
                    expandedWidget.let(context.jeiSettings::addJeiExclusionArea)

                    for (slot in expandedWidget.getIngredientSlots()) {
                        context.jeiSettings.addJeiGhostIngredientSlot(slot)
                    }
                }
            }

            field = value
        }
    var clickable = true
    var expandedWidget: ExpandedTabWidget? = null
        set(value) {
            value?.setEnabledIf { showExpanded }
            child(value)
            value?.tabIcon = tabIcon
            field = value
        }
    var tabIcon: IDrawable? = null

    init {
        size(TAB_TEXTURE.width, TAB_TEXTURE.height).right(-TAB_TEXTURE.width + 4).top(tabIndex * 30)
    }

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (!clickable)
            return Interactable.Result.STOP

        for (tabWidget in panel.tabWidgets) {
            if (tabWidget != this) {
                tabWidget.showExpanded = false
            }
        }

        if (mouseButton == 0)
            showExpanded = !showExpanded

        Interactable.playButtonClickSound()
        return Interactable.Result.ACCEPT
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetTheme?) {
        super.drawOverlay(context, widgetTheme)

        tabIcon?.draw(context, 8, 6, 16, 16, widgetTheme)
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