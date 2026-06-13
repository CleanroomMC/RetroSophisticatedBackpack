package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IRefillUpgrade

class RefillUpgradeItem(registryName: String, wrapperFactory: () -> IRefillUpgrade) :
    RankedUpgradeItem<IRefillUpgrade>(registryName, wrapperFactory)
