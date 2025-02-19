package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey

class CyclicVariantButtonWidget(
    private val variants: List<Variant>,
    private var index: Int = 0,
    private val mousePressedUpdater: CyclicVariantButtonWidget.(Int) -> Unit,
) : ButtonWidget<CyclicVariantButtonWidget>() {
    var inEffect: Boolean = true

    init {
        size(20, 20)
            .onMousePressed {
                index = (index + 1) % variants.size
                mousePressedUpdater(index)
                true
            }.tooltipDynamic {
                it.addLine(variants[index].name)

                if (!inEffect) {
                    it.addLine(IKey.lang("gui.not_in_effect".asTranslationKey()).style(IKey.RED))
                }

                it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
    }

    override fun drawOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawOverlay(context, widgetTheme)

        variants[index].drawable.draw(context, 2, 2, 16, 16, widgetTheme)
    }

    data class Variant(val name: IKey, val drawable: IDrawable)
}