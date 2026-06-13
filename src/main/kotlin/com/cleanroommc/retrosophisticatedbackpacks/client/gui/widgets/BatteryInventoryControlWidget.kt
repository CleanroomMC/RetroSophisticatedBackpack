package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.BatteryUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault

class BatteryInventoryControlWidget(
    private val upgradeSlot: Int,
    private val backpackWrapper: BackpackWrapper,
    height: Int
) : Widget<BatteryInventoryControlWidget>() {
    companion object {
        const val WIDTH = 36
        private const val BATTERY_LEFT = 9
        private const val BATTERY_WIDTH = 18
        private const val OVERLAY_LEFT = BATTERY_LEFT + 1
        private const val OVERLAY_WIDTH = 16
        private const val CHARGE_SEGMENT_HEIGHT = 6
        private const val TOP_BAR_COLOR = 0xFF1A1A
        private const val BOTTOM_BAR_COLOR = 0xFFFF40
    }

    init {
        size(WIDTH, height)
        tooltipAutoUpdate(true)
        tooltipDynamic {
            val wrapper = currentWrapper()
            it.addLine(IKey.str("${wrapper?.energyStored ?: 0}/${wrapper?.getMaxEnergyStored(backpackWrapper) ?: 0} FE"))
            it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        val theme = widgetTheme.getThemeOrDefault()
        val height = area.height
        RSBTextures.BAR_BACKGROUND_TOP.draw(context, BATTERY_LEFT, 0, BATTERY_WIDTH, if (height < 36) height / 2 else 18, theme)
        var yOffset = 18
        repeat((height - 36).coerceAtLeast(0) / 18) {
            RSBTextures.BAR_BACKGROUND_MIDDLE.draw(context, BATTERY_LEFT, yOffset, BATTERY_WIDTH, 18, theme)
            yOffset += 18
        }
        RSBTextures.BAR_BACKGROUND_BOTTOM.draw(
            context,
            BATTERY_LEFT,
            if (height < 36) height / 2 else yOffset,
            BATTERY_WIDTH,
            if (height < 36) height / 2 else 18,
            theme
        )
        yOffset = 0
        repeat(height / 18) {
            RSBTextures.BATTERY_OVERLAY.draw(context, OVERLAY_LEFT, yOffset, OVERLAY_WIDTH, 18, theme)
            yOffset += 18
        }
        renderCharge(context, currentWrapper(), height, theme)
        RSBTextures.BATTERY_CONNECTION_TOP.draw(context, OVERLAY_LEFT, 0, OVERLAY_WIDTH, 4, theme)
        RSBTextures.BATTERY_CONNECTION_BOTTOM.draw(context, OVERLAY_LEFT, height - 4, OVERLAY_WIDTH, 4, theme)
    }

    private fun currentWrapper(): BatteryUpgradeWrapper? =
        backpackWrapper.upgradeItemStackHandler.inventory[upgradeSlot]
            .getCapability(Capabilities.BATTERY_UPGRADE_CAPABILITY, null)

    private fun renderCharge(
        context: ModularGuiContext,
        wrapper: BatteryUpgradeWrapper?,
        height: Int,
        theme: com.cleanroommc.modularui.theme.WidgetTheme
    ) {
        val max = wrapper?.getMaxEnergyStored(backpackWrapper) ?: 0
        if (wrapper == null || max <= 0 || wrapper.energyStored <= 0) {
            return
        }
        val numberOfSegments = height / CHARGE_SEGMENT_HEIGHT
        val displayLevel = (numberOfSegments * (wrapper.energyStored.toFloat() / max)).toInt()
        if (displayLevel <= 0) {
            return
        }

        val topRed = TOP_BAR_COLOR shr 16 and 255
        val topGreen = TOP_BAR_COLOR shr 8 and 255
        val topBlue = TOP_BAR_COLOR and 255
        val bottomRed = BOTTOM_BAR_COLOR shr 16 and 255
        val bottomGreen = BOTTOM_BAR_COLOR shr 8 and 255
        val bottomBlue = BOTTOM_BAR_COLOR and 255

        for (segmentIndex in 0 until displayLevel) {
            val percentage = if (numberOfSegments <= 1) 0f else segmentIndex.toFloat() / (numberOfSegments - 1)
            val red = (bottomRed * (1 - percentage) + topRed * percentage).toInt()
            val green = (bottomGreen * (1 - percentage) + topGreen * percentage).toInt()
            val blue = (bottomBlue * (1 - percentage) + topBlue * percentage).toInt()
            RSBTextures.BATTERY_CHARGE.withColorOverride((255 shl 24) or (red shl 16) or (green shl 8) or blue).draw(
                context,
                OVERLAY_LEFT,
                height - (segmentIndex + 1) * CHARGE_SEGMENT_HEIGHT,
                OVERLAY_WIDTH,
                CHARGE_SEGMENT_HEIGHT,
                theme
            )
        }
    }
}
