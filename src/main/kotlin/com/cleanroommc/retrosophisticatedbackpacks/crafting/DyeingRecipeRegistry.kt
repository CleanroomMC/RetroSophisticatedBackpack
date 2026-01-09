package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper

object DyeingRecipeRegistry {
    fun constructRecipe(
        backpackItem: BackpackItem,
        mainColor: EnumDyeColor?,
        accentColor: EnumDyeColor?
    ): IRecipe? {
        if (mainColor == null && accentColor == null)
            return null

        val backpackStack = ItemStack(backpackItem, 1)
        val backpackWrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return null

        mainColor?.let { backpackWrapper.mainColor = it.colorValue }
        accentColor?.let { backpackWrapper.accentColor = it.colorValue }

        return if (mainColor != null && accentColor != null) {
            constructRecipe(
                ResourceLocation(
                    Tags.MOD_ID,
                    "${backpackItem.registryName?.path}_dye_both_${mainColor.dyeDamage}_${accentColor.dyeDamage}"
                ),
                backpackStack,
                "   ",
                " BD",
                " R ",
                'B',
                ItemStack(backpackItem, 1),
                'D',
                ItemStack(net.minecraft.init.Items.DYE, 1, mainColor.dyeDamage),
                'R',
                ItemStack(net.minecraft.init.Items.DYE, 1, accentColor.dyeDamage)
            )
        } else if (mainColor != null) {
            constructRecipe(
                ResourceLocation(Tags.MOD_ID, "${backpackItem.registryName?.path}_dye_main_${mainColor.dyeDamage}"),
                backpackStack,
                "   ",
                " BD",
                "   ",
                'B',
                ItemStack(backpackItem, 1),
                'D',
                ItemStack(net.minecraft.init.Items.DYE, 1, mainColor.dyeDamage)
            )
        } else if (accentColor != null) {
            constructRecipe(
                ResourceLocation(Tags.MOD_ID, "${backpackItem.registryName?.path}_dye_accent_${accentColor.dyeDamage}"),
                backpackStack,
                "   ",
                " B ",
                " D ",
                'B',
                ItemStack(backpackItem, 1),
                'D',
                ItemStack(net.minecraft.init.Items.DYE, 1, accentColor.dyeDamage)
            )
        } else null
    }

    fun constructRecipe(name: ResourceLocation, output: ItemStack, vararg params: Any): IRecipe {
        val primer = CraftingHelper.parseShaped(*params)
        return ShapedDyeingNBTRecipe(
            primer.input,
            output
        ).setRegistryName(name)
    }
}
