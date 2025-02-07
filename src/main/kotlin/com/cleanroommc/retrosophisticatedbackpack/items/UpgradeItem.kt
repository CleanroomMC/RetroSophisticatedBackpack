package com.cleanroommc.retrosophisticatedbackpack.items

import com.cleanroommc.retrosophisticatedbackpack.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpack.handlers.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpack.utils.Utils.asTranslationKey

sealed class UpgradeItem(registryName: String): ItemBase() {
    init {
        setMaxStackSize(1)
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setRegistryName(registryName)
        setTranslationKey(registryName.asTranslationKey())

        Items.ITEMS.add(this)
        RegistryHandler.MODELS.add(this)
    }
}