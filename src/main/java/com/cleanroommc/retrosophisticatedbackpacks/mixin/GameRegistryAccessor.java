package com.cleanroommc.retrosophisticatedbackpacks.mixin;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRegistry.class)
public interface GameRegistryAccessor {
    @Invoker("register")
    static <K extends IForgeRegistryEntry<K>> K invokeRegister(K item) {
        throw new AssertionError();
    }
}
