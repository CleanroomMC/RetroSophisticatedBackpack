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
import net.minecraft.client.renderer.GlStateManager

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

    fun selectIndex(index: Int) {
        this.index = index.coerceIn(0, variants.lastIndex)
        markTooltipDirty()
    }

    init {
        size(buttonWidth, buttonHeight)
            .onMousePressed {
                this.index =
                    if (it == 1) (this.index - 1 + variants.size) % variants.size
                    else (this.index + 1) % variants.size
                mousePressedUpdater(this.index)
                markTooltipDirty()
                true
            }.tooltipAutoUpdate(true)
            .tooltipDynamic {
                it.addLine(variants[this.index].name)
                for (detailLine in variants[this.index].detailLines) {
                    it.addLine(detailLine)
                }

                if (!inEffect) {
                    it.addLine(IKey.lang("gui.not_in_effect".asTranslationKey()).style(IKey.RED))
                }

                it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
    }

    override fun drawBackground(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        if (hasCustomTexture || buttonWidth != 20 || buttonHeight != 20) {
            val texture = when {
                buttonWidth == 12 && buttonHeight == 12 && isHovering -> RSBTextures.SMALL_BUTTON_HOVERED
                buttonWidth == 12 && buttonHeight == 12 -> RSBTextures.SMALL_BUTTON
                isHovering -> hoveredTexture
                else -> notHoveredTexture
            }
            texture.draw(context, 0, 0, buttonWidth, buttonHeight, widgetTheme.getThemeOrDefault())
        } else {
            super.drawBackground(context, widgetTheme)
        }
    }

    override fun drawOverlay(context: ModularGuiContext?, widgetTheme: WidgetThemeEntry<*>?) {
        super.drawOverlay(context, widgetTheme)

        val drawable = variants[index].drawable
        context?.let {
            GlStateManager.color(1f, 1f, 1f, 1f)
            drawable.draw(context, iconOffset, iconOffset, iconSize, iconSize, widgetTheme.getThemeOrDefault())
            GlStateManager.color(1f, 1f, 1f, 1f)
        }
    }

    data class Variant(val name: IKey, val drawable: IDrawable, val detailLines: List<IKey> = emptyList())
}
