package com.cleanroommc.retrosophisticatedbackpacks.backpack

import baubles.api.BaublesApi
import baubles.api.inv.BaublesInventoryWrapper
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory

object BackpackFeedingHelper {
    fun attemptFeed(player: EntityPlayer) {
        var result = false

        if (RetroSophisticatedBackpacks.baublesLoaded)
            result = attemptFeed(player, BaublesInventoryWrapper(BaublesApi.getBaublesHandler(player)))

        if (!result)
            attemptFeed(player, player.inventory)
    }

    fun attemptFeed(player: EntityPlayer, searchInventory: IInventory): Boolean {
        val size = searchInventory.sizeInventory

        for (i in 0 until size) {
            val stack = searchInventory.getStackInSlot(i)

            if (stack.isEmpty)
                continue

            val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: continue
            val feedingStack = wrapper.getFeedingStack(player.foodStats.foodLevel, player.health, player.maxHealth)

            if (feedingStack.isEmpty)
                continue

            feedingStack.onItemUseFinish(player.world, player)
            return true
        }

        return false
    }
}