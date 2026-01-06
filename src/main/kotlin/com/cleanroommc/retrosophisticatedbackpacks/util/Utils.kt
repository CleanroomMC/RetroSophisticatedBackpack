package com.cleanroommc.retrosophisticatedbackpacks.util

import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import java.util.function.Predicate
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.sign

object Utils {
    fun String.asTranslationKey(): String =
        "${Tags.MOD_ID}.${this}"

    fun Int.ceilDiv(other: Int): Int =
        this.floorDiv(other) + this.rem(other).sign.absoluteValue

    /**
     * Sets both a predicate for whether the widget should be enabled, and the current enabled value for this widget to what the predicate outputs.
     * @see Widget.setEnabledIf
     * @see Widget.setEnabled
     * @param predicate The predicate to use.
     */
    fun <W: Widget<W>> W.setEnabledIfAndEnabled(predicate: Predicate<W>): W {
        setEnabledIf(predicate)
        isEnabled = predicate.test(`this`)
        return `this`
    }
    /**
     * Sets both a predicate for whether the widget should be enabled, and the current enabled value for this widget to the provided value.
     * @see Widget.setEnabledIf
     * @see Widget.setEnabled
     * @param predicate The predicate to use.
     * @param enable Whether the widget should be enabled.
     */
    fun <W: Widget<W>> W.setEnabledIfAndEnabled(predicate: Predicate<W>, enable: Boolean): W {
        setEnabledIf(predicate)
        isEnabled = enable
        return `this`
    }

    fun WidgetThemeEntry<out WidgetTheme>?.getThemeOrDefault(): WidgetTheme =
        this?.theme ?: WidgetTheme.getDefault().theme
}