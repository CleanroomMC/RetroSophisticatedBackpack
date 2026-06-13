package com.cleanroommc.retrosophisticatedbackpacks.handler

import baubles.api.BaublesApi
import baubles.common.container.SlotBauble
import com.cleanroommc.modularui.screen.ClientScreenHandler
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedRefillUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SOpenBackpackPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SRefillBlockPickPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SToolSwapBlockPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SToolSwapEntityPacket
import com.cleanroommc.retrosophisticatedbackpacks.proxy.RSBProxy
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.items.IItemHandler
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

        if (RSBProxy.ClientProxy.TOOL_SWAP_KEYBIND.isPressed) {
            sendToolSwapPacket(mc)
        }

        if (mc.gameSettings.keyBindPickBlock.isPressed) {
            sendRefillBlockPickPacket(mc)
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onMouseInput(event: InputEvent.MouseInputEvent) {
        val mc = Minecraft.getMinecraft()
        if (RSBProxy.ClientProxy.TOOL_SWAP_KEYBIND.isPressed) {
            sendToolSwapPacket(mc)
        }
        if (mc.gameSettings.keyBindPickBlock.isPressed) {
            sendRefillBlockPickPacket(mc)
        }
    }

    private fun sendToolSwapPacket(mc: Minecraft) {
        val player = mc.player ?: return
        val target = mc.objectMouseOver ?: return
        if (player.heldItemMainhand.item is BackpackItem) {
            player.sendStatusMessage(TextComponentTranslation("gui.status.unable_to_swap_tool_for_backpack".asTranslationKey()), true)
            return
        }

        when (target.typeOfHit) {
            RayTraceResult.Type.BLOCK -> NetworkHandler.INSTANCE.sendToServer(C2SToolSwapBlockPacket(target.blockPos))
            RayTraceResult.Type.ENTITY -> target.entityHit?.let { NetworkHandler.INSTANCE.sendToServer(C2SToolSwapEntityPacket(it.entityId)) }
            else -> Unit
        }
    }

    private fun sendRefillBlockPickPacket(mc: Minecraft) {
        val player = mc.player ?: return
        val target = mc.objectMouseOver ?: return
        if (player.isCreative || target.typeOfHit != RayTraceResult.Type.BLOCK || !hasAdvancedRefillBackpack(player.inventory) &&
            (!RetroSophisticatedBackpacks.baublesLoaded || !hasAdvancedRefillBackpack(BaublesApi.getBaublesHandler(player)))) {
            return
        }

        val pos = target.blockPos
        val state = mc.world.getBlockState(pos)
        if (state.block.isAir(state, mc.world, pos)) {
            return
        }

        val pickedStack = state.block.getPickBlock(state, target, mc.world, pos, player)
        if (!pickedStack.isEmpty && !hasMatchingStack(player.inventory, pickedStack)) {
            NetworkHandler.INSTANCE.sendToServer(C2SRefillBlockPickPacket(pickedStack))
        }
    }

    private fun hasAdvancedRefillBackpack(inventory: IInventory): Boolean {
        for (slot in 0 until inventory.sizeInventory) {
            val wrapper = inventory.getStackInSlot(slot).getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: continue
            if (wrapper.gatherCapabilityUpgrades(Capabilities.ADVANCED_REFILL_UPGRADE_CAPABILITY)
                    .filterIsInstance<AdvancedRefillUpgradeWrapper>()
                    .any { it.enabled }
            ) {
                return true
            }
        }
        return false
    }

    private fun hasMatchingStack(inventory: IInventory, stack: ItemStack): Boolean {
        for (slot in 0 until inventory.sizeInventory) {
            if (net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack(inventory.getStackInSlot(slot), stack)) {
                return true
            }
        }
        return false
    }

    private fun hasAdvancedRefillBackpack(inventory: IItemHandler): Boolean {
        for (slot in 0 until inventory.slots) {
            val wrapper = inventory.getStackInSlot(slot).getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: continue
            if (wrapper.gatherCapabilityUpgrades(Capabilities.ADVANCED_REFILL_UPGRADE_CAPABILITY)
                    .filterIsInstance<AdvancedRefillUpgradeWrapper>()
                    .any { it.enabled }
            ) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun onKeyInputInGuiScreen(keyCode: Int) {
        val mc = Minecraft.getMinecraft()
        val screen = mc.currentScreen
        val muiScreen = ClientScreenHandler.getMuiScreen()

        if (RSBProxy.ClientProxy.OPEN_BACKPACK_KEYBIND.keyCode == keyCode && screen is GuiContainer) {
            val hoveredSlot = screen.slotUnderMouse
            val stack = hoveredSlot?.stack ?: ItemStack.EMPTY
            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)

            if (stack.isEmpty && muiScreen != null && muiScreen.name == "backpack_gui") {
                muiScreen.close()
                return
            } else if (hoveredSlot == null)
                return

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
