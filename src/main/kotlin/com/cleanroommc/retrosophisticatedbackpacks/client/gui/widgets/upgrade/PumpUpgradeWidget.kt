package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.PumpUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.DynamicIconButtonWidget
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.item.ItemStack

open class PumpUpgradeWidget(
    slotIndex: Int,
    wrapper: PumpUpgradeWrapper,
    stack: ItemStack
) : ExpandedUpgradeTabWidget<PumpUpgradeWrapper>(slotIndex, wrapper, 3, stack, wrapper.settingsLangKey, width = 48) {
    init {
        size(48, 50)
        child(toggleButton({ wrapper.isInput }, RSBTextures.PUMP_INPUT_ICON, RSBTextures.PUMP_OUTPUT_ICON, "gui.pump_input", "gui.pump_output") {
            wrapper.isInput = !wrapper.isInput
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_PUMP_INPUT) { it.writeBoolean(wrapper.isInput) }
        }.pos(3, 24))
    }

    protected fun toggleButton(
        state: () -> Boolean,
        enabledIcon: IDrawable,
        disabledIcon: IDrawable,
        enabledTooltip: String,
        disabledTooltip: String,
        action: () -> Unit
    ): DynamicIconButtonWidget =
        DynamicIconButtonWidget({ if (state()) enabledIcon else disabledIcon })
            .size(18)
            .onMousePressed {
                if (it != 0) false else {
                    action()
                    Interactable.playButtonClickSound()
                    true
                }
            }
            .tooltipAutoUpdate(true)
            .tooltipDynamic {
                val key = if (state()) enabledTooltip else disabledTooltip
                it.addLine(IKey.lang(key.asTranslationKey()))
                    .addLine(IKey.lang("${key}_detail".asTranslationKey()).style(IKey.GRAY))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }
}

class AdvancedPumpUpgradeWidget(slotIndex: Int, wrapper: PumpUpgradeWrapper, stack: ItemStack) :
    PumpUpgradeWidget(slotIndex, wrapper, stack) {
    init {
        size(84, 82)
        width(84)
        child(toggleButton({ wrapper.interactWithFluidHandlers }, RSBTextures.PUMP_FLUID_HANDLER_ICON, RSBTextures.PUMP_NO_FLUID_HANDLER_ICON, "gui.pump_fluid_handlers", "gui.pump_no_fluid_handlers") {
            wrapper.interactWithFluidHandlers = !wrapper.interactWithFluidHandlers
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_PUMP_FLUID_HANDLERS) {}
        }.pos(21, 24))
        child(toggleButton({ wrapper.interactWithWorld }, RSBTextures.PUMP_WORLD_ICON, RSBTextures.PUMP_NO_WORLD_ICON, "gui.pump_world", "gui.pump_no_world") {
            wrapper.interactWithWorld = !wrapper.interactWithWorld
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_PUMP_WORLD) {}
        }.pos(39, 24))
        child(toggleButton({ wrapper.interactWithHand }, RSBTextures.PUMP_HAND_ICON, RSBTextures.PUMP_NO_HAND_ICON, "gui.pump_hand", "gui.pump_no_hand") {
            wrapper.interactWithHand = !wrapper.interactWithHand
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_PUMP_HAND) {}
        }.pos(57, 24))
        for (slot in wrapper.fluidFilters.indices) {
            child(FluidFilterSlotWidget(slot, wrapper).pos(3 + slot * 18, 44))
        }
    }

    private inner class FluidFilterSlotWidget(
        private val filterSlot: Int,
        private val pumpWrapper: PumpUpgradeWrapper
    ) : Widget<FluidFilterSlotWidget>(), Interactable {
        init {
            size(18)
            tooltipDynamic {
                val fluid = pumpWrapper.fluidFilters.getOrNull(filterSlot)
                it.addLine(if (fluid == null) IKey.lang("gui.none".asTranslationKey()) else IKey.str(fluid.localizedName))
                it.addLine(IKey.lang("gui.pump_fluid_filter_detail".asTranslationKey()).style(IKey.GRAY))
                it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }.tooltipAutoUpdate(true)
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            if (mouseButton != 0 && mouseButton != 1) {
                return Interactable.Result.IGNORE
            }
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_PUMP_FLUID_FILTER) { it.writeInt(filterSlot) }
            Interactable.playButtonClickSound()
            return Interactable.Result.SUCCESS
        }

        override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
            RSBTextures.SLOT_BACKGROUND.draw(context, 0, 0, 18, 18, widgetTheme.getThemeOrDefault())
            val fluid = pumpWrapper.fluidFilters.getOrNull(filterSlot)
            if (fluid != null) {
                GuiDraw.drawFluidTexture(fluid, 1f, 1f, 16f, 16f, context.currentDrawingZ.toFloat())
            }
            super.draw(context, widgetTheme)
        }
    }
}
