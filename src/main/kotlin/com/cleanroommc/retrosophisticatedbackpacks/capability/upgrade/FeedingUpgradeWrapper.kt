package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.FeedingUpgradeItem
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class FeedingUpgradeWrapper : BasicUpgradeWrapper<FeedingUpgradeItem>(), IFeedingUpgrade {
    override val filterItems: ExposedItemStackHandler = object : ExposedItemStackHandler(9) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.item is ItemFood
    }

    override fun checkFilter(stack: ItemStack): Boolean =
        stack.item is ItemFood && super.checkFilter(stack)

    override fun getFeedingStack(inventory: IInventory, foodLevel: Int, health: Float, maxHealth: Float): ItemStack {
        val size = inventory.sizeInventory

        for (i in 0 until size) {
            val stack = inventory.getStackInSlot(i)

            if (stack.isEmpty)
                continue

            if (checkFilter(stack))
                return inventory.decrStackSize(i, 1)
        }

        return ItemStack.EMPTY
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.FEEDING_UPGRADE_CAPABILITY ||
                super<IFeedingUpgrade>.hasCapability(capability, facing) ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing)
}