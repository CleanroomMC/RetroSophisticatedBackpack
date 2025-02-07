package com.cleanroommc.retrosophisticatedbackpack.proxy

import com.cleanroommc.retrosophisticatedbackpack.client.BackpackDynamicModel
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

abstract class RSBProxy {
    open fun preInit(event: FMLPreInitializationEvent) {}

    open fun postInit(event: FMLPostInitializationEvent) {}

    open fun registerItemRenderer(item: Item, meta: Int, id: String) {}

    class ServerProxy : RSBProxy() {}

    class ClientProxy : RSBProxy() {
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

        override fun preInit(event: FMLPreInitializationEvent) {
            super.preInit(event)
            ModelLoaderRegistry.registerLoader(BackpackDynamicModel.Loader())
        }
    }
}