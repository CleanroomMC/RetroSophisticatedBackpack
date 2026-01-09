package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

class CyclicVariantButtonWidget(
    private val variants: List<Variant>,
    index: Int = 0,
    private var iconOffset: Int = 2,
    private var iconSize: Int = 16,
    private val buttonWidth: Int = 20,
    private val buttonHeight: Int = 20,
    private val hasCustomTexture: Boolean = false,
    private val notHoveredTexture: IDrawable = RSBTextures.STANDARD_BUTTON,
    private val hoveredTexture: IDrawable = RSBTextures.STANDARD_BUTTON_HOVERED,
    private val mousePressedUpdater: CyclicVariantButtonWidget.(Int) -> Unit
) : ButtonWidget<CyclicVariantButtonWidget>() {
    var index = index
        private set
    var inEffect: Boolean = true

    init {
        size(buttonWidth, buttonHeight)
            .onMousePressed {
                this.index =
                    if (it == 1) (this.index - 1 + variants.size) % variants.size
                    else (this.index + 1) % variants.size
                mousePressedUpdater(this.index)
                markTooltipDirty()
                true
            }.tooltipDynamic {
                it.addLine(variants[this.index].name)

                if (!inEffect) {
                    it.addLine(IKey.lang("gui.not_in_effect".asTranslationKey()).style(IKey.RED))
                }

                it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
    }

    override fun draw(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        if (hasCustomTexture) {
            if (isHovering) {
                hoveredTexture.draw(context, 0, 0, buttonWidth, buttonHeight, widgetTheme.getThemeOrDefault())
            } else {
                notHoveredTexture.draw(context, 0, 0, buttonWidth, buttonHeight, widgetTheme.getThemeOrDefault())
            }
        }
        super.draw(context, widgetTheme)
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)

        val drawable = variants[index].drawable
        context?.let {
            drawable.draw(context, iconOffset, iconOffset, iconSize, iconSize, widgetTheme.getThemeOrDefault())
        }
    }

    data class Variant(val name: IKey, val drawable: IDrawable)
}