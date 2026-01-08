package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

class CraftingCyclicVariantButtonWidget(
    variants: List<Variant>,
    index: Int = 0,
    iconOffset: Int = 1,
    iconSize: Int = 16,
    mousePressedUpdater: CyclicVariantButtonWidget.(Int) -> Unit,
): CyclicVariantButtonWidget(variants, index, iconOffset, iconSize, mousePressedUpdater) {
    companion object {
        private val NOT_HOVERED = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls.png")
            .imageSize(256, 256)
            .xy(29, 0, 18, 18)
            .build()
        private val HOVERED = UITexture.builder()
            .location(Tags.MOD_ID, "gui/gui_controls.png")
            .imageSize(256, 256)
            .xy(47, 0, 18, 18)
            .build()
    }

    init {
        size(18, 18)
    }

    override fun draw(
        context: ModularGuiContext?,
        widgetTheme: WidgetThemeEntry<*>?
    ) {
        if (isHovering) {
            HOVERED.draw(context, 0, 0, 18, 18, widgetTheme.getThemeOrDefault())
        } else {
            NOT_HOVERED.draw(context, 0, 0, 18, 18, widgetTheme.getThemeOrDefault())
        }
        super.draw(context, widgetTheme)
    }
}