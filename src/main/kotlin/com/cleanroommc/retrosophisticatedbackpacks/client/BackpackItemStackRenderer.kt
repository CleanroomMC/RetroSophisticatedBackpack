package com.cleanroommc.retrosophisticatedbackpacks.client

import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class BackpackItemStackRenderer : TileEntityItemStackRenderer() {
    private val mc = Minecraft.getMinecraft()

    override fun renderByItem(itemStackIn: ItemStack, partialTicks: Float) {
        val model = mc.renderItem.getItemModelWithOverrides(itemStackIn, null, null)

        mc.renderItem.renderModel(model, itemStackIn)

        itemStackIn.getCapability(Capabilities.BACKPACK_CAPABILITY, null)?.let {
            BackpackDisplayItemRenderer.render(it, itemScale = 0.7f, originAtCenter = false)
        }
    }
}
