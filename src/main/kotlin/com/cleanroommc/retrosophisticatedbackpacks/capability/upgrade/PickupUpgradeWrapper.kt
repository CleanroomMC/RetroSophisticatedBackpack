package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.PickupUpgradeItem
import net.minecraftforge.common.capabilities.Capability

class PickupUpgradeWrapper : BasicUpgradeWrapper<PickupUpgradeItem>(), IPickupUpgrade {
    override val acceptableCapabilities: List<Capability<*>>
        get() = listOf(
            Capabilities.PICKUP_UPGRADE_CAPABILITY,
            Capabilities.IPICKUP_UPGRADE_CAPABILITY,
            *super.acceptableCapabilities.toTypedArray()
        )
}