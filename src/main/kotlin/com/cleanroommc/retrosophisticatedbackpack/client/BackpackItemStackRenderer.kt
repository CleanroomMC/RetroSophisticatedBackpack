package com.cleanroommc.retrosophisticatedbackpack.client

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.ItemStack

class BackpackItemStackRenderer : TileEntityItemStackRenderer() {
    private val mc = Minecraft.getMinecraft()

    override fun renderByItem(itemStackIn: ItemStack, partialTicks: Float) {
        val itemRenderer = mc.itemRenderer
        val model = mc.renderItem.getItemModelWithOverrides(itemStackIn, null, null)

        mc.renderItem.renderItem(itemStackIn, model)
    }
}