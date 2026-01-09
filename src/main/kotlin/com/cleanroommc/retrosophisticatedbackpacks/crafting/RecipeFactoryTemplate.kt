package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.JsonUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.IRecipeFactory
import net.minecraftforge.common.crafting.JsonContext

abstract class RecipeFactoryTemplate<T : IRecipe>(private val ctor: (ResourceLocation?, ItemStack, CraftingHelper.ShapedPrimer) -> T) :
    IRecipeFactory {
    override fun parse(context: JsonContext, json: JsonObject): IRecipe {
        val group = JsonUtils.getString(json, "group", "")
        val primer = RecipeUtil.parseShaped(context, json)
        val result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context)

        return ctor(if (group.isEmpty()) null else ResourceLocation(group), result, primer)
    }
}
