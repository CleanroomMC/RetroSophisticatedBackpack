package com.cleanroommc.retrosophisticatedbackpack.inventory.widget

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.SingleChildWidget

class TabWidget(private val tabIndex: Int, private val onMousePressedListener: TabWidget.(Int) -> Unit) :
    SingleChildWidget<TabWidget>(), Interactable {
    companion object {
        private const val TAB_SIZE = 22
        private val TAB_TEXTURE = GuiTextures.TAB_RIGHT
    }

    var showExpanded = false
    var expandedWidget: ExpandedTabWidget? = null
        set(value) {
            value?.setEnabledIf { showExpanded }
            child(value)
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
        onMousePressedListener(mouseButton)
        
        if (mouseButton == 0)
            showExpanded = !showExpanded

        return Interactable.Result.ACCEPT
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.draw(context, widgetTheme)

        if (!showExpanded) {
            val index = if (tabIndex == 0) -1 else 0
            val rightTab = TAB_TEXTURE.get(index, false)

            rightTab.draw(context, 0, 0, TAB_TEXTURE.width, TAB_TEXTURE.height, widgetTheme)
        }

        tabIcon?.draw(context, 8, 6, 16, 16, widgetTheme)
    }
}