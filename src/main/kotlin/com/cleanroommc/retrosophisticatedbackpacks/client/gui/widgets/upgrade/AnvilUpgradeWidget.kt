package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.upgrade

import com.cleanroommc.modularui.api.drawable.IDrawable
import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.modularui.widgets.SlotGroupWidget
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AnvilUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.VanillaTextFieldWidget
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.slot.NoBackgroundItemSlot
import com.cleanroommc.retrosophisticatedbackpacks.sync.UpgradeSlotSH
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.getThemeOrDefault
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class AnvilUpgradeWidget(
    slotIndex: Int,
    wrapper: AnvilUpgradeWrapper,
    stack: ItemStack
) : ExpandedUpgradeTabWidget<AnvilUpgradeWrapper>(slotIndex, wrapper, 5, stack, wrapper.settingsLangKey, width = 103) {
    init {
        size(103, 92)
        child(AnvilNameField().pos(4, 24).size(90, 14))
        val slots = SlotGroupWidget().name("anvil_$slotIndex").disableSortButtons()
        slots.size(56, 18).pos(3, 42)
        slots.child(NoBackgroundItemSlot().syncHandler("anvil_$slotIndex", 0).pos(0, 0))
        slots.child(NoBackgroundItemSlot().syncHandler("anvil_$slotIndex", 1).pos(38, 0))
        child(slots)
        child(AnvilResultWidget().pos(79, 42))
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        val player = context.mc.player
        val result = if (player == null) ItemStack.EMPTY else wrapper.updateRepairOutput(player, player.world)
        val theme = widgetTheme.theme
        val textFieldBackground =
            if (wrapper.getInventory().getStackInSlot(0).isEmpty) RSBTextures.ANVIL_NAME_BACKGROUND_DISABLED
            else RSBTextures.ANVIL_NAME_BACKGROUND
        textFieldBackground.draw(context, 3, 23, 94, 16, theme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 3, 42, 18, 18, widgetTheme.theme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 41, 42, 18, 18, widgetTheme.theme)
        RSBTextures.SLOT_BACKGROUND.draw(context, 79, 42, 18, 18, widgetTheme.theme)
        RSBTextures.ANVIL_PLUS_SIGN.draw(context, 25, 45, 13, 13, theme)
        RSBTextures.ANVIL_ARROW.draw(context, 62, 44, 14, 15, theme)
        if (!wrapper.getInventory().getStackInSlot(0).isEmpty && result.isEmpty) {
            RSBTextures.ANVIL_RED_CROSS.draw(context, 62, 44, 15, 15, theme)
        }
        drawCost(player, result)
        super.draw(context, widgetTheme)
    }

    private fun drawCost(player: EntityPlayer?, result: ItemStack) {
        val cost = wrapper.maximumCost
        if (cost <= 0) {
            return
        }
        val tooExpensive = player != null && cost >= 40 && !player.capabilities.isCreativeMode
        val text = when {
            tooExpensive -> I18n.format("container.repair.expensive")
            result.isEmpty -> return
            else -> I18n.format("container.repair.cost", cost)
        }
        val color = if (tooExpensive || player?.let { !wrapper.canTakeResult(it) } == true) 16736352 else 8453920
        val font = Minecraft.getMinecraft().fontRenderer
        val x = 3
        val y = 62
        val maxWidth = 94
        val lines = font.listFormattedStringToWidth(text, maxWidth)
        Gui.drawRect(x, y, x + maxWidth, y + lines.size * 12, 1325400064)
        var yOffset = 0
        for (line in lines) {
            font.drawStringWithShadow(line, x + 2 + (maxWidth - font.getStringWidth(line)) / 2f, y + 2 + yOffset.toFloat(), color)
            yOffset += 12
        }
    }

    private inner class AnvilNameField : VanillaTextFieldWidget<AnvilNameField>(3, 90, 12) {
        init {
            textField.setMaxStringLength(50)
            textField.setText(wrapper.itemName)
            textField.setTextColor(-1)
            textField.setDisabledTextColour(-1)
        }

        override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
            drawTextField()
        }

        override fun onUpdate() {
            super.onUpdate()
            if (!isFocused() && textField.text != wrapper.itemName) {
                textField.setText(wrapper.itemName)
            }
        }

        override fun onTextChanged(text: String) {
            wrapper.itemName = text
        }

        override fun onEditingFinished() {
            wrapper.itemName = textField.text
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ANVIL_ITEM_NAME) { it.writeString(wrapper.itemName) }
        }
    }

    private inner class AnvilResultWidget : Widget<AnvilResultWidget>(), Interactable {
        init {
            size(18)
            tooltipDynamic {
                val player = context.mc.player
                val result = if (player == null) ItemStack.EMPTY else wrapper.updateRepairOutput(player, player.world)
                if (result.isEmpty) {
                    it.addLine(IKey.lang("gui.anvil_no_result".asTranslationKey()))
                } else {
                    it.addLine(IKey.str(result.displayName))
                }
                it.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
            }.tooltipAutoUpdate(true)
        }

        override fun onMousePressed(mouseButton: Int): Interactable.Result {
            if (mouseButton != 0) {
                return Interactable.Result.IGNORE
            }
            val player = context.mc.player ?: return Interactable.Result.IGNORE
            if (wrapper.updateRepairOutput(player, player.world).isEmpty || !wrapper.canTakeResult(player)) {
                return Interactable.Result.IGNORE
            }
            slotSyncHandler?.syncToServer(UpgradeSlotSH.UPDATE_ANVIL_TAKE_RESULT) {}
            Interactable.playButtonClickSound()
            return Interactable.Result.SUCCESS
        }

        override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
            val player = context.mc.player ?: return
            val result = wrapper.updateRepairOutput(player, player.world)
            if (!result.isEmpty) {
                ItemDrawable(result).draw(context, 1, 1, 16, 16, widgetTheme.getThemeOrDefault())
            }
            super.draw(context, widgetTheme)
        }
    }
}
