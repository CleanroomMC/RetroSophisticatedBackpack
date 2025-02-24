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

    val IN_OUT_ICON = icon("in_out", 0, 32)
    val IN_ICON = icon("in", 16, 32)
    val OUT_ICON = icon("out", 32, 32)

    val ADD_ICON = icon("add", 96, 32)
    val REMOVE_ICON = icon("remove", 112, 32)
    val BRAIN_ICON = icon("brain", 128, 32)

    val ONE_IN_FOUR_SLOT_ICON = icon("one_in_four_slot", 0, 80)
    val ALL_FOUR_SLOT_ICON = icon("all_in_four_slot", 16, 80)
    val NO_SORT_ICON = icon("no_sort", 32, 80)
    val NONE_FOUR_SLOT_ICON = icon("none_in_four_slot", 48, 80)

    val SETTING_ICON = icon("setting", 16, 96)

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