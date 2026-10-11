package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IJukeboxUpgrade

class JukeboxUpgradeItem(val registryName: String, wrapperFactory: () -> IJukeboxUpgrade) :
    RankedUpgradeItem<IJukeboxUpgrade>(registryName, wrapperFactory)
