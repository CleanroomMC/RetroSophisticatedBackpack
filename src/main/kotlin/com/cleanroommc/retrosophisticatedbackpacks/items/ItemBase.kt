package com.cleanroommc.retrosophisticatedbackpacks.items

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.utils.IModelRegister
import net.minecraft.item.Item

open class ItemBase : Item(), IModelRegister {
    override fun registerModels() {
        RetroSophisticatedBackpacks.proxy.registerItemRenderer(this, 0, "inventory")
    }
}