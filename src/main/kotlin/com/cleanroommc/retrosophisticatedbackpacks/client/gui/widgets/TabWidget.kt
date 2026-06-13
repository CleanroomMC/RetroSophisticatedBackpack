package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.TabTexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.SingleChildWidget
import com.cleanroommc.modularui.widget.sizer.Unit
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.setEnabledIfAndEnabled
import net.minecraft.client.renderer.GlStateManager

class TabWidget(
    private val tabIndex: Int,
    var tabOrder: Int = tabIndex,
    private val expandDirection: ExpandDirection = ExpandDirection.RIGHT
) :
    SingleChildWidget<TabWidget>(), Interactable {
    companion object {
        val TAB_TEXTURE: TabTexture = GuiTextures.TAB_RIGHT
        const val TAB_TOP_OFFSET = 0
        const val TAB_VERTICAL_SPACE = 1
    }

    var showExpanded = false
        set(value) {
            // Probably a hack, but should prevent minor flickering
            expandedWidget?.isEnabled = value

            field = value
            markTooltipDirty()
        }

    var expandedWidget: ExpandedTabWidget? = null
        set(value) {
            if (value != null) {
                if (expandDirection == ExpandDirection.LEFT)
                    value.right(0)

                child(value.setEnabledIfAndEnabled { showExpanded })
            } else {
                child(null)
            }

            field = value
        }
    var tabIcon: IDrawable? = null
    var onToggle: ((Boolean) -> Unit)? = null

    init {
        size(TAB_TEXTURE.width, TAB_TEXTURE.height)
            .top({
                (TAB_TOP_OFFSET + (tabOrder - 1).coerceAtLeast(0) * (TAB_TEXTURE.height + TAB_VERTICAL_SPACE)).toDouble()
            }, Unit.Measure.PIXEL)

        when (expandDirection) {
            ExpandDirection.LEFT -> left(-TAB_TEXTURE.width - 2)
            ExpandDirection.RIGHT -> right(-TAB_TEXTURE.width + 4)
        }
    }

    override fun onInit() {
        context.recipeViewerSettings.addExclusionArea(this)
    }

    override fun dispose() {
        if (isValid)
            context.recipeViewerSettings.removeExclusionArea(this)
        super.dispose()
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (!isEnabled || expandedWidget == null)
            return Interactable.Result.STOP

        if (mouseButton == 0) {
            expandedWidget?.updateTabState()
            onToggle?.invoke(showExpanded)
            Interactable.playButtonClickSound()
            return Interactable.Result.SUCCESS
        }

        return Interactable.Result.STOP
    }

    override fun draw(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.draw(context, widgetTheme)

        if (showExpanded)
            return

        GlStateManager.color(1f, 1f, 1f, 1f)
        tabIcon?.draw(context, 8, 6, 16, 16, widgetTheme.getThemeOrDefault())
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawBackground(context, widgetTheme)

        if (showExpanded)
            return

        val index = if (tabIndex == 0) -1 else 0

        when (expandDirection) {
            ExpandDirection.LEFT -> GuiTextures.TAB_LEFT.get(index, false)
                .drawAtZero(context, TAB_TEXTURE.width, TAB_TEXTURE.height, widgetTheme.getThemeOrDefault())

            ExpandDirection.RIGHT -> GuiTextures.TAB_RIGHT.get(index, false)
                .drawAtZero(context, TAB_TEXTURE.width, TAB_TEXTURE.height, widgetTheme.getThemeOrDefault())
        }
    }
}
