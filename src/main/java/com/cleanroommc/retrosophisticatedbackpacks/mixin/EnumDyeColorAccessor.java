package com.cleanroommc.retrosophisticatedbackpacks.mixin;

import net.minecraft.item.EnumDyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnumDyeColor.class)
public interface EnumDyeColorAccessor {

    @Accessor(value = "colorValue")
    int rsb$getColorValue();

}
