package com.cleanroommc.retrosophisticatedbackpacks.handler

import baubles.api.BaublesApi
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackFeedingHelper
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.SoundCategory
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
object EntityEventHandler {
    private var feedTickCounter = 0

    @SubscribeEvent
    @JvmStatic
    fun onItemPickup(event: EntityItemPickupEvent) {
        val player = event.entityPlayer
        val inventory = player.inventory
        var stack = event.item.item.copy()

        stack = attemptPickup(InvWrapper(inventory), stack)

        if (!stack.isEmpty && RetroSophisticatedBackpacks.baublesLoaded) {
            stack = attemptPickup(BaublesApi.getBaublesHandler(player), stack)
        }

        if (stack.isEmpty) {
            event.item.setDead()
            event.isCanceled = true

            event.item.world.playSound(
                null,
                event.item.posX, event.item.posY, event.item.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                SoundCategory.PLAYERS, 0.2f,
                ((player.rng.nextFloat() - player.rng.nextFloat()) * 0.7f + 1.0f) * 2.0f
            )
            return
        } else if (stack.count != event.item.item.count) {
            event.item.setDead()
            event.isCanceled = true

            val world = event.item.world
            val alteredEntityItem = EntityItem(world, event.item.posX, event.item.posY, event.item.posZ, stack)
            alteredEntityItem.setNoPickupDelay()
            world.spawnEntity(alteredEntityItem)
        }
    }

    /**
     * Attempts to perform pickup to any backpack exists in targetInventory.
     */
    private fun attemptPickup(targetInventory: IItemHandler, stack: ItemStack): ItemStack {
        var stack = stack

        for (i in 0 until targetInventory.slots) {
            val backpackStack = targetInventory.getStackInSlot(i)

            if (backpackStack.item !is BackpackItem)
                continue

            val wrapper = backpackStack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: continue

            if (!wrapper.canPickupItem(stack))
                continue

            var slotIndex = 0
            while (!stack.isEmpty && slotIndex < wrapper.slots) {
                stack = wrapper.backpackItemStackHandler.prioritizedInsertion(slotIndex, stack, false)

                slotIndex++
            }

            if (stack.isEmpty)
                break
        }

        return stack
    }

    @SubscribeEvent
    @JvmStatic
    fun onPlayerInteract(event: PlayerInteractEvent.EntityInteract) {
        val player = event.entityPlayer
        val stack = player.heldItemMainhand
        val entity = event.target

        if (stack.item is BackpackItem) {
            if (player.isSneaking) {
                val wrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null)
                    ?: return
                var transferred = BackpackInventoryHelper.attemptDepositOnEntity(wrapper, entity)
                transferred =
                    BackpackInventoryHelper.attemptRestockFromEntity(wrapper, entity) || transferred

                if (transferred) {
                    player.world.playSound(
                        null,
                        player.position,
                        SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                        SoundCategory.BLOCKS,
                        0.5f,
                        0.5f
                    )

                    event.isCanceled = true
                    event.cancellationResult = EnumActionResult.SUCCESS
                }
            }
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onPlayerTicking(event: TickEvent.PlayerTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            feedTickCounter++

            if (feedTickCounter % 20 == 0) {
                feedTickCounter = 0

                if (event.player.isCreative)
                    return

                BackpackFeedingHelper.attemptFeed(event.player)
            }
        }
    }
}