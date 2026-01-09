package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.PickupUpgradeItem
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
class ShapedUpgradeRecipe(
    group: ResourceLocation?,
    result: ItemStack,
    primer: CraftingHelper.ShapedPrimer
) : ShapedOreRecipe(group, result, primer) {
    override fun getCraftingResult(inventory: InventoryCrafting): ItemStack {
        val outputStack = super.getCraftingResult(inventory)

        if (!outputStack.isEmpty) {
            val stack = inventory.getStackInSlot(4)

            when (stack.item) {
                is PickupUpgradeItem -> {
                    val wrapper =
                        stack.getCapability(Capabilities.PICKUP_UPGRADE_CAPABILITY, null) ?: return outputStack
                    val newWrapper =
                        outputStack.getCapability(Capabilities.ADVANCED_PICKUP_UPGRADE_CAPABILITY, null)
                            ?: return outputStack

                    // Clones item filter settings, retains their relative position
                    newWrapper.filterType = wrapper.filterType

                    for (i in 0 until 9) {
                        newWrapper.filterItems.inventory[i / 3 + i] = wrapper.filterItems.inventory[i]
                    }
                }
            }
        }

        return outputStack
    }

    class Factory : RecipeFactoryTemplate<ShapedUpgradeRecipe>(::ShapedUpgradeRecipe)
}
