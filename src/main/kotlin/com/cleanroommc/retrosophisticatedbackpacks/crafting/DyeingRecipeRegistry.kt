package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.mixin.GameRegistryAccessor
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper

object DyeingRecipeRegistry {
    fun register(backpackItem: BackpackItem, mainColor: EnumDyeColor?, accentColor: EnumDyeColor?) {
        if (mainColor == null && accentColor == null)
            return

        val backpackStack = ItemStack(backpackItem, 1)
        val backpackWrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return

        mainColor?.let { backpackWrapper.mainColor = it.colorValue }
        accentColor?.let { backpackWrapper.accentColor = it.colorValue }

        if (mainColor != null && accentColor != null) {
            addShapeDyeingNBTdRecipe(
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
            addShapeDyeingNBTdRecipe(
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
            addShapeDyeingNBTdRecipe(
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
        }
    }

    fun addShapeDyeingNBTdRecipe(name: ResourceLocation, output: ItemStack, vararg params: Any) {
        val primer = CraftingHelper.parseShaped(*params)
        GameRegistryAccessor.invokeRegister(
            ShapedDyeingNBTRecipe(
                primer.input,
                output
            ).setRegistryName(name)
        )
    }
}
