package com.cleanroommc.retrosophisticatedbackpacks.mixin;

import com.cleanroommc.retrosophisticatedbackpacks.handler.KeyInputHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen {
    @Inject(method = "keyTyped", at = @At("TAIL"))
    private void keyTyped(char typedChar, int keyCode, CallbackInfo info) {
        KeyInputHandler.onKeyInputInGuiScreen(keyCode);
    }
}
