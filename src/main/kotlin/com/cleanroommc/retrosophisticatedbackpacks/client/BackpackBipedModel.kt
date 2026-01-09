package com.cleanroommc.retrosophisticatedbackpacks.client

import net.minecraft.client.Minecraft
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class BackpackBipedModel(private val backpackItemStack: ItemStack) : ModelBiped() {
    companion object {
        fun renderBackpack(backpackItemStack: ItemStack, entityIn: Entity) {
            GlStateManager.translate(0.0, 0.3, 0.225)
            GlStateManager.rotate(180f, 1f, 0f, 0f)

            if (entityIn.isSneaking) {
                GlStateManager.translate(0.0f, -0.05f, 0.0f)
                GlStateManager.rotate(28.647888f, 1.0f, 0.0f, 0.0f)
            }

            val mc = Minecraft.getMinecraft()

            mc.renderItem.renderItem(backpackItemStack, ItemCameraTransforms.TransformType.FIXED)
        }
    }

    override fun render(
        entityIn: Entity,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        scale: Float
    ) {
        GlStateManager.pushMatrix()

        bipedBody.postRender(0.0625f)
        renderBackpack(backpackItemStack, entityIn)

        GlStateManager.popMatrix()
    }
}
