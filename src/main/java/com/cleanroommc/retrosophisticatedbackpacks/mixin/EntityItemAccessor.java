package com.cleanroommc.retrosophisticatedbackpacks.mixin;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityItem.class)
public interface EntityItemAccessor {
    @Accessor("pickupDelay")
    int rsb$getPickupDelay();

    @Accessor("age")
    void rsb$setAge(int age);

}
