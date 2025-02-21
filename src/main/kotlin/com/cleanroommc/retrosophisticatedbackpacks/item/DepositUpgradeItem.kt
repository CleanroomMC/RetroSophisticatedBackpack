package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IDepositUpgrade

class DepositUpgradeItem(registryName: String, wrapperFactory: () -> IDepositUpgrade) :
    RankedUpgradeItem<IDepositUpgrade>(registryName, wrapperFactory)
