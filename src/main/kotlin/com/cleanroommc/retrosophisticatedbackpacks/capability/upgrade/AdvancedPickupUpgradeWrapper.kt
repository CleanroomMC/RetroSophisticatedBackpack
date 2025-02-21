package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.PickupUpgradeItem
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class AdvancedPickupUpgradeWrapper : AdvancedUpgradeWrapper<PickupUpgradeItem>(), IPickupUpgrade {
    override fun canPickup(stack: ItemStack): Boolean =
        checkFilter(stack)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_PICKUP_UPGRADE_CAPABILITY ||
                super<IPickupUpgrade>.hasCapability(capability, facing) ||
                super<AdvancedUpgradeWrapper>.hasCapability(capability, facing)
}