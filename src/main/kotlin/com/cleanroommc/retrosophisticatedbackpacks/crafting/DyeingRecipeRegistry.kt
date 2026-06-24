package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.mixin.EnumDyeColorAccessor
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.cast
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper

object DyeingRecipeRegistry {
    private val DYES: Array<String> = arrayOf(
        "Black",
        "Red",
        "Green",
        "Brown",
        "Blue",
        "Purple",
        "Cyan",
        "LightGray",
        "Gray",
        "Pink",
        "Lime",
        "Yellow",
        "LightBlue",
        "Magenta",
        "Orange",
        "White"
    )

    fun constructRecipe(
        backpackItem: BackpackItem,
        mainColor: EnumDyeColor?,
        accentColor: EnumDyeColor?
    ): IRecipe? {
        val backpackStack = ItemStack(backpackItem, 1)
        val backpackWrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return null

        backpackWrapper.mainColor = mainColor?.cast<EnumDyeColorAccessor>()?.`rsb$getColorValue`() ?: BackpackWrapper.DEFAULT_MAIN_COLOR
        backpackWrapper.accentColor = accentColor?.cast<EnumDyeColorAccessor>()?.`rsb$getColorValue`() ?: BackpackWrapper.DEFAULT_ACCENT_COLOR

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
                "dye${DYES[mainColor.dyeDamage]}",
                'R',
                "dye${DYES[accentColor.dyeDamage]}",
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
                "dye${DYES[mainColor.dyeDamage]}",
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
                "dye${DYES[accentColor.dyeDamage]}",
            )
        } else {
            constructRecipe(
                ResourceLocation(Tags.MOD_ID, "${backpackItem.registryName?.path}_dye_none"),
                backpackStack,
                "   ",
                " B ",
                "   ",
                'B',
                ItemStack(backpackItem, 1),
            )
        }
    }

    fun constructRecipe(name: ResourceLocation, output: ItemStack, vararg params: Any): IRecipe {
        val primer = CraftingHelper.parseShaped(*params)
        return ShapedDyeingNBTRecipe(
            primer.input,
            output
        ).setRegistryName(name)
    }
}
