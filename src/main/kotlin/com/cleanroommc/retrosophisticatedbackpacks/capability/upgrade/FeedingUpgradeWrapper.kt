package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackDataFixer
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.FeedingUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.BackpackItemStackHelper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.IItemHandler

class FeedingUpgradeWrapper :
    BasicUpgradeWrapper<FeedingUpgradeItem>(Config.feedingUpgrade.filterSlots, Config.feedingUpgrade.slotsInRow),
    IFeedingUpgrade {
    override val settingsLangKey: String = "gui.feeding_settings".asTranslationKey()

    override val filterItems: ExposedItemStackHandler = object : ExposedItemStackHandler(Config.feedingUpgrade.filterSlots) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            IFeedingUpgrade.isValidFood(stack)
    }

    override fun checkFilter(stack: ItemStack): Boolean =
        IFeedingUpgrade.isValidFood(stack) && super.checkFilter(stack)

    override fun getFoodSlot(handler: IItemHandler, foodLevel: Int, health: Float, maxHealth: Float): Int {
        for (slot in 0 until handler.slots) {
            val hunger = BackpackItemStackHelper.getHungerFromSlot(handler, slot, ::checkFilter) ?: continue

            if (hunger <= 20 - foodLevel)
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
