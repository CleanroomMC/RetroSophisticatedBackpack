package com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot

import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.item.ExponentialStackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.BatteryUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.MobCatcherUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.TankUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherStorage
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class ModularUpgradeSlot(
    private val panel: BackpackPanel,
    private val wrapper: BackpackWrapper,
    index: Int,
) : ModularSlot(wrapper.upgradeItemStackHandler, index) {
    override fun canTakeStack(playerIn: EntityPlayer): Boolean {
        if (panel.isSettingMode)
            return false

        val originalUpgradeItem = stack.item
        val newUpgradeItem = playerIn.inventory.itemStack.item

        if (originalUpgradeItem is StackUpgradeItem) {


            return if (newUpgradeItem is StackUpgradeItem) wrapper.canReplaceStackUpgrade(
                originalUpgradeItem.multiplier(),
                newUpgradeItem.multiplier()
            )
            else wrapper.canRemoveStackUpgrade(originalUpgradeItem.multiplier())
        }

        if (originalUpgradeItem is ExponentialStackUpgradeItem) {
            return if (newUpgradeItem is ExponentialStackUpgradeItem) true
            else wrapper.canRemoveExponentialStackUpgrade()
        }

        if (originalUpgradeItem is InceptionUpgradeItem) {
            return if (newUpgradeItem !is InceptionUpgradeItem) wrapper.canRemoveInceptionUpgrade()
            else true
        }

        if (originalUpgradeItem is MobCatcherUpgradeItem) {
            if (wrapper.capturedMobs.isEmpty()) {
                return true
            }
            return newUpgradeItem is MobCatcherUpgradeItem &&
                    (newUpgradeItem.advanced || MobCatcherStorage.canFitBasicTier(wrapper, Config.mobCatcherUpgrade.basicMaxSlotCost))
        }

        return true
    }

    override fun getItemStackLimit(stack: ItemStack): Int =
        1

    override fun isItemValid(stack: ItemStack): Boolean {
        val item = stack.item as? UpgradeItem ?: return false
        if (!canFitConfiguredUpgradeLimit(item)) {
            return false
        }
        return when (item) {
            is StackUpgradeItem -> wrapper.canAddStackUpgrade(item.multiplier())
            is ExponentialStackUpgradeItem -> wrapper.canAddExponentialStackUpgrade()
            is TankUpgradeItem -> MobCatcherStorage.canFitWithAdditionalInventoryControls(
                wrapper,
                if (this.stack.item is TankUpgradeItem || wrapper.tankUpgradeSlots().size >= 2) 0 else 1
            )
            is BatteryUpgradeItem -> (this.stack.item is BatteryUpgradeItem || wrapper.canAddBatteryUpgrade()) &&
                    MobCatcherStorage.canFitWithAdditionalInventoryControls(
                        wrapper,
                        if (this.stack.item is BatteryUpgradeItem || wrapper.hasBatteryUpgrade()) 0 else 1
                    )
            is MobCatcherUpgradeItem -> this.stack.item is MobCatcherUpgradeItem || wrapper.canAddMobCatcherUpgrade()
            else -> true
        }
    }

    private fun canFitConfiguredUpgradeLimit(item: UpgradeItem): Boolean {
        val (limitKey, max) = Config.getUpgradeLimit(item) ?: return true
        val currentItem = stack.item as? UpgradeItem
        val currentCount = wrapper.upgradeItemStackHandler.inventory.count {
            val upgrade = it.item as? UpgradeItem ?: return@count false
            Config.matchesUpgradeLimit(upgrade, limitKey)
        }
        val replacingSameLimit = currentItem != null && Config.matchesUpgradeLimit(currentItem, limitKey)
        return currentCount - (if (replacingSameLimit) 1 else 0) < max
    }
}
