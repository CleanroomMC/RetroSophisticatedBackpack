package com.cleanroommc.retrosophisticatedbackpack.handlers

import com.cleanroommc.retrosophisticatedbackpack.Tags
import com.cleanroommc.retrosophisticatedbackpack.blocks.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpack.blocks.Blocks
import com.cleanroommc.retrosophisticatedbackpack.items.BackpackItem
import com.cleanroommc.retrosophisticatedbackpack.items.Items
import com.cleanroommc.retrosophisticatedbackpack.tileentity.BackpackTileEntity
import com.cleanroommc.retrosophisticatedbackpack.utils.IModelRegister
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.event.ColorHandlerEvent
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
object RegistryHandler {
    @JvmField
    val MODELS = mutableListOf<IModelRegister>()

    @SubscribeEvent
    @JvmStatic
    fun onItemRegister(event: RegistryEvent.Register<Item>) {
        event.registry.registerAll(*Items.ITEMS.toTypedArray())
    }

    @SubscribeEvent
    @JvmStatic
    fun onBlockRegister(event: RegistryEvent.Register<Block>) {
        event.registry.registerAll(*Blocks.BLOCKS.toTypedArray())

        GameRegistry.registerTileEntity(BackpackTileEntity::class.java, ResourceLocation(Tags.MOD_ID, "backpack"))
    }

    @SubscribeEvent
    @JvmStatic
    fun onModelRegister(event: ModelRegistryEvent) {
        for (model in MODELS)
            model.registerModels()
    }
}