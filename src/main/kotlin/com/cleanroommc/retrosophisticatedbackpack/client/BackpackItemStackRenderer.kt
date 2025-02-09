package com.cleanroommc.retrosophisticatedbackpack.client

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class BackpackItemStackRenderer : TileEntityItemStackRenderer() {
    private val mc = Minecraft.getMinecraft()

    override fun renderByItem(itemStackIn: ItemStack, partialTicks: Float) {
        val itemRenderer = mc.itemRenderer
        val model = mc.renderItem.getItemModelWithOverrides(itemStackIn, null, null)

        mc.renderItem.renderItem(itemStackIn, model)
    }
}