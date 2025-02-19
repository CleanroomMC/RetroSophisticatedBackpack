package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widget

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widgets.ButtonWidget

class CyclicVariantButtonWidget(
    private val variants: List<Variant>,
    private val mousePressedUpdater: CyclicVariantButtonWidget.(Int) -> Unit,
    private var index: Int = 0,
) : ButtonWidget<CyclicVariantButtonWidget>() {
    init {
        onMousePressed {
            index = (index + 1) % variants.size
            mousePressedUpdater(index)
            true
        }.tooltip {
            it.add(variants[index].name)
                .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }
    }

    override fun drawOverlay(context: ModularGuiContext, widgetTheme: WidgetTheme) {
        super.drawOverlay(context, widgetTheme)

        variants[index].drawable.draw(context, 2, 2, 16, 16, widgetTheme)
    }

    data class Variant(val name: IKey, val drawable: IDrawable)
}