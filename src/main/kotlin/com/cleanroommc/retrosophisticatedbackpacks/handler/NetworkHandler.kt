package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.network.C2SOpenBackpackPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SMobCatcherReleasePacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SRefillBlockPickPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SStashToBackpackPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SToolSwapBlockPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.C2SToolSwapEntityPacket
import com.cleanroommc.retrosophisticatedbackpacks.network.S2CMobCatcherContentsPacket
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.relauncher.Side

object NetworkHandler {
    val INSTANCE: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("rsb")

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
        INSTANCE.registerMessage(
            C2SRefillBlockPickPacket.Handler::class.java,
            C2SRefillBlockPickPacket::class.java,
            idGenerator.next(),
            Side.SERVER
        )
        INSTANCE.registerMessage(
            C2SStashToBackpackPacket.Handler::class.java,
            C2SStashToBackpackPacket::class.java,
            idGenerator.next(),
            Side.SERVER
        )
        INSTANCE.registerMessage(
            C2SToolSwapBlockPacket.Handler::class.java,
            C2SToolSwapBlockPacket::class.java,
            idGenerator.next(),
            Side.SERVER
        )
        INSTANCE.registerMessage(
            C2SToolSwapEntityPacket.Handler::class.java,
            C2SToolSwapEntityPacket::class.java,
            idGenerator.next(),
            Side.SERVER
        )
        INSTANCE.registerMessage(
            C2SMobCatcherReleasePacket.Handler::class.java,
            C2SMobCatcherReleasePacket::class.java,
            idGenerator.next(),
            Side.SERVER
        )
        INSTANCE.registerMessage(
            S2CMobCatcherContentsPacket.Handler::class.java,
            S2CMobCatcherContentsPacket::class.java,
            idGenerator.next(),
            Side.CLIENT
        )
    }
}
