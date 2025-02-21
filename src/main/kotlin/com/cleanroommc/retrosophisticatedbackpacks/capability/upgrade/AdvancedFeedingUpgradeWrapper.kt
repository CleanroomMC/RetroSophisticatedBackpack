package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.FeedingUpgradeItem
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class AdvancedFeedingUpgradeWrapper : AdvancedUpgradeWrapper<FeedingUpgradeItem>(), IFeedingUpgrade {
    companion object {
        private const val HUNGER_FEEDING_STRATEGY_TAG = "HungerFeedingStrategy"
        private const val HURT_FEEDING_STRATEGY_TAG = "HurtFeedingStrategy"
    }

    override val filterItems: ExposedItemStackHandler = object : ExposedItemStackHandler(16) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.item is ItemFood
    }
    var hungerFeedingStrategy: FeedingStrategy.Hunger = FeedingStrategy.Hunger.HALF
    var healthFeedingStrategy: FeedingStrategy.HEALTH = FeedingStrategy.HEALTH.IGNORE

    override fun checkFilter(stack: ItemStack): Boolean =
        stack.item is ItemFood && super.checkFilter(stack)

    override fun getFeedingStack(inventory: IInventory, foodLevel: Int, health: Float, maxHealth: Float): ItemStack {
        val size = inventory.sizeInventory

        for (i in 0 until size) {
            val stack = inventory.getStackInSlot(i)

            if (stack.isEmpty)
                continue

            if (!checkFilter(stack))
                continue

            val item = stack.item as ItemFood
            val healingAmount = item.getHealAmount(stack)

            if (maxHealth > health && healthFeedingStrategy == FeedingStrategy.HEALTH.ALWAYS)
                return inventory.decrStackSize(i, 1)

            val flag = when (hungerFeedingStrategy) {
                FeedingStrategy.Hunger.FULL -> healingAmount <= 20 - foodLevel
                FeedingStrategy.Hunger.HALF -> healingAmount / 2 <= 20 - foodLevel
                FeedingStrategy.Hunger.ALWAYS -> foodLevel < 20
            }

            if (flag)
                return inventory.decrStackSize(i, 1)
        }

        return ItemStack.EMPTY
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_FEEDING_UPGRADE_CAPABILITY ||
                super<IFeedingUpgrade>.hasCapability(capability, facing) ||
                super<AdvancedUpgradeWrapper>.hasCapability(capability, facing)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setByte(HUNGER_FEEDING_STRATEGY_TAG, hungerFeedingStrategy.ordinal.toByte())
        nbt.setByte(HURT_FEEDING_STRATEGY_TAG, healthFeedingStrategy.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        hungerFeedingStrategy = FeedingStrategy.Hunger.entries[nbt.getByte(HUNGER_FEEDING_STRATEGY_TAG).toInt()]
        healthFeedingStrategy = FeedingStrategy.HEALTH.entries[nbt.getByte(HURT_FEEDING_STRATEGY_TAG).toInt()]
    }

    class FeedingStrategy private constructor() {
        enum class Hunger {
            FULL,
            HALF,
            ALWAYS;
        }

        enum class HEALTH {
            ALWAYS,
            IGNORE;
        }
    }
}