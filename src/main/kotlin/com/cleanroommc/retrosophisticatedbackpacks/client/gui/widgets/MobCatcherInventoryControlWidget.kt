package com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets

import com.cleanroommc.modularui.api.drawable.IKey
import com.cleanroommc.modularui.api.layout.IViewportStack
import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.modularui.screen.RichTooltip
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext
import com.cleanroommc.modularui.theme.WidgetThemeEntry
import com.cleanroommc.modularui.widget.Widget
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.CapturedMob
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherStorage
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.BackpackPanel
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.RSBTextures
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SMobCatcherReleasePacket
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EntityList
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class MobCatcherInventoryControlWidget(private val panel: BackpackPanel) :
    Widget<MobCatcherInventoryControlWidget>(), Interactable {
    companion object {
        private const val SLOT_SIZE = 18
        private const val CAPTURED_MOB_SLOT_OFFSET = 1
        private val GUI_CONTROLS = ResourceLocation(Tags.MOD_ID, "textures/gui/gui_controls.png")
        private const val GUI_CONTROLS_TEXTURE_WIDTH = 256
        private const val GUI_CONTROLS_TEXTURE_HEIGHT = 256
        private const val CAPTURED_MOB_BACKGROUND_U = 29
        private const val CAPTURED_MOB_BACKGROUND_V = 30
        private const val CAPTURED_MOB_BACKGROUND_WIDTH = 18
        private const val CAPTURED_MOB_BACKGROUND_HEIGHT = 54
        private const val CAPTURED_MOB_BACKGROUND_COLOR = 0xFF2B2B2B.toInt()
        private const val CAPTURED_MOB_BACKGROUND_LAYER_COLOR = 0x184A4A4A
        private const val BODY_YAW_RANGE = 50f
        private const val HEAD_STATIC_YAW_RANGE = 24f
        private const val HEAD_IDLE_YAW_AMPLITUDE = 33f
        private const val HEAD_STATIC_PITCH_RANGE = 6f
        private const val HEAD_IDLE_PITCH_AMPLITUDE = 10f
        private const val HEAD_IDLE_MIN_CYCLE_TICKS = 340f
        private const val HEAD_IDLE_CYCLE_VARIATION_TICKS = 180f
        private const val HEAD_IDLE_MOVE_TICKS = 30f
        private const val HEAD_IDLE_HOLD_TICKS = 15f
    }

    private val renderEntities = mutableMapOf<java.util.UUID, EntityLivingBase>()

    init {
        size(panel.backpackSlotsWidth, panel.colSize * SLOT_SIZE)
        tooltipAutoUpdate(true)
        tooltipDynamic { tooltip ->
            val hovered = hoveredCapturedMob(getContext().mouseX, getContext().mouseY) ?: return@tooltipDynamic
            val entity = renderEntity(hovered)
            tooltip.addLine(IKey.str(tooltipDisplayName(hovered, entity)))
            tooltip.addLine(
                IKey.lang("gui.mob_catcher.click_to_release".asTranslationKey())
                    .style(TextFormatting.GRAY, TextFormatting.ITALIC)
            )
            tooltip.addLine(
                IKey.str(
                    "${TextFormatting.RED}\u2665 ${hovered.currentHealth}/${hovered.maxHealth}"
                )
            )
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE)
        }
    }

    override fun isInside(stack: IViewportStack, mx: Int, my: Int, absolute: Boolean): Boolean {
        if (panel.isSearchViewActive())
            return false

        val x = if (absolute) stack.unTransformX(mx.toFloat(), my.toFloat()) else mx
        val y = if (absolute) stack.unTransformY(mx.toFloat(), my.toFloat()) else my
        return hoveredCapturedMob(x, y) != null
    }

    override fun canHoverThrough(): Boolean = true

    override fun canClickThrough(): Boolean = true

    override fun onMousePressed(mouseButton: Int): Interactable.Result {
        if (panel.isSearchViewActive())
            return Interactable.Result.IGNORE

        if (mouseButton != 0) {
            return Interactable.Result.STOP
        }
        val capturedMob = hoveredCapturedMob(context.mouseX, context.mouseY) ?: return Interactable.Result.IGNORE
        NetworkHandler.INSTANCE.sendToServer(C2SMobCatcherReleasePacket(capturedMob.id))
        Interactable.playButtonClickSound()
        return Interactable.Result.SUCCESS
    }

    override fun draw(context: ModularGuiContext, widgetTheme: WidgetThemeEntry<*>) {
        if (panel.isSearchViewActive())
            return

        panel.backpackWrapper.ensureCapturedMobLayoutCurrent()
        val theme = widgetTheme.theme
        val hoveredMob = hoveredCapturedMob(context.mouseX, context.mouseY)
        val capturedMobs = MobCatcherStorage.getCapturedMobs(panel.backpackWrapper)
            .filter { isValidBackpackSlot(it.slot) }
            .map { capturedMob ->
                CapturedMobRenderInfo(
                    capturedMob,
                    slotX(capturedMob.slot),
                    slotY(capturedMob.slot),
                    capturedMob.width * SLOT_SIZE,
                    capturedMob.height * SLOT_SIZE
                )
            }

        for (renderInfo in capturedMobs) {
            renderCapturedMobArea(renderInfo.x, renderInfo.y, renderInfo.capturedMob.width, renderInfo.capturedMob.height)
        }
        for (renderInfo in capturedMobs) {
            renderEntity(renderInfo.capturedMob)?.let { entity ->
                val state = EntityRenderState.capture(entity)
                try {
                    prepareEntityForRender(entity, renderInfo.capturedMob, context)
                    val scale = getRenderScale(entity, renderInfo.width, renderInfo.height)
                    renderEntityInInventory(
                        renderInfo.x + renderInfo.width / 2,
                        getRenderBottomY(entity, renderInfo.y, renderInfo.height, scale),
                        scale,
                        entity,
                        context.partialTicks
                    )
                } finally {
                    state.restore(entity)
                }
            }
        }
        capturedMobs.firstOrNull { hoveredMob?.id == it.capturedMob.id }?.let {
            RSBTextures.SOLID_DOWN_ARROW_ICON.draw(context, it.x + it.width - 14, it.y + it.height - 14, 12, 12, theme)
        }
    }

    private fun renderCapturedMobArea(x: Int, y: Int, widthSlots: Int, heightSlots: Int) {
        val backgroundX = x - 1
        val backgroundY = y - 1
        val width = widthSlots * SLOT_SIZE
        val height = heightSlots * SLOT_SIZE
        renderTiledControlBackground(
            backgroundX,
            backgroundY,
            width,
            height,
            CAPTURED_MOB_BACKGROUND_U,
            CAPTURED_MOB_BACKGROUND_V,
            CAPTURED_MOB_BACKGROUND_WIDTH,
            CAPTURED_MOB_BACKGROUND_HEIGHT
        )
        renderCapturedMobBackgroundLayers(backgroundX + 1, backgroundY + 1, width - 2, height - 2)
    }

    private fun renderCapturedMobBackgroundLayers(x: Int, y: Int, width: Int, height: Int) {
        GuiDraw.drawRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), CAPTURED_MOB_BACKGROUND_COLOR)
        val layers = max(1, min(5, min(width, height) / 5))
        for (layer in 0 until layers) {
            val insetX = 1 + layer * width / (layers * 3)
            val insetY = 1 + layer * height / (layers * 3)
            val alpha = 24 + layer * 12
            val color = (alpha shl 24) or (CAPTURED_MOB_BACKGROUND_LAYER_COLOR and 0x00FFFFFF)
            GuiDraw.drawRect(
                (x + insetX).toFloat(),
                (y + insetY).toFloat(),
                (width - insetX * 2).toFloat(),
                (height - insetY * 2).toFloat(),
                color
            )
        }
        restoreTexturedGuiState()
    }

    private fun restoreTexturedGuiState() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun renderTiledControlBackground(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        u: Int,
        v: Int,
        textureWidth: Int,
        textureHeight: Int
    ) {
        val leftWidth = 1
        val topHeight = 1
        val rightWidth = min(1, width - leftWidth)
        val bottomHeight = min(1, height - topHeight)
        val sourceRightU = u + textureWidth - rightWidth
        val sourceBottomV = v + textureHeight - bottomHeight
        val centerWidth = width - leftWidth - rightWidth
        val centerHeight = height - topHeight - bottomHeight
        val sourceCenterWidth = textureWidth - leftWidth - rightWidth
        val sourceCenterHeight = textureHeight - topHeight - bottomHeight

        GlStateManager.color(1f, 1f, 1f, 1f)
        drawGuiControlsTexture(x, y, leftWidth, topHeight, u, v)
        drawGuiControlsTexture(x + leftWidth + centerWidth, y, rightWidth, topHeight, sourceRightU, v)
        drawGuiControlsTexture(x, y + topHeight + centerHeight, leftWidth, bottomHeight, u, sourceBottomV)
        drawGuiControlsTexture(
            x + leftWidth + centerWidth,
            y + topHeight + centerHeight,
            rightWidth,
            bottomHeight,
            sourceRightU,
            sourceBottomV
        )
        renderTiledTexture(x + leftWidth, y, centerWidth, topHeight, u + leftWidth, v, sourceCenterWidth, topHeight)
        renderTiledTexture(
            x + leftWidth,
            y + topHeight + centerHeight,
            centerWidth,
            bottomHeight,
            u + leftWidth,
            sourceBottomV,
            sourceCenterWidth,
            bottomHeight
        )
        renderTiledTexture(x, y + topHeight, leftWidth, centerHeight, u, v + topHeight, leftWidth, sourceCenterHeight)
        renderTiledTexture(
            x + leftWidth + centerWidth,
            y + topHeight,
            rightWidth,
            centerHeight,
            sourceRightU,
            v + topHeight,
            rightWidth,
            sourceCenterHeight
        )
        renderTiledTexture(
            x + leftWidth,
            y + topHeight,
            centerWidth,
            centerHeight,
            u + leftWidth,
            v + topHeight,
            sourceCenterWidth,
            sourceCenterHeight
        )
    }

    private fun renderTiledTexture(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        u: Int,
        v: Int,
        textureWidth: Int,
        textureHeight: Int
    ) {
        if (width <= 0 || height <= 0 || textureWidth <= 0 || textureHeight <= 0) {
            return
        }
        var renderedY = 0
        while (renderedY < height) {
            val chunkHeight = min(textureHeight, height - renderedY)
            var renderedX = 0
            while (renderedX < width) {
                val chunkWidth = min(textureWidth, width - renderedX)
                drawGuiControlsTexture(x + renderedX, y + renderedY, chunkWidth, chunkHeight, u, v)
                renderedX += chunkWidth
            }
            renderedY += chunkHeight
        }
    }

    private fun drawGuiControlsTexture(x: Int, y: Int, width: Int, height: Int, u: Int, v: Int) {
        if (width <= 0 || height <= 0) {
            return
        }
        GuiDraw.drawTexture(
            GUI_CONTROLS,
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            u,
            v,
            GUI_CONTROLS_TEXTURE_WIDTH,
            GUI_CONTROLS_TEXTURE_HEIGHT
        )
    }

    private fun renderEntityInInventory(posX: Int, posY: Float, scale: Int, entity: EntityLivingBase, partialTicks: Float) {
        val renderManager = Minecraft.getMinecraft().renderManager
        val previousViewY = renderManager.playerViewY
        val previousShadow = renderManager.isRenderShadow
        val previousBrightnessX = OpenGlHelper.lastBrightnessX
        val previousBrightnessY = OpenGlHelper.lastBrightnessY
        restoreTexturedGuiState()
        GlStateManager.enableColorMaterial()
        GlStateManager.enableDepth()
        GlStateManager.pushMatrix()
        try {
            GlStateManager.translate(posX.toFloat(), posY, 50f)
            GlStateManager.scale((-scale).toFloat(), scale.toFloat(), scale.toFloat())
            GlStateManager.rotate(180f, 0f, 0f, 1f)
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 220f, 220f)
            enableFrontEntityLighting()
            GlStateManager.disableCull()
            renderManager.setPlayerViewY(180f)
            renderManager.setRenderShadow(false)
            renderManager.renderEntity(entity, 0.0, 0.0, 0.0, 0f, partialTicks, false)
        } finally {
            renderManager.setRenderShadow(previousShadow)
            renderManager.setPlayerViewY(previousViewY)
            GlStateManager.popMatrix()
            GlStateManager.enableCull()
            RenderHelper.disableStandardItemLighting()
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousBrightnessX, previousBrightnessY)
            GlStateManager.disableRescaleNormal()
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
            GlStateManager.disableTexture2D()
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
            GlStateManager.disableDepth()
            restoreTexturedGuiState()
        }
    }

    private fun enableFrontEntityLighting() {
        GlStateManager.enableLighting()
        GlStateManager.enableLight(0)
        GlStateManager.enableLight(1)
        GlStateManager.enableColorMaterial()
        GlStateManager.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE)
        setFrontEntityLight(GL11.GL_LIGHT0, 0f, 0.65f, 1f, 0.55f)
        setFrontEntityLight(GL11.GL_LIGHT1, 0f, -0.25f, 1f, 0.35f)
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, RenderHelper.setColorBuffer(0.32f, 0.32f, 0.32f, 1f))
    }

    private fun setFrontEntityLight(light: Int, x: Float, y: Float, z: Float, diffuse: Float) {
        val length = sqrt((x * x + y * y + z * z).toDouble()).toFloat().takeIf { it > 0f } ?: 1f
        GlStateManager.glLight(light, GL11.GL_POSITION, RenderHelper.setColorBuffer(x / length, y / length, z / length, 0f))
        GlStateManager.glLight(light, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(diffuse, diffuse, diffuse, 1f))
        GlStateManager.glLight(light, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0f, 0f, 0f, 1f))
        GlStateManager.glLight(light, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0f, 0f, 0f, 1f))
    }

    private fun hoveredCapturedMob(mouseX: Int, mouseY: Int): CapturedMob? {
        panel.backpackWrapper.ensureCapturedMobLayoutCurrent()
        return MobCatcherStorage.getCapturedMobs(panel.backpackWrapper).firstOrNull { capturedMob ->
            if (!isValidBackpackSlot(capturedMob.slot)) {
                return@firstOrNull false
            }
            val x = slotX(capturedMob.slot) - 1
            val y = slotY(capturedMob.slot) - 1
            mouseX >= x && mouseX < x + capturedMob.width * SLOT_SIZE &&
                    mouseY >= y && mouseY < y + capturedMob.height * SLOT_SIZE
        }
    }

    private fun isValidBackpackSlot(slot: Int): Boolean =
        slot in 0 until panel.backpackWrapper.backpackInventorySize()

    private fun slotX(slot: Int): Int = slot % panel.rowSize * SLOT_SIZE + CAPTURED_MOB_SLOT_OFFSET

    private fun slotY(slot: Int): Int = slot / panel.rowSize * SLOT_SIZE + CAPTURED_MOB_SLOT_OFFSET

    private fun renderEntity(capturedMob: CapturedMob): EntityLivingBase? {
        renderEntities[capturedMob.id]?.let { return it }
        val world = Minecraft.getMinecraft().world ?: return null
        val entity = EntityList.createEntityByIDFromName(capturedMob.entityType, world) as? EntityLivingBase ?: return null
        entity.readFromNBT(capturedMob.entityNbt.copy() as net.minecraft.nbt.NBTTagCompound)
        renderEntities[capturedMob.id] = entity
        return entity
    }

    private fun tooltipDisplayName(capturedMob: CapturedMob, entity: EntityLivingBase?): String =
        when {
            entity == null -> capturedMob.displayName
            entity.hasCustomName() -> entity.customNameTag
            else -> entity.displayName.unformattedText
        }

    private fun prepareEntityForRender(entity: EntityLivingBase, capturedMob: CapturedMob, context: ModularGuiContext) {
        val renderTime = renderTime(capturedMob, context)
        resetCapturedEntityVisualState(entity)
        entity.setLocationAndAngles(0.0, 0.0, 0.0, 0f, 0f)
        val bodyRot = (uuidFloat(capturedMob.id, 0) - 0.5f) * BODY_YAW_RANGE
        val headOffset = -HEAD_STATIC_YAW_RANGE / 2f + uuidFloat(capturedMob.id, 1) * HEAD_STATIC_YAW_RANGE +
                idlePoseOffset(capturedMob.id, renderTime, 0, HEAD_IDLE_YAW_AMPLITUDE)
        val pitch = -HEAD_STATIC_PITCH_RANGE / 2f + uuidFloat(capturedMob.id, 2) * HEAD_STATIC_PITCH_RANGE +
                idlePoseOffset(capturedMob.id, renderTime, 1, HEAD_IDLE_PITCH_AMPLITUDE)
        entity.ticksExisted = renderTime.toInt()
        entity.renderYawOffset = bodyRot
        entity.prevRenderYawOffset = bodyRot
        entity.rotationYaw = bodyRot
        entity.prevRotationYaw = bodyRot
        entity.rotationYawHead = bodyRot + headOffset
        entity.prevRotationYawHead = entity.rotationYawHead
        entity.rotationPitch = pitch
        entity.prevRotationPitch = pitch
    }

    private fun resetCapturedEntityVisualState(entity: EntityLivingBase) {
        entity.hurtResistantTime = 0
        entity.hurtTime = 0
        entity.maxHurtTime = 0
        entity.deathTime = 0
        entity.attackedAtYaw = 0f
        entity.limbSwing = 0f
        entity.limbSwingAmount = 0f
        entity.prevLimbSwingAmount = 0f
    }

    private fun renderTime(capturedMob: CapturedMob, context: ModularGuiContext): Float {
        val player = Minecraft.getMinecraft().player
        val baseTime = player?.ticksExisted?.toFloat() ?: context.tick.toFloat()
        return baseTime + context.partialTicks + uuidFloat(capturedMob.id, 3) * 200f
    }

    private fun idlePoseOffset(uuid: java.util.UUID, renderTime: Float, salt: Int, amplitude: Float): Float {
        val cycleLength = HEAD_IDLE_MIN_CYCLE_TICKS + uuidFloat(uuid, salt + 4) * HEAD_IDLE_CYCLE_VARIATION_TICKS
        val cycle = floor(renderTime / cycleLength).toInt()
        val phase = renderTime - cycle * cycleLength
        val target = (cycleFloat(uuid, cycle, salt) - 0.5f) * 2f * amplitude
        return when {
            phase < HEAD_IDLE_MOVE_TICKS -> smoothStep(phase / HEAD_IDLE_MOVE_TICKS) * target
            phase < HEAD_IDLE_MOVE_TICKS + HEAD_IDLE_HOLD_TICKS -> target
            phase < HEAD_IDLE_MOVE_TICKS * 2f + HEAD_IDLE_HOLD_TICKS ->
                (1f - smoothStep((phase - HEAD_IDLE_MOVE_TICKS - HEAD_IDLE_HOLD_TICKS) / HEAD_IDLE_MOVE_TICKS)) * target
            else -> 0f
        }
    }

    private fun smoothStep(value: Float): Float =
        value * value * (3f - 2f * value)

    private fun cycleFloat(uuid: java.util.UUID, cycle: Int, salt: Int): Float =
        Math.floorMod(uuid.hashCode() * 31 + cycle * 131 + salt * 17, 1000) / 999f

    private fun uuidFloat(uuid: java.util.UUID, salt: Int): Float =
        Math.floorMod(uuid.hashCode() + salt * 31, 1000) / 999f

    private fun getRenderScale(entity: EntityLivingBase, width: Int, height: Int): Int {
        val entityWidth = max(entity.width, 0.25f)
        val entityHeight = max(entity.height, 0.25f)
        return max(8, (min((width - 6) / entityWidth, (height - 6) / entityHeight) * 0.5625f).toInt())
    }

    private fun getRenderBottomY(entity: EntityLivingBase, y: Int, height: Int, scale: Int): Float =
        y + height / 2f + entity.height * scale / 2f

    private data class EntityRenderState(
        val renderYawOffset: Float,
        val prevRenderYawOffset: Float,
        val rotationYaw: Float,
        val prevRotationYaw: Float,
        val rotationYawHead: Float,
        val prevRotationYawHead: Float,
        val rotationPitch: Float,
        val prevRotationPitch: Float,
        val ticksExisted: Int,
        val hurtResistantTime: Int,
        val hurtTime: Int,
        val maxHurtTime: Int,
        val deathTime: Int,
        val attackedAtYaw: Float,
        val limbSwing: Float,
        val limbSwingAmount: Float,
        val prevLimbSwingAmount: Float
    ) {
        fun restore(entity: EntityLivingBase) {
            entity.renderYawOffset = renderYawOffset
            entity.prevRenderYawOffset = prevRenderYawOffset
            entity.rotationYaw = rotationYaw
            entity.prevRotationYaw = prevRotationYaw
            entity.rotationYawHead = rotationYawHead
            entity.prevRotationYawHead = prevRotationYawHead
            entity.rotationPitch = rotationPitch
            entity.prevRotationPitch = prevRotationPitch
            entity.ticksExisted = ticksExisted
            entity.hurtResistantTime = hurtResistantTime
            entity.hurtTime = hurtTime
            entity.maxHurtTime = maxHurtTime
            entity.deathTime = deathTime
            entity.attackedAtYaw = attackedAtYaw
            entity.limbSwing = limbSwing
            entity.limbSwingAmount = limbSwingAmount
            entity.prevLimbSwingAmount = prevLimbSwingAmount
        }

        companion object {
            fun capture(entity: EntityLivingBase): EntityRenderState =
                EntityRenderState(
                    entity.renderYawOffset,
                    entity.prevRenderYawOffset,
                    entity.rotationYaw,
                    entity.prevRotationYaw,
                    entity.rotationYawHead,
                    entity.prevRotationYawHead,
                    entity.rotationPitch,
                    entity.prevRotationPitch,
                    entity.ticksExisted,
                    entity.hurtResistantTime,
                    entity.hurtTime,
                    entity.maxHurtTime,
                    entity.deathTime,
                    entity.attackedAtYaw,
                    entity.limbSwing,
                    entity.limbSwingAmount,
                    entity.prevLimbSwingAmount
                )
        }
    }

    private data class CapturedMobRenderInfo(
        val capturedMob: CapturedMob,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
}
