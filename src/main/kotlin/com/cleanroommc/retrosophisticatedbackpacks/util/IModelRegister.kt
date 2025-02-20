package com.cleanroommc.retrosophisticatedbackpacks.util

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import net.minecraft.item.Item

interface IModelRegister {
    fun registerModels()

    interface Block : IModelRegister {
        override fun registerModels() {
            RetroSophisticatedBackpacks.proxy.registerItemRenderer(
                Item.getItemFromBlock(this as net.minecraft.block.Block),
                0,
                "inventory"
            )
        }
    }
}