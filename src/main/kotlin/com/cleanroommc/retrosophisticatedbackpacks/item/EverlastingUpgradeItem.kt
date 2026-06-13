package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IEverlastingUpgrade

class EverlastingUpgradeItem(registryName: String, wrapperFactory: () -> IEverlastingUpgrade) :
    HiddenUpgradeItem<IEverlastingUpgrade>(registryName, wrapperFactory)
