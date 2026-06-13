package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IVoidUpgrade

class VoidUpgradeItem(registryName: String, wrapperFactory: () -> IVoidUpgrade) :
    RankedUpgradeItem<IVoidUpgrade>(registryName, wrapperFactory)
