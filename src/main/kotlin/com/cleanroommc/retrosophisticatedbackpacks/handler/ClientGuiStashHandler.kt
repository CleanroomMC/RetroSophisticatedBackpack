package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackStashHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackStashHelper.Result
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.slot.ModularBackpackSlot
import com.cleanroommc.retrosophisticatedbackpacks.mixin.GuiContainerAccessor
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SStashToBackpackPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiContainerCreative
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.GuiContainerEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = [Side.CLIENT])
object ClientGuiStashHandler {
    private const val MATCH_AND_SPACE_COLOR = 0x55FF55
    private const val SPACE_COLOR = 0xFFFF55

    @SubscribeEvent
    @JvmStatic
    fun onDrawForeground(event: GuiContainerEvent.DrawForeground) {
        val screen = event.guiContainer
        if (screen is GuiContainerCreative) {
            return
        }

        val player = Minecraft.getMinecraft().player ?: return
        val carried = player.inventory.itemStack
        if (carried.isEmpty) {
            return
        }

        for (slot in screen.inventorySlots.inventorySlots) {
            if (slot is ModularBackpackSlot || !slot.isEnabled || slot.xPos < -100 || slot.yPos < -100) {
                continue
            }
            drawStashSign(player, slot, carried)
        }
    }

    @JvmStatic
    fun handleMouseClicked(screen: GuiContainer, mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (screen is GuiContainerCreative || mouseButton != 1) {
            return false
        }

        val player = Minecraft.getMinecraft().player ?: return false
        val carried = player.inventory.itemStack
        if (carried.isEmpty) {
            return false
        }

        val slot = (screen as GuiContainerAccessor).`rsb$getSlotAtPosition`(mouseX, mouseY) ?: return false
        val action = getStashAction(player, slot, carried) ?: return false
        NetworkHandler.INSTANCE.sendToServer(C2SStashToBackpackPacket(slot.slotNumber, action))
        return true
    }

    @JvmStatic
    fun getStashActionForSlot(player: EntityPlayer, slot: Slot, carried: ItemStack): C2SStashToBackpackPacket.Action? =
        getStashAction(player, slot, carried)

    @JvmStatic
    fun getStashResultForSlot(player: EntityPlayer, slot: Slot, carried: ItemStack): Pair<String, Result>? {
        val slotStack = slot.stack
        if (!slotStack.isEmpty && slotStack.count == 1 && slot.canTakeStack(player)) {
            val result = BackpackStashHelper.getStashResult(slotStack, carried)
            if (result != Result.NO_SPACE) {
                return "+" to result
            }
        }

        if (!slotStack.isEmpty && carried.count == 1 && slot.canTakeStack(player) &&
            carried.getCapability(Capabilities.BACKPACK_CAPABILITY, null) != null
        ) {
            val result = BackpackStashHelper.getStashResult(carried, slotStack)
            if (result != Result.NO_SPACE) {
                return "-" to result
            }
        }

        return null
    }

    private fun drawStashSign(
        player: EntityPlayer,
        slot: Slot,
        carried: ItemStack
    ) {
        val slotStack = slot.stack
        val plusResult = if (!slotStack.isEmpty && slotStack.count == 1 && slot.canTakeStack(player)) {
            BackpackStashHelper.getStashResult(slotStack, carried)
        } else {
            Result.NO_SPACE
        }

        if (plusResult != Result.NO_SPACE) {
            drawStashText(
                "+",
                (slot.xPos + 10).toFloat(),
                (slot.yPos + 8).toFloat(),
                color(plusResult)
            )
            return
        }

        if (slotStack.isEmpty || !slot.canTakeStack(player) || carried.count != 1 ||
            carried.getCapability(Capabilities.BACKPACK_CAPABILITY, null) == null
        ) {
            return
        }

        val minusResult = BackpackStashHelper.getStashResult(carried, slotStack)
        if (minusResult == Result.NO_SPACE) {
            return
        }

        drawStashText(
            "-",
            (slot.xPos + 1).toFloat(),
            slot.yPos.toFloat(),
            color(minusResult)
        )
    }

    private fun drawStashText(text: String, x: Float, y: Float, color: Int) {
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color)
        GlStateManager.enableDepth()
        RenderHelper.enableGUIStandardItemLighting()
    }

    private fun getStashAction(
        player: EntityPlayer,
        slot: Slot,
        carried: ItemStack
    ): C2SStashToBackpackPacket.Action? {
        val slotStack = slot.stack
        if (!slotStack.isEmpty && slotStack.count == 1 && slot.canTakeStack(player) &&
            BackpackStashHelper.getStashResult(slotStack, carried) != Result.NO_SPACE
        ) {
            return C2SStashToBackpackPacket.Action.CARRIED_TO_SLOT_BACKPACK
        }

        if (!slotStack.isEmpty && carried.count == 1 && slot.canTakeStack(player) &&
            carried.getCapability(Capabilities.BACKPACK_CAPABILITY, null) != null &&
            BackpackStashHelper.getStashResult(carried, slotStack) != Result.NO_SPACE
        ) {
            return C2SStashToBackpackPacket.Action.SLOT_TO_CARRIED_BACKPACK
        }

        return null
    }

    @JvmStatic
    fun color(result: Result): Int =
        if (result == Result.MATCH_AND_SPACE) MATCH_AND_SPACE_COLOR else SPACE_COLOR
}
