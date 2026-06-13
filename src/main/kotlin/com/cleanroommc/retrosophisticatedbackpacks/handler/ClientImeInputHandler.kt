package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.modularui.api.IMuiScreen
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.client.gui.widgets.VanillaTextFieldWidget
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.input.Keyboard

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = [Side.CLIENT])
object ClientImeInputHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    @JvmStatic
    fun onKeyboardInput(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (event.gui !is IMuiScreen || Keyboard.getEventKey() != 0 || Keyboard.getEventKeyState()) {
            return
        }

        val character = Keyboard.getEventCharacter()
        if (character >= ' ' && VanillaTextFieldWidget.handleCommittedCharacter(character)) {
            event.isCanceled = true
        }
    }
}
