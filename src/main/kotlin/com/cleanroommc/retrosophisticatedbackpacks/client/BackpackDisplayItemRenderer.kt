package com.cleanroommc.retrosophisticatedbackpacks.client

import com.cleanroommc.retrosophisticatedbackpacks.backpack.DisplaySide
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
object BackpackDisplayItemRenderer {
    private const val MODEL_CENTER = 0.5
    private const val CENTER_Y = 0.6
    private const val FRONT_Z = -0.314
    private const val SIDE_X = 0.455
    private const val SCALE = 0.5f

    fun render(
        wrapper: BackpackWrapper,
        modelScale: Double = 1.0,
        itemScale: Float = 1.0f,
        originAtCenter: Boolean = true
    ) {
        val displayItem = wrapper.getDisplayItem() ?: return

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GlStateManager.pushMatrix()
        try {
            applySideTransform(displayItem.side, modelScale, originAtCenter)
            GlStateManager.rotate(displayItem.rotation.toFloat(), 0f, 0f, 1f)
            val scale = SCALE * itemScale
            GlStateManager.scale(scale, scale, scale)
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.enableRescaleNormal()
            GlStateManager.enableDepth()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
            RenderHelper.enableStandardItemLighting()
            Minecraft.getMinecraft().renderItem.renderItem(displayItem.stack, ItemCameraTransforms.TransformType.FIXED)
        } finally {
            RenderHelper.disableStandardItemLighting()
            GlStateManager.disableRescaleNormal()
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.popMatrix()
            GL11.glPopAttrib()
            RenderStateHelper.syncGlStateManagerCache()
        }
    }

    private fun applySideTransform(side: DisplaySide, modelScale: Double, originAtCenter: Boolean) {
        val invScale = 1.0 / modelScale
        fun x(relative: Double) =
            if (originAtCenter) relative * invScale else MODEL_CENTER + relative * invScale

        fun z(relative: Double) =
            if (originAtCenter) relative * invScale else MODEL_CENTER + relative * invScale

        when (side) {
            DisplaySide.FRONT -> {
                GlStateManager.translate(x(0.0), CENTER_Y * invScale, z(FRONT_Z))
            }

            DisplaySide.LEFT -> {
                GlStateManager.translate(x(SIDE_X), CENTER_Y * invScale, z(0.0))
                GlStateManager.rotate(-90f, 0f, 1f, 0f)
            }

            DisplaySide.RIGHT -> {
                GlStateManager.translate(x(-SIDE_X), CENTER_Y * invScale, z(0.0))
                GlStateManager.rotate(90f, 0f, 1f, 0f)
            }
        }
    }
}
