package com.cleanroommc.retrosophisticatedbackpacks.client.gui

import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.drawable.UITexture
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

object RSBTextures {
    private val ICON_LOCATION = ResourceLocation(Tags.MOD_ID, "gui/icons")

    val CHECK_ICON = icon("check", 0, 0)
    val CROSS_ICON = icon("cross", 16, 0)

    val MATCH_NBT_ICON = icon("consider_nbt", 32, 0)
    val IGNORE_NBT_ICON = icon("ignore_nbt", 48, 0)

    val COMPLETE_HUNGER_ICON = icon("complete_hunger", 96, 0)
    val HALF_HUNGER_ICON = icon("half_hunger", 112, 0)
    val IMMEDIATE_HUNGER_ICON = icon("impose_hunger", 128, 0)

    val MATCH_DURABILITY_ICON = icon("consider_duration", 0, 16)
    val IGNORE_DURABILITY_ICON = icon("ignore_duration", 16, 16)

    val HALF_HEART_ICON = icon("half_heart", 96, 16)
    val IGNORE_HALF_HEART_ICON = icon("ignore_half_heart", 112, 16)

    val BY_MOD_ID_ICON = icon("by_mod_id", 32, 16)
    val BY_ITEM_ICON = ItemDrawable(ItemStack(Items.APPLE))

    val ADD_ICON = icon("add", 96, 32)
    val REMOVE_ICON = icon("remove", 112, 32)

    val MATCH_ORE_DICT_ICON = icon("consider_ore_dict", 112, 96)
    val IGNORE_ORE_DICT_ICON = icon("ignore_ore_dict", 128, 96)

    val TOGGLE_DISABLE_ICON = icon("disable", 0, 128, 4, 10)
    val TOGGLE_ENABLE_ICON = icon("enable", 4, 128, 4, 10)

    private fun icon(name: String, x: Int, y: Int, w: Int = 16, h: Int = 16): UITexture =
        UITexture.builder()
            .location(ICON_LOCATION)
            .imageSize(256, 256)
            .uv(x, y, w, h)
            .name(name)
            .build()
}