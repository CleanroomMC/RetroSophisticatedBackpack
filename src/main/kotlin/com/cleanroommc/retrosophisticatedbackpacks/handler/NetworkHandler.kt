package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SOpenBackpackPacket
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side

object NetworkHandler {
    val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID)

    private val idGenerator = iterator {
        var x = 0
        while (true) {
            yield(x)
            x++
        }
    }

    fun register() {
        INSTANCE.registerMessage(
            C2SOpenBackpackPacket.Handler::class.java,
            C2SOpenBackpackPacket::class.java,
            idGenerator.next(),
            Side.SERVER
        )
    }
}