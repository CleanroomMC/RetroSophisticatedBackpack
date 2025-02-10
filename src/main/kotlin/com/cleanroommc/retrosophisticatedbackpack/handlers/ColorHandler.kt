package com.cleanroommc.retrosophisticatedbackpack.handlers

import com.cleanroommc.retrosophisticatedbackpack.Tags
import com.cleanroommc.retrosophisticatedbackpack.blocks.BackpackBlock
import com.cleanroommc.retrosophisticatedbackpack.blocks.Blocks
import com.cleanroommc.retrosophisticatedbackpack.items.BackpackItem
import com.cleanroommc.retrosophisticatedbackpack.items.Items
import com.cleanroommc.retrosophisticatedbackpack.tileentity.BackpackTileEntity
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.event.ColorHandlerEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
@SideOnly(Side.CLIENT)
object ColorHandler {
    @SubscribeEvent
    @JvmStatic
    fun onItemColors(event: ColorHandlerEvent.Item) {
        event.itemColors.registerItemColorHandler(object : IItemColor {
            override fun colorMultiplier(stack: ItemStack, tintIndex: Int): Int {
                if (tintIndex > 1 || stack.item !is BackpackItem)
                    return -1

                val backpackInventory =
                    stack.getCapability(CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY!!, null)!!

                return when (tintIndex) {
                    0 -> backpackInventory.mainColor
                    1 -> backpackInventory.accentColor
                    else -> -1
                }
            }
        }, *Items.BACKPACK_ITEMS.toTypedArray())
    }

    @SubscribeEvent
    @JvmStatic
    fun onBlockColors(event: ColorHandlerEvent.Block) {
        event.blockColors.registerBlockColorHandler(object : IBlockColor {
            override fun colorMultiplier(
                state: IBlockState,
                worldIn: IBlockAccess?,
                pos: BlockPos?,
                tintIndex: Int
            ): Int {
                if (tintIndex > 1 || state.block !is BackpackBlock || pos == null)
                    return -1

                val tileEntity = worldIn?.getTileEntity(pos) as BackpackTileEntity? ?: return -1
                val backpackInventory =
                    tileEntity.getCapability(CapabilityHandler.BACKPACK_ITEM_HANDLER_CAPABILITY!!, null)!!

                return when (tintIndex) {
                    0 -> backpackInventory.mainColor
                    1 -> backpackInventory.accentColor
                    else -> -1
                }
            }
        }, *Blocks.BACKPACK_BLOCKS.toTypedArray())
    }
}