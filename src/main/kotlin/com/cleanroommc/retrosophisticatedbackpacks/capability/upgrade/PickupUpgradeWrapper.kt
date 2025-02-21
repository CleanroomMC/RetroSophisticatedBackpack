package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.PickupUpgradeItem
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class PickupUpgradeWrapper : BasicUpgradeWrapper<PickupUpgradeItem>(), IPickupUpgrade {
    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.PICKUP_UPGRADE_CAPABILITY ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing) ||
                super<IPickupUpgrade>.hasCapability(capability, facing)

}