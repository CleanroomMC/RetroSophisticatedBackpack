package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IPickupUpgrade

class PickupUpgradeItem(val registryName: String, wrapperFactory: () -> IPickupUpgrade) :
    RankedUpgradeItem<IPickupUpgrade>(registryName, wrapperFactory)
