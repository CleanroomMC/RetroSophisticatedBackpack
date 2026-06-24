package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.client.sound.BackpackSoundManager
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@Mod.EventBusSubscriber(Side.CLIENT, modid = Tags.MOD_ID)
object ClientEventHandler {
    @SubscribeEvent
    @JvmStatic
    @SideOnly(Side.CLIENT)
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        BackpackSoundManager.clear()
    }
    
    @SubscribeEvent
    @JvmStatic
    @SideOnly(Side.CLIENT)
    fun onClientWorldUnload(event: WorldEvent.Unload) {
        BackpackSoundManager.clear()
        CapabilityHandler.BACKPACK_INVENTORY_CACHE.clear()
    }
}
