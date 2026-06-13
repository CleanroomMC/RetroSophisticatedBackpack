package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.TankUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraftforge.fluids.FluidStack

class TankInventoryControlWidget(
    private val slotSyncHandler: UpgradeSlotSH,
    private val upgradeSlot: Int,
    private val backpackWrapper: BackpackWrapper,
    height: Int
) : Widget<TankInventoryControlWidget>(), Interactable {
    companion object {
        const val WIDTH = 36
        private const val TANK_LEFT = 9
        private const val TANK_WIDTH = 18
        private const val OVERLAY_LEFT = TANK_LEFT + 1
        private const val OVERLAY_WIDTH = 16
    }

    init {
        size(WIDTH, height)
        tooltipAutoUpdate(true)
        tooltipDynamic { tooltip ->
            val wrapper = currentWrapper()
            wrapper?.getFluid()?.let { tooltip.addLine(IKey.str(it.localizedName)) }
            tooltip.addLine(IKey.str("${wrapper?.getFluid()?.amount ?: 0}/${wrapper?.getTankCapacity(backpackWrapper) ?: 0} mB"))
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        val theme = widgetTheme.getThemeOrDefault()
        val height = area.height
        RSBTextures.BAR_BACKGROUND_TOP.draw(context, TANK_LEFT, 0, TANK_WIDTH, if (height < 36) height / 2 else 18, theme)

        var yOffset = 18
        repeat((height - 36).coerceAtLeast(0) / 18) {
            RSBTextures.BAR_BACKGROUND_MIDDLE.draw(context, TANK_LEFT, yOffset, TANK_WIDTH, 18, theme)
            yOffset += 18
        }

        RSBTextures.BAR_BACKGROUND_BOTTOM.draw(
            context,
            TANK_LEFT,
            if (height < 36) height / 2 else yOffset,
            TANK_WIDTH,
            if (height < 36) height / 2 else 18,
            theme
        )

        renderFluid(context, currentWrapper(), height)

        yOffset = 0
        repeat(height / 18) {
            RSBTextures.TANK_OVERLAY.draw(context, OVERLAY_LEFT, yOffset, OVERLAY_WIDTH, 18, theme)
            yOffset += 18
        }
    }

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (mouseButton != 0 && mouseButton != 1) {
            return Interactable.Result.IGNORE
        }
        slotSyncHandler.syncToServer(UpgradeSlotSH.UPDATE_TANK_CLICK) {}
        Interactable.playButtonClickSound()
        return Interactable.Result.SUCCESS
    }

    private fun currentWrapper(): TankUpgradeWrapper? =
        backpackWrapper.upgradeItemStackHandler.inventory[upgradeSlot]
            .getCapability(Capabilities.TANK_UPGRADE_CAPABILITY, null)

    private fun renderFluid(context: ModularGuiContext, wrapper: TankUpgradeWrapper?, height: Int) {
        val fluid = wrapper?.getFluid()
        if (fluid == null || fluid.amount <= 0) {
            return
        }
        val displayLevel = ((height - 2) * (fluid.amount.toFloat() / wrapper.getTankCapacity(backpackWrapper))).toInt()
            .coerceIn(1, height - 2)
        GuiDraw.drawFluidTexture(
            fluid,
            (TANK_LEFT + 1).toFloat(),
            (height - 1 - displayLevel).toFloat(),
            OVERLAY_WIDTH.toFloat(),
            displayLevel.toFloat(),
            context.currentDrawingZ.toFloat()
        )
    }
}
