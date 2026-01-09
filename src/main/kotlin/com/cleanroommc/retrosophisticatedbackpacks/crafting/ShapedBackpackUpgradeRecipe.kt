package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.google.gson.JsonObject
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.JsonUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.IRecipeFactory
import net.minecraftforge.common.crafting.JsonContext
import net.minecraftforge.oredict.ShapedOreRecipe

@Suppress("UNUSED")
class ShapedBackpackUpgradeRecipe(
    group: ResourceLocation?,
    result: ItemStack,
    primer: CraftingHelper.ShapedPrimer
) : ShapedOreRecipe(group, result, primer) {
    override fun getCraftingResult(inventory: InventoryCrafting): ItemStack {
        val outputStack = super.getCraftingResult(inventory)

        if (!outputStack.isEmpty) {
            val backpackStack = inventory.getStackInSlot(4)
            val backpackWrapper =
                backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return outputStack
            val newBackpackWrapper =
                outputStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return outputStack

            for (i in 0 until backpackWrapper.backpackInventorySize()) {
                newBackpackWrapper.backpackItemStackHandler.inventory[i] =
                    backpackWrapper.backpackItemStackHandler.inventory[i]
                newBackpackWrapper.backpackItemStackHandler.memorizedSlotStack[i] =
                    backpackWrapper.backpackItemStackHandler.memorizedSlotStack[i]
                newBackpackWrapper.backpackItemStackHandler.sortLockedSlots[i] =
                    backpackWrapper.backpackItemStackHandler.sortLockedSlots[i]
            }

            newBackpackWrapper.sortType = backpackWrapper.sortType
            newBackpackWrapper.mainColor = backpackWrapper.mainColor
            newBackpackWrapper.accentColor = backpackWrapper.accentColor

            for (i in 0 until backpackWrapper.upgradeSlotsSize()) {
                newBackpackWrapper.upgradeItemStackHandler.inventory[i] =
                    backpackWrapper.upgradeItemStackHandler.inventory[i]
            }
        }

        return outputStack
    }

    class Factory : RecipeFactoryTemplate<ShapedBackpackUpgradeRecipe>(::ShapedBackpackUpgradeRecipe)
}
