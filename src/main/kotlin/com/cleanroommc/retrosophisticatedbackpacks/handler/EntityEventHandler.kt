package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.SoundEvents
import net.minecraft.util.SoundCategory
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
object EntityEventHandler {
    @SubscribeEvent
    @JvmStatic
    fun onItemPickup(event: EntityItemPickupEvent) {
        val inventory = event.entityPlayer.inventory
        var stack = event.item.item.copy()

        for (i in 0 until inventory.sizeInventory) {
            val backpackStack = inventory.getStackInSlot(i)

            if (backpackStack.item !is BackpackItem)
                continue

            val wrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: continue

            if (!wrapper.canPickupItem(stack))
                continue

            var slotIndex = 0
            while (!stack.isEmpty && slotIndex < inventory.sizeInventory) {
                stack = wrapper.insertStack(slotIndex, stack)

                slotIndex++
            }

            if (stack.isEmpty) {
                event.item.setDead()
                event.isCanceled = true

                val avRandomizer = Random()
                event.item.world.playSound(
                    null,
                    event.item.posX, event.item.posY, event.item.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS, 0.2f,
                    (avRandomizer.nextFloat() - avRandomizer.nextFloat()) * 1.4f + 2.0f
                )
                return
            }
        }

        if (stack.count != event.item.item.count) {
            event.item.setDead()
            event.isCanceled = true

            val world = event.item.world
            val alteredEntityItem = EntityItem(world, event.item.posX, event.item.posY, event.item.posZ, stack)
            alteredEntityItem.setNoPickupDelay()
            world.spawnEntity(alteredEntityItem)
        }
    }
}