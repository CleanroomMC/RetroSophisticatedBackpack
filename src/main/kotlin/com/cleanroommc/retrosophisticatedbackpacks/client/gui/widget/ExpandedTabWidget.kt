package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.drawable.AdaptableUITexture
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.Tags

sealed class ExpandedTabWidget(val coveredTabSize: Int) : ParentWidget<ExpandedTabWidget>() {
    companion object {
        val TAB_TEXTURE = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls")
            .imageSize(256, 256)
            .uv(128, 0, 128, 256)
            .adaptable(4)
            .tiled()
            .build() as AdaptableUITexture
    }

    internal var tabIcon: IDrawable? = null

    open fun getIngredientSlots(): List<ItemSlot> =
        listOf()

    override fun onInit() {
        context.jeiSettings.addJeiExclusionArea(this)
    }

    override fun drawOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawOverlay(context, widgetTheme)

        tabIcon?.draw(context, 8, 6, 16, 16, widgetTheme)
    }

    override fun drawBackground(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawBackground(context, widgetTheme)

        TAB_TEXTURE.draw(context, 0, 0, flex.area.width, flex.area.height, widgetTheme)
    }
}