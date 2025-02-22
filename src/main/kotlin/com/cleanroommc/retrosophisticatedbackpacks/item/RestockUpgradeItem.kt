package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IRestockUpgrade

class RestockUpgradeItem(registryName: String, wrapperFactory: () -> IRestockUpgrade) :
    RankedUpgradeItem<IRestockUpgrade>(registryName, wrapperFactory)
