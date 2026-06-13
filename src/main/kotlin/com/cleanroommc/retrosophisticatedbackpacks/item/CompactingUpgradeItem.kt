package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.ICompactingUpgrade

class CompactingUpgradeItem(registryName: String, wrapperFactory: () -> ICompactingUpgrade) :
    RankedUpgradeItem<ICompactingUpgrade>(registryName, wrapperFactory)
