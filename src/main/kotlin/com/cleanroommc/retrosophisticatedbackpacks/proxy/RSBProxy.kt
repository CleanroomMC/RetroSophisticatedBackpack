package com.cleanroommc.retrosophisticatedbackpacks.proxy

import com.cleanroommc.bogosorter.BogoSortAPI
import com.cleanroommc.modularui.factory.GuiManager
import com.cleanroommc.retrosophisticatedbackpacks.client.BackpackDynamicModel
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlotWrapper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.Item
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.lwjgl.input.Keyboard

abstract class RSBProxy {
    open fun preInit(event: FMLPreInitializationEvent) {
        GuiManager.registerFactory(PlayerInventoryGuiFactory)

        if (Loader.isModLoaded("bogosorter")) {
            BogoSortAPI.INSTANCE.addSlotGetter(ModularBackpackSlot::class.java, ::ModularBackpackSlotWrapper)
            // BackpackContainer compat is added in its class
        }
    }

    open fun init(event: FMLInitializationEvent) {}

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