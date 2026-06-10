package com.cleanroommc.retrosophisticatedbackpacks.handler

import baubles.api.BaublesApi
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IVoidUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiData
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.PlayerInventoryGuiFactory
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.Items
import net.minecraft.entity.EntityList
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.SoundCategory
import net.minecraftforge.event.entity.living.LivingSpawnEvent
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper
import kotlin.math.cos
import kotlin.math.sin

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
object EntityEventHandler {
    private val BACKPACK_SPAWN_CHANCES = lazy {
        arrayOf(
            BackpackChances(
                Items.backpackObsidian,
                Config.obsidianBackpack.spawnChanceOnMob,
                Config.obsidianBackpack.dropChance
            ),
            BackpackChances(
                Items.backpackDiamond,
                Config.diamondBackpack.spawnChanceOnMob,
                Config.diamondBackpack.dropChance
            ),
            BackpackChances(Items.backpackGold, Config.goldBackpack.spawnChanceOnMob, Config.goldBackpack.dropChance),
            BackpackChances(Items.backpackIron, Config.ironBackpack.spawnChanceOnMob, Config.ironBackpack.dropChance),
            BackpackChances(
                Items.backpackLeather,
                Config.leatherBackpack.spawnChanceOnMob,
                Config.leatherBackpack.dropChance
            )
        )
    }

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
                stack = wrapper.backpackItemStackHandler.prioritizedInsertionRespectVoid(
                    slotIndex,
                    stack,
                    false,
                    IVoidUpgrade.TransferSource.UPGRADE_OR_WORLD_INTERACTION
                )

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
    fun onPlayerRightClick(event: PlayerInteractEvent.EntityInteractSpecific) {
        val clicker = event.entityPlayer
        val target = event.target as? EntityLivingBase ?: return
        val world = clicker.world

        if (clicker.getDistanceSq(target) > 16.0) return

        val stack = target.getItemStackFromSlot(EntityEquipmentSlot.CHEST)
        val item = stack.item

        if (stack.isEmpty) return
        if (item !is BackpackItem) return

        val hitVec = event.localPos
        val yawRad = Math.toRadians((-target.renderYawOffset).toDouble())

        val localX = hitVec.x * cos(yawRad) - hitVec.z * sin(yawRad)
        val localZ = hitVec.x * sin(yawRad) + hitVec.z * cos(yawRad)
        val localY = hitVec.y

        val isClickingBackpack =
            localX in -0.3..0.3 &&             // Width of the backpack
                    localY in 0.7..1.5 &&      // Height of the backpack on the body
                    localZ in -0.5..-0.15

        if (!isClickingBackpack) return

        if (!world.isRemote) {
            PlayerInventoryGuiFactory.open(
                target,
                clicker,
                PlayerInventoryGuiData.InventoryType.PLAYER_INVENTORY,
                EntityEquipmentSlot.CHEST.index
            )
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onSpecialSpawn(event: LivingSpawnEvent.SpecialSpawn) {
        val entity = event.entityLiving
        val world = entity.world

        if (world.isRemote) return

        val registryName = EntityList.getKey(entity)?.toString() ?: return

        if (entity !is EntityLiving) return

        if (registryName in Config.backpackEntitySpawnList) {
            val currentChestStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST)

            if (!currentChestStack.isEmpty) return

            val difficulty = world.getDifficultyForLocation(entity.position).additionalDifficulty
            val encounterChance = Config.baseBackpackSpawnChance + (difficulty * 0.002f)

            if (world.rand.nextFloat() <= encounterChance) {
                val tierRoll = world.rand.nextFloat()

                for ((backpack, spawnChance, dropChance) in BACKPACK_SPAWN_CHANCES.value) {
                    val threshold = spawnChance + (difficulty * 0.01f)

                    if (tierRoll <= threshold) {
                        val backpackStack = ItemStack(backpack, 1)

                        // TODO: Add disc upgrade here :)

                        entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, backpackStack)
                        entity.setDropChance(EntityEquipmentSlot.CHEST, dropChance)

                        return
                    }
                }
            }
        }
    }

    private data class BackpackChances(val backpack: BackpackItem, val spawnChance: Float, val dropChance: Float)
}
