package com.cleanroommc.retrosophisticatedbackpacks.proxy

import com.cleanroommc.bogosorter.BogoSortAPI
import com.cleanroommc.modularui.factory.GuiManager
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.block.Blocks
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.client.BackpackDynamicModel
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlotWrapper
import com.cleanroommc.retrosophisticatedbackpacks.crafting.DyeingRecipeRegistry
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary
import org.lwjgl.input.Keyboard

abstract class RSBProxy {
    open fun preInit(event: FMLPreInitializationEvent) {
        GuiManager.registerFactory(PlayerInventoryGuiFactory)

        if (Loader.isModLoaded("bogosorter")) {
            BogoSortAPI.INSTANCE.addSlotGetter(ModularBackpackSlot::class.java, ::ModularBackpackSlotWrapper)
            // BackpackContainer compat is added in its class
        }
    }

    open fun init(event: FMLInitializationEvent) {
        for (backpackItem in Items.BACKPACK_ITEMS) {
            OreDictionary.registerOre("backpack", backpackItem)
            OreDictionary.registerOre("sophisticatedBackpack", backpackItem)
        }
        for (backpackItemBlock in Blocks.BACKPACK_BLOCKS) {
            OreDictionary.registerOre("backpack", backpackItemBlock)
            OreDictionary.registerOre("sophisticatedBackpack", backpackItemBlock)
        }

        for (backpackItem in Items.BACKPACK_ITEMS) {
            val iter = EnumDyeColor.entries.toMutableList<EnumDyeColor?>()
            iter.add(null)
            
            for ((mainColor, accentColor) in iter.flatMap { a -> iter.map { a to it } }) {
                DyeingRecipeRegistry.register(backpackItem, mainColor, accentColor)
            }
        }
    }

    open fun postInit(event: FMLPostInitializationEvent) {}

    open fun registerItemRenderer(item: Item, meta: Int, id: String) {}

    class ServerProxy : RSBProxy()

    class ClientProxy : RSBProxy() {
        companion object {
            private val KEYBINDS: List<KeyBinding> by lazy {
                listOf(OPEN_BACKPACK_KEYBIND)
            }
            private val KEY_CATEGORY = "".asTranslationKey()

            val OPEN_BACKPACK_KEYBIND = KeyBinding(
                "key.open_backpack.desc".asTranslationKey(),
                Keyboard.KEY_B,
                "key.category".asTranslationKey()
            )
        }


        override fun registerItemRenderer(item: Item, meta: Int, id: String) {
            ModelLoader.setCustomModelResourceLocation(
                item,
                meta,
                ModelResourceLocation(
                    item.registryName ?: throw IllegalArgumentException("Missing registry name while registering Item"),
                    id
                )
            )
        }

        override fun init(event: FMLInitializationEvent) {
            super.init(event)

            for (keyBinding in KEYBINDS)
                ClientRegistry.registerKeyBinding(keyBinding)
        }

        override fun preInit(event: FMLPreInitializationEvent) {
            super.preInit(event)

            ModelLoaderRegistry.registerLoader(BackpackDynamicModel.Loader())
        }
    }
}