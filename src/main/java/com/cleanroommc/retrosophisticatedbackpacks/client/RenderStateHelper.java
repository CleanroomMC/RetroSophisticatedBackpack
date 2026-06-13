package com.cleanroommc.retrosophisticatedbackpacks.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public final class RenderStateHelper {
    private static final ByteBuffer COLOR_MASK = BufferUtils.createByteBuffer(16);

    private RenderStateHelper() {
    }

    public static void syncGlStateManagerCache() {
        forceSetToggle(GL11.glIsEnabled(GL11.GL_DEPTH_TEST), GlStateManager::enableDepth, GlStateManager::disableDepth);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_BLEND), GlStateManager::enableBlend, GlStateManager::disableBlend);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_CULL_FACE), GlStateManager::enableCull, GlStateManager::disableCull);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_LIGHTING), GlStateManager::enableLighting, GlStateManager::disableLighting);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_ALPHA_TEST), GlStateManager::enableAlpha, GlStateManager::disableAlpha);
        forceSetToggle(GL11.glIsEnabled(GL11.GL_FOG), GlStateManager::enableFog, GlStateManager::disableFog);
        GlStateManager.setActiveTexture(GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE));
        forceSetToggle(GL11.glIsEnabled(GL11.GL_TEXTURE_2D), GlStateManager::enableTexture2D, GlStateManager::disableTexture2D);
        forceSetToggle(GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL), GlStateManager::enableRescaleNormal, GlStateManager::disableRescaleNormal);

        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        GlStateManager.depthMask(!depthMask);
        GlStateManager.depthMask(depthMask);
        GlStateManager.depthFunc(GL11.glGetInteger(GL11.GL_DEPTH_FUNC));
        GlStateManager.alphaFunc(GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC), GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF));
        GlStateManager.tryBlendFuncSeparate(
                GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),
                GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),
                GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA)
        );
        syncColorMask();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.color(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.bindTexture(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));
    }

    private static void syncColorMask() {
        COLOR_MASK.clear();
        GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK, COLOR_MASK);
        GlStateManager.colorMask(COLOR_MASK.get(0) != 0, COLOR_MASK.get(1) != 0, COLOR_MASK.get(2) != 0, COLOR_MASK.get(3) != 0);
    }

    private static void forceSetToggle(boolean desired, Runnable enable, Runnable disable) {
        if (desired) {
            disable.run();
            enable.run();
        } else {
            enable.run();
            disable.run();
        }
    }
}
