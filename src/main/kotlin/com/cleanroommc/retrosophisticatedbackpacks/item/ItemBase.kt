package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.util.IModelRegister
import net.minecraft.item.Item

open class ItemBase : Item(), IModelRegister {
    override fun registerModels() {
        RetroSophisticatedBackpacks.proxy.registerItemRenderer(this, 0, "inventory")
    }
}