package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.modularui.drawable.GuiDraw
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackEnergyStorage
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackFluidHandler
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.client.event.RenderTooltipEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = [Side.CLIENT])
object BackpackTooltipHandler {
    private const val MAX_STACKS_ON_LINE = 9
    private const val STACK_WIDTH = 18
    private const val GAUGE_SCALE = 0.65f
    private const val GAUGE_RENDER_HEIGHT = 10
    private const val GAUGE_TEXTURE_WIDTH = 54
    private const val GAUGE_TEXTURE_HEIGHT = 18
    private const val GAUGE_TEXT_LEFT = 41
    private const val ROW_HEIGHT = 20
    private const val TITLE_HEIGHT = 10
    private const val COUNT_PADDING = 2
    private const val CHARGE_SEGMENT_HEIGHT = 6
    private const val GUI_CONTROLS_SIZE = 256f
    private const val GUI_CONTROLS = Tags.MOD_ID + ":textures/gui/gui_controls.png"
    private const val TANK_MARKER = "\u00A70\u00A71\u00A72\u00A73"
    private const val BATTERY_MARKER = "\u00A70\u00A71\u00A72\u00A74"
    private const val UPGRADES_MARKER = "\u00A70\u00A71\u00A72\u00A75"
    private const val ITEMS_MARKER = "\u00A70\u00A71\u00A72\u00A76"
    private const val TOP_BAR_COLOR = 0xFF1A1A
    private const val BOTTOM_BAR_COLOR = 0xFFFF40
    private val GUI_CONTROLS_LOCATION = ResourceLocation(GUI_CONTROLS)
    private val TWO_DIGIT_FORMAT = DecimalFormat("#.00", DecimalFormatSymbols(Locale.ROOT))
    private val ONE_DIGIT_FORMAT = DecimalFormat("##.0", DecimalFormatSymbols(Locale.ROOT))
    private val SUFFIXES = arrayOf("k", "m", "b")

    fun shouldShowContentsTooltip(): Boolean {
        val player = Minecraft.getMinecraft().player
        return GuiScreen.isShiftKeyDown() || player != null && !player.inventory.itemStack.isEmpty
    }

    fun addTooltipLines(wrapper: BackpackWrapper, tooltip: MutableList<String>) {
        val data = TooltipData.of(wrapper)
        data.summaryLines().forEach(tooltip::add)
        data.blocks.forEach { block ->
            repeat(block.placeholderRows) { row ->
                tooltip.add(if (row == 0) block.marker else block.placeholderLine(data.width))
            }
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onTooltipPostText(event: RenderTooltipEvent.PostText) {
        if (event.stack.item !is BackpackItem || !shouldShowContentsTooltip()) {
            return
        }
        val wrapper = event.stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return
        val data = TooltipData.of(wrapper)
        data.blocks.forEach { block ->
            val lineIndex = event.lines.indexOfFirst { it.contains(block.marker) }
            if (lineIndex >= 0) {
                block.render(data, event.x, lineTop(event.y, lineIndex))
            }
        }
    }

    private fun lineTop(tooltipY: Int, lineIndex: Int): Int =
        tooltipY + if (lineIndex == 0) 0 else 12 + (lineIndex - 1) * 10

    private fun renderGaugeTitle(title: String, leftX: Int, topY: Int) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(
            TextFormatting.YELLOW.toString() + title,
            leftX.toFloat(),
            topY.toFloat(),
            -1
        )
    }

    private inline fun renderRotatedGauge(leftX: Int, topY: Int, render: () -> Unit) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(leftX.toFloat(), topY + GAUGE_TEXTURE_HEIGHT * GAUGE_SCALE, 0f)
        GlStateManager.scale(GAUGE_SCALE, GAUGE_SCALE, 1f)
        GlStateManager.rotate(-90f, 0f, 0f, 1f)
        render()
        GlStateManager.popMatrix()
    }

