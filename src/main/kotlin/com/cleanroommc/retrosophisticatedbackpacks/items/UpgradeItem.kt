package com.cleanroommc.retrosophisticatedbackpacks.items

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.handlers.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.utils.Utils.asTranslationKey

sealed class UpgradeItem(
    registryName: String,
    val hasTab: Boolean = false,
    val hasToggle: Boolean = false
) : ItemBase() {
    init {
        setMaxStackSize(1)
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setRegistryName(registryName)
        setTranslationKey(registryName.asTranslationKey())

        Items.ITEMS.add(this)
        RegistryHandler.MODELS.add(this)
    }
}