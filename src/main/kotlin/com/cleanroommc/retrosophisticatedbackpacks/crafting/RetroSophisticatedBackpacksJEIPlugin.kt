package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackCraftingTransferInfo
import mezz.jei.api.IJeiHelpers
import mezz.jei.api.IModPlugin
import mezz.jei.api.IModRegistry
import mezz.jei.api.JEIPlugin
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry

@JEIPlugin
class RetroSophisticatedBackpacksJEIPlugin : IModPlugin {
    companion object {
        lateinit var helpers: IJeiHelpers
    }

    override fun register(registry: IModRegistry) {
        helpers = registry.jeiHelpers

        // By hijacking vanilla recipe types
        // We can utilize JEI's built-in recipe transfer handler
        val recipeTransferRegistry: IRecipeTransferRegistry = registry.recipeTransferRegistry
        recipeTransferRegistry.addRecipeTransferHandler<BackpackContainer>(BackpackCraftingTransferInfo())
    }
}
