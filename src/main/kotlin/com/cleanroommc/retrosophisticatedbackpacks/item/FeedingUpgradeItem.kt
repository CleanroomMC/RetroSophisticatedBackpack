package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFeedingUpgrade

class FeedingUpgradeItem(registryName: String, wrapperFactory: () -> IFeedingUpgrade) :
    RankedUpgradeItem<IFeedingUpgrade>(registryName, wrapperFactory)
