package com.cleanroommc.retrosophisticatedbackpacks.handlers

import baubles.api.BaublesApi
import baubles.common.container.SlotBauble
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.inventory.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SOpenBackpackPacket
import com.cleanroommc.retrosophisticatedbackpacks.proxy.RSBProxy
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = [Side.CLIENT])
object KeyInputHandler {
    @SubscribeEvent
    @JvmStatic
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        val mc = Minecraft.getMinecraft()
        val player = mc.player

        if (RSBProxy.ClientProxy.OPEN_BACKPACK_KEYBIND.isPressed) {
            // Look for first encountered backpack item and send packet to server side to open it

            if (RetroSophisticatedBackpacks.baublesLoaded) {
                val baubleInv = BaublesApi.getBaublesHandler(player)

                for (slotIndex in 0 until baubleInv.slots) {
                    val stack = baubleInv.getStackInSlot(slotIndex)
                    val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)

                    if (wrapper == null)
                        continue

                    NetworkHandler.INSTANCE.sendToServer(
                        C2SOpenBackpackPacket(
                            PlayerInventoryGuiData.InventoryType.PLAYER_BAUBLES,
                            slotIndex
                        )
                    )
                }
            }

            val playerInv = player.inventory

            for (slotIndex in 0 until playerInv.sizeInventory) {
                val stack = playerInv.getStackInSlot(slotIndex)
                val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)

                if (wrapper == null)
                    continue

                NetworkHandler.INSTANCE.sendToServer(
                    C2SOpenBackpackPacket(
                        PlayerInventoryGuiData.InventoryType.PLAYER_INVENTORY,
                        slotIndex
                    )
                )
            }
        }
    }

    @JvmStatic
    fun onKeyInputInGuiScreen(keyCode: Int) {
        val mc = Minecraft.getMinecraft()
        val screen = mc.currentScreen

        if (RSBProxy.ClientProxy.OPEN_BACKPACK_KEYBIND.keyCode == keyCode && screen is GuiContainer) {
            val hoveredSlot = screen.slotUnderMouse ?: return
            val stack = hoveredSlot.stack
            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)

            if (wrapper == null)
                return

            if (RetroSophisticatedBackpacks.baublesLoaded) {
                if (hoveredSlot is SlotBauble) {
                    NetworkHandler.INSTANCE.sendToServer(
                        C2SOpenBackpackPacket(
                            PlayerInventoryGuiData.InventoryType.PLAYER_BAUBLES,
                            hoveredSlot.slotIndex
                        )
                    )
                    return
                }
            }

            NetworkHandler.INSTANCE.sendToServer(
                C2SOpenBackpackPacket(
                    PlayerInventoryGuiData.InventoryType.PLAYER_INVENTORY,
                    hoveredSlot.slotIndex
                )
            )
        }
    }
}