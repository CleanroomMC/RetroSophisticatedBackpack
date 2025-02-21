package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.DepositUpgradeItem
import net.minecraftforge.common.capabilities.Capability

class AdvancedDepositUpgradeWrapper : AdvancedUpgradeWrapper<DepositUpgradeItem>(), IDepositUpgrade {
    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(
            Capabilities.ADVANCED_DEPOSIT_UPGRADE_CAPABILITY,
            Capabilities.IDEPOSIT_UPGRADE_CAPABILITY,
            *super.acceptableCapabilities.toTypedArray()
        )
}