    private fun renderVerticalGaugeBackground() {
        val mc = Minecraft.getMinecraft()
        mc.textureManager.bindTexture(GUI_CONTROLS_LOCATION)
        GlStateManager.color(1f, 1f, 1f, 1f)
        drawTexturedRect(0, 0, 29, 30, 18, 18)
        drawTexturedRect(0, 18, 29, 48, 18, 18)
        drawTexturedRect(0, 36, 29, 66, 18, 18)
    }

    private fun renderVerticalGaugeOverlay(overlayU: Int, overlayV: Int) {
        val mc = Minecraft.getMinecraft()
        mc.textureManager.bindTexture(GUI_CONTROLS_LOCATION)
        GlStateManager.color(1f, 1f, 1f, 1f)
        repeat(GAUGE_TEXTURE_WIDTH / 18) {
            drawTexturedRect(1, it * 18, overlayU, overlayV, 16, 18)
        }
    }

    private fun renderTank(data: TooltipData, leftX: Int, topY: Int) {
        val mc = Minecraft.getMinecraft()
        val title = I18n.format("tooltip.backpack.fluid_title".asTranslationKey())
        renderGaugeTitle(title, leftX, topY)
        data.tanks.forEachIndexed { index, tank ->
            val gaugeY = topY + TITLE_HEIGHT + index * (GAUGE_RENDER_HEIGHT + 2)
            val fluid = tank.fluid
            renderRotatedGauge(leftX, gaugeY) {
                renderVerticalGaugeBackground()
                if (fluid != null && fluid.amount > 0) {
                    val displayLevel = ((GAUGE_TEXTURE_WIDTH - 2) * (fluid.amount.toFloat() / tank.capacity.coerceAtLeast(1))).toInt()
                        .coerceIn(1, GAUGE_TEXTURE_WIDTH - 2)
                    GuiDraw.drawFluidTexture(
                        fluid,
                        1f,
                        1f,
                        16f,
                        displayLevel.toFloat(),
                        300f
                    )
                }
                renderVerticalGaugeOverlay(47, 30)
            }
            mc.fontRenderer.drawStringWithShadow(
                if (fluid == null || fluid.amount <= 0) {
                    TextFormatting.BLUE.toString() + I18n.format("tooltip.backpack.fluid_empty".asTranslationKey())
                } else {
                    TextFormatting.BLUE.toString() + TextComponentTranslation(
                        "tooltip.backpack.fluid".asTranslationKey(),
                        TextFormatting.WHITE.toString() + abbreviate(fluid.amount),
                        TextFormatting.BLUE.toString() + fluid.localizedName
                    ).formattedText
                },
                (leftX + GAUGE_TEXT_LEFT).toFloat(),
                (gaugeY + 2).toFloat(),
                -1
            )
        }
    }

    private fun renderBattery(data: TooltipData, leftX: Int, topY: Int) {
        val mc = Minecraft.getMinecraft()
        renderGaugeTitle(I18n.format("tooltip.backpack.energy_title".asTranslationKey()), leftX, topY)
        val gaugeY = topY + TITLE_HEIGHT
        renderRotatedGauge(leftX, gaugeY) {
            renderVerticalGaugeBackground()
            renderVerticalGaugeOverlay(47, 56)
            renderVerticalBatteryCharge(data.energyStored, data.energyCapacity)
            mc.textureManager.bindTexture(GUI_CONTROLS_LOCATION)
            GlStateManager.color(1f, 1f, 1f, 1f)
            drawTexturedRect(1, 0, 47, 48, 16, 4)
            drawTexturedRect(1, GAUGE_TEXTURE_WIDTH - 4, 47, 52, 16, 4)
        }
        mc.fontRenderer.drawStringWithShadow(
            TextFormatting.RED.toString() + TextComponentTranslation(
                "tooltip.backpack.energy".asTranslationKey(),
                TextFormatting.WHITE.toString() + abbreviate(data.energyStored)
            ).formattedText,
            (leftX + GAUGE_TEXT_LEFT).toFloat(),
            (gaugeY + 2).toFloat(),
            -1
        )
    }

