package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class ModularUpgradeSlot(
    private val panel: BackpackPanel,
    private val wrapper: BackpackWrapper,
    index: Int,
) : ModularSlot(wrapper.upgradeItemStackHandler, index) {
    override fun canTakeStack(playerIn: EntityPlayer): Boolean {
        if (panel.settingPanel.isPanelOpen)
            return false

        val originalUpgradeItem = stack.item

        if (originalUpgradeItem is StackUpgradeItem) {
            val newUpgradeItem = playerIn.inventory.itemStack.item

            return if (newUpgradeItem is StackUpgradeItem) wrapper.canReplaceStackUpgrade(
                originalUpgradeItem.multiplier(),
                newUpgradeItem.multiplier()
            )
            else wrapper.canRemoveStackUpgrade(originalUpgradeItem.multiplier())
        }

        if (originalUpgradeItem is InceptionUpgradeItem) {
            val newUpgradeItem = playerIn.inventory.itemStack.item

            return if (newUpgradeItem !is InceptionUpgradeItem) wrapper.canRemoveInceptionUpgrade()
            else true
        }

        return true
    }

    override fun getItemStackLimit(stack: ItemStack): Int =
        1

    override fun isItemValid(stack: ItemStack): Boolean = when (val item = stack.item) {
        is StackUpgradeItem -> wrapper.canAddStackUpgrade(item.multiplier())
        else -> item is UpgradeItem
    }
}