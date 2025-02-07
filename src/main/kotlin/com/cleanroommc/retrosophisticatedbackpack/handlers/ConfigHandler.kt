package com.cleanroommc.retrosophisticatedbackpack.handlers

import com.cleanroommc.retrosophisticatedbackpack.Tags
import net.minecraftforge.common.config.Config
import net.minecraftforge.common.config.ConfigManager
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
class ConfigHandler {
    @SubscribeEvent
    fun onConfigChange(event: ConfigChangedEvent.OnConfigChangedEvent) {
        if (event.modID == Tags.MOD_ID) {
            ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE)
        }
    }
}