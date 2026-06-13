package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IMagnetUpgrade

class MagnetUpgradeItem(registryName: String, wrapperFactory: () -> IMagnetUpgrade) :
    RankedUpgradeItem<IMagnetUpgrade>(registryName, wrapperFactory)
