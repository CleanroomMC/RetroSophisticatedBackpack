package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackDataFixer
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.FeedingUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.IItemHandler

class FeedingUpgradeWrapper : BasicUpgradeWrapper<FeedingUpgradeItem>(), IFeedingUpgrade {
    override val settingsLangKey: String = "gui.feeding_settings".asTranslationKey()

    override val filterItems: ExposedItemStackHandler = object : ExposedItemStackHandler(9) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.item is ItemFood
    }

    override fun checkFilter(stack: ItemStack): Boolean =
        stack.item is ItemFood && super.checkFilter(stack)

    override fun getFoodSlot(handler: IItemHandler, foodLevel: Int, health: Float, maxHealth: Float): Int {
        for (slot in 0 until handler.slots) {
            val stack = handler.getStackInSlot(slot)

            if (!checkFilter(stack))
                continue

            val item = stack.item as? ItemFood ?: continue
            val healingAmount = item.getHealAmount(stack)

            if (healingAmount <= 20 - foodLevel)
                return slot
        }

        return -1
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.FEEDING_UPGRADE_CAPABILITY ||
                super<IFeedingUpgrade>.hasCapability(capability, facing) ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing)

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        BackpackDataFixer.fixFeedingUpgrade(filterItems)
    }

}