    private fun renderContentsTitle(title: String, leftX: Int, topY: Int) {
        val mc = Minecraft.getMinecraft()
        mc.fontRenderer.drawStringWithShadow(TextFormatting.YELLOW.toString() + title, leftX.toFloat(), topY.toFloat(), -1)
    }

    private fun renderUpgradesBlock(data: TooltipData, leftX: Int, topY: Int) {
        renderContentsTitle(I18n.format("tooltip.backpack.upgrades_title".asTranslationKey()), leftX, topY)
        renderUpgrades(data.upgrades, leftX, topY + TITLE_HEIGHT)
    }

    private fun renderItemsBlock(data: TooltipData, leftX: Int, topY: Int) {
        renderContentsTitle(I18n.format("tooltip.backpack.inventory_title".asTranslationKey()), leftX, topY)
        renderItems(data.items, leftX, topY + TITLE_HEIGHT)
    }

    private fun renderUpgrades(stacks: List<ItemStack>, leftX: Int, topY: Int) {
        val mc = Minecraft.getMinecraft()
        withItemLighting {
            stacks.forEachIndexed { index, stack ->
                val x = leftX + index * STACK_WIDTH
                mc.renderItem.renderItemAndEffectIntoGUI(stack, x, topY)
            }
        }
    }

    private fun renderItems(stacks: List<ItemStack>, leftX: Int, topY: Int) {
        val mc = Minecraft.getMinecraft()
        withItemLighting {
            var x = leftX
            stacks.forEachIndexed { index, stack ->
                if (index % MAX_STACKS_ON_LINE == 0) {
                    x = leftX
                }
                val y = topY + index / MAX_STACKS_ON_LINE * ROW_HEIGHT
                val count = abbreviate(stack.count)
                val width = maxOf(mc.fontRenderer.getStringWidth(count) + COUNT_PADDING, STACK_WIDTH)
                val offset = width - STACK_WIDTH
                mc.renderItem.renderItemAndEffectIntoGUI(stack, x + offset, y)
                mc.renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x + offset, y, count)
                x += width
            }
        }
    }

    private inline fun withItemLighting(render: () -> Unit) {
        val mc = Minecraft.getMinecraft()
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.enableDepth()
        GlStateManager.enableRescaleNormal()
        mc.renderItem.zLevel = 300f
        render()
        mc.renderItem.zLevel = 0f
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
    }

    private fun renderVerticalBatteryCharge(energyStored: Int, maxEnergyStored: Int) {
        if (maxEnergyStored <= 0 || energyStored <= 0) {
            return
        }
        val mc = Minecraft.getMinecraft()
        mc.textureManager.bindTexture(GUI_CONTROLS_LOCATION)
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        val numberOfSegments = GAUGE_TEXTURE_WIDTH / CHARGE_SEGMENT_HEIGHT
        val displayLevel = (numberOfSegments * (energyStored.toFloat() / maxEnergyStored)).toInt()
        for (segmentIndex in 0 until displayLevel) {
            val percentage = if (numberOfSegments <= 1) 0f else segmentIndex.toFloat() / (numberOfSegments - 1)
            GlStateManager.color(
                colorChannel(BOTTOM_BAR_COLOR, 16, percentage, TOP_BAR_COLOR) / 255f,
                colorChannel(BOTTOM_BAR_COLOR, 8, percentage, TOP_BAR_COLOR) / 255f,
                colorChannel(BOTTOM_BAR_COLOR, 0, percentage, TOP_BAR_COLOR) / 255f,
                1f
            )
            drawTexturedRect(1, GAUGE_TEXTURE_WIDTH - (segmentIndex + 1) * CHARGE_SEGMENT_HEIGHT, 47, 74, 16, CHARGE_SEGMENT_HEIGHT)
        }
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun colorChannel(bottom: Int, shift: Int, percentage: Float, top: Int): Int {
        val bottomChannel = bottom shr shift and 255
        val topChannel = top shr shift and 255
        return (bottomChannel * (1 - percentage) + topChannel * percentage).toInt()
    }

    private fun drawTexturedRect(x: Int, y: Int, u: Int, v: Int, width: Int, height: Int) {
        val minU = u / GUI_CONTROLS_SIZE
        val maxU = (u + width) / GUI_CONTROLS_SIZE
        val minV = v / GUI_CONTROLS_SIZE
        val maxV = (v + height) / GUI_CONTROLS_SIZE
        drawSprite(x, y, width, height, minU, maxU, minV, maxV)
    }

    private fun drawSprite(x: Int, y: Int, width: Int, height: Int, minU: Float, maxU: Float, minV: Float, maxV: Float) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX)
        buffer.pos(x.toDouble(), (y + height).toDouble(), 300.0).tex(minU.toDouble(), maxV.toDouble()).endVertex()
        buffer.pos((x + width).toDouble(), (y + height).toDouble(), 300.0).tex(maxU.toDouble(), maxV.toDouble()).endVertex()
        buffer.pos((x + width).toDouble(), y.toDouble(), 300.0).tex(maxU.toDouble(), minV.toDouble()).endVertex()
        buffer.pos(x.toDouble(), y.toDouble(), 300.0).tex(minU.toDouble(), minV.toDouble()).endVertex()
        tessellator.draw()
    }

    private data class TooltipData(
        val items: List<ItemStack>,
        val upgrades: List<ItemStack>,
        val summary: List<String>,
        val tanks: List<TankTooltipInfo>,
        val energyStored: Int,
        val energyCapacity: Int
    ) {
        val blocks: List<TooltipBlock> =
            buildList {
                if (tanks.isNotEmpty()) add(TooltipBlock.Tank(tanks.size))
                if (energyCapacity > 0) add(TooltipBlock.Battery)
                if (upgrades.isNotEmpty()) add(TooltipBlock.Upgrades)
                if (items.isNotEmpty()) add(TooltipBlock.Items(items.size))
            }

        fun summaryLines(): List<String> =
            if (summary.isEmpty() && blocks.isEmpty()) {
                listOf(TextFormatting.YELLOW.toString() + I18n.format("tooltip.backpack.empty".asTranslationKey()))
            } else summary

        val width: Int by lazy {
            val mc = Minecraft.getMinecraft()
            val lineWidth = summaryLines().maxOfOrNull(mc.fontRenderer::getStringWidth) ?: 0
            maxOf(lineWidth, stackWidth(upgrades), stackWidth(items), gaugeWidth())
        }

        private fun stackWidth(stacks: List<ItemStack>): Int =
            stacks.take(MAX_STACKS_ON_LINE).sumOf { stack ->
                maxOf(Minecraft.getMinecraft().fontRenderer.getStringWidth(abbreviate(stack.count)) + COUNT_PADDING, STACK_WIDTH)
            }

        private fun gaugeWidth(): Int {
            val mc = Minecraft.getMinecraft()
            val tankTextWidth = tanks.maxOfOrNull {
                val fluid = it.fluid
                if (fluid == null || fluid.amount <= 0) mc.fontRenderer.getStringWidth(I18n.format("tooltip.backpack.fluid_empty".asTranslationKey()))
                else mc.fontRenderer.getStringWidth(I18n.format("tooltip.backpack.fluid".asTranslationKey(), abbreviate(fluid.amount), fluid.localizedName))
            } ?: 0
            val energyTextWidth = if (energyCapacity > 0) mc.fontRenderer.getStringWidth(I18n.format("tooltip.backpack.energy".asTranslationKey(), abbreviate(energyStored))) else 0
            return GAUGE_TEXT_LEFT + maxOf(tankTextWidth, energyTextWidth, 0)
        }

        companion object {
            fun of(wrapper: BackpackWrapper): TooltipData {
                val items = compactItems(wrapper.backpackItemStackHandler.inventory.filterNot(ItemStack::isEmpty))
                val upgrades = wrapper.upgradeItemStackHandler.inventory.filterNot(ItemStack::isEmpty).map(ItemStack::copy)
                val summary = mutableListOf<String>()
                addMultiplierTooltip(wrapper, summary)
                val tanks = tanks(wrapper)
                val energy = BackpackEnergyStorage(wrapper)
                val energyCapacity = if (wrapper.hasBatteryUpgrade()) energy.maxEnergyStored else 0
                return TooltipData(items, upgrades, summary, tanks, energy.energyStored, energyCapacity)
            }

            private fun compactItems(stacks: List<ItemStack>): List<ItemStack> =
                stacks.fold(mutableListOf<ItemStack>()) { compacted, stack ->
                    val matching = compacted.firstOrNull { ItemStack.areItemsEqual(it, stack) && ItemStack.areItemStackTagsEqual(it, stack) }
                    if (matching == null) {
                        compacted.add(stack.copy())
                    } else {
                        matching.count += stack.count
                    }
                    compacted
                }.sortedByDescending(ItemStack::getCount)

            private fun addMultiplierTooltip(wrapper: BackpackWrapper, tooltip: MutableList<String>) {
                val multiplier = wrapper.getTotalStackMultiplier()
                if (multiplier > 1) {
                    tooltip.add(
                        TextFormatting.GREEN.toString() + TextComponentTranslation(
                            "tooltip.backpack.stack_multiplier".asTranslationKey(),
                            TextFormatting.WHITE.toString() + multiplier
                        ).formattedText
                    )
                }
            }

            private fun tanks(wrapper: BackpackWrapper): List<TankTooltipInfo> =
                if (!wrapper.hasTankUpgrade()) emptyList()
                else BackpackFluidHandler(wrapper).tankProperties.map { TankTooltipInfo(it.contents?.copy(), it.capacity) }
        }
    }

    private data class TankTooltipInfo(val fluid: FluidStack?, val capacity: Int)

    private sealed class TooltipBlock(
        val marker: String,
        private val height: Int,
        private val renderer: (TooltipData, Int, Int) -> Unit
    ) {
        val placeholderRows: Int = (height + 9) / 10

        fun placeholderLine(width: Int): String =
            " ".repeat((width + 3) / 4)

        fun render(data: TooltipData, x: Int, y: Int) {
            renderer(data, x, y)
        }

        class Tank(tankCount: Int) : TooltipBlock(TANK_MARKER, TITLE_HEIGHT + tankCount * (GAUGE_RENDER_HEIGHT + 2) + 2, ::renderTank)
        object Battery : TooltipBlock(BATTERY_MARKER, TITLE_HEIGHT + GAUGE_RENDER_HEIGHT + 2, ::renderBattery)
        object Upgrades : TooltipBlock(UPGRADES_MARKER, TITLE_HEIGHT + ROW_HEIGHT + 2, ::renderUpgradesBlock)
        class Items(itemCount: Int) : TooltipBlock(ITEMS_MARKER, TITLE_HEIGHT + ((itemCount + MAX_STACKS_ON_LINE - 1) / MAX_STACKS_ON_LINE) * ROW_HEIGHT + 2, ::renderItemsBlock)
    }

    private fun abbreviate(count: Int, maxCharacters: Int = 4): String {
        val digits = count.toString().length
        if (digits <= maxCharacters) {
            return String.format(Locale.ROOT, "%,d", count)
        }
        val thousandsExponent = (digits - maxCharacters) / 3 + 1
        val suffix = SUFFIXES.getOrElse(thousandsExponent - 1) { "b" }
        val divisionResult = count / Math.pow(1000.0, thousandsExponent.toDouble())
        val wholeDigits = digits - thousandsExponent * 3
        val precisionDigits = maxCharacters - 1 - wholeDigits
        val numberPart = when {
            wholeDigits > 3 || precisionDigits == 0 -> String.format(Locale.ROOT, "%,d", divisionResult.toInt())
            precisionDigits == 2 -> TWO_DIGIT_FORMAT.format(divisionResult)
            precisionDigits == 1 -> ONE_DIGIT_FORMAT.format(divisionResult)
            else -> divisionResult.toInt().toString()
        }
        return numberPart + suffix
    }
}
