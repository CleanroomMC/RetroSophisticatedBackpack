package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade

class FilterUpgradeItem(registryName: String, wrapperFactory: () -> IFilterUpgrade) :
    RankedUpgradeItem<IFilterUpgrade>(registryName, wrapperFactory)