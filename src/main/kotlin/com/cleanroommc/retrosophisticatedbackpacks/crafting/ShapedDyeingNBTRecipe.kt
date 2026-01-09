package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.crafting.ShapedRecipes
import net.minecraft.util.NonNullList

class ShapedDyeingNBTRecipe(ingredients: NonNullList<Ingredient>, result: ItemStack) :
    ShapedRecipes("", 3, 3, ingredients, result) {
    override fun getCraftingResult(inventory: InventoryCrafting): ItemStack {
        val backpackStack = inventory.getStackInSlot(4)
        val outputStack = recipeOutput.copy()

        if (!backpackStack.isEmpty && backpackStack.item is BackpackItem) {
            val backpackWrapper =
                backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return outputStack
            val newBackpackWrapper =
                outputStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return outputStack
            val mainColor = newBackpackWrapper.mainColor
            val accentColor = newBackpackWrapper.accentColor

            newBackpackWrapper.deserializeNBT(backpackWrapper.serializeNBT())
            newBackpackWrapper.mainColor = mainColor
            newBackpackWrapper.accentColor = accentColor
        }

        return outputStack
    }
}
