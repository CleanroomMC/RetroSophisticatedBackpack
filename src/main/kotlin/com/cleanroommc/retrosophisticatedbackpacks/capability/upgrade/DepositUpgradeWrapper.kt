package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.DepositUpgradeItem
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

class DepositUpgradeWrapper : BasicUpgradeWrapper<DepositUpgradeItem>(), IDepositUpgrade {
    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.DEPOSIT_UPGRADE_CAPABILITY ||
                super<IDepositUpgrade>.hasCapability(capability, facing) ||
                super<BasicUpgradeWrapper>.hasCapability(capability, facing)
}