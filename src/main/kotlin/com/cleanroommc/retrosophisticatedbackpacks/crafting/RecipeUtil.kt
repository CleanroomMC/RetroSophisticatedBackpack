package com.cleanroommc.retrosophisticatedbackpacks.crafting

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.JsonUtils
import net.minecraft.util.NonNullList
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer
import net.minecraftforge.common.crafting.JsonContext


object RecipeUtil {
    fun parseShaped(context: JsonContext, json: JsonObject): ShapedPrimer {
        val ingredientMap: MutableMap<Char?, Ingredient> = Maps.newHashMap()
        for (entry in JsonUtils.getJsonObject(json, "key").entrySet()) {
            if (entry.key.length != 1) throw JsonSyntaxException("Invalid key entry: '" + entry.key + "' is an invalid symbol (must be 1 character only).")
            if (" " == entry.key) throw JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.")

            ingredientMap.put(entry.key.toCharArray()[0], CraftingHelper.getIngredient(entry.value, context))
        }

        ingredientMap.put(' ', Ingredient.EMPTY)

        val patternJ = JsonUtils.getJsonArray(json, "pattern")

        if (patternJ.size() == 0) throw JsonSyntaxException("Invalid pattern: empty pattern not allowed")

        val pattern: Array<String> = Array(patternJ.size()) { "" }
        for (x in pattern.indices) {
            val line: String = JsonUtils.getString(patternJ.get(x), "pattern[$x]")
            if (x > 0 && pattern[0].length != line.length) throw JsonSyntaxException("Invalid pattern: each row must  be the same width")
            pattern[x] = line
        }

        val primer = ShapedPrimer()
        primer.width = pattern[0].length
        primer.height = pattern.size
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true)
        primer.input = NonNullList.withSize<Ingredient?>(primer.width * primer.height, Ingredient.EMPTY)

        val keys: MutableSet<Char?> = Sets.newHashSet(ingredientMap.keys)
        keys.remove(' ')

        var index = 0
        for (line in pattern) {
            for (chr in line.toCharArray()) {
                val ing = ingredientMap[chr]
                if (ing == null) throw JsonSyntaxException("Pattern references symbol '$chr' but it's not defined in the key")
                primer.input[index++] = ing
                keys.remove(chr)
            }
        }

        if (!keys.isEmpty()) throw JsonSyntaxException("Key defines symbols that aren't used in pattern: $keys")

        return primer
    }
}