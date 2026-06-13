package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.handler.CapabilityHandler
import com.cleanroommc.retrosophisticatedbackpacks.handler.NetworkHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.network.S2CMobCatcherContentsPacket
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityFlying
import net.minecraft.entity.EntityList
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.IEntityOwnable
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.EntityWaterMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.IInventory
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import java.util.UUID
import kotlin.math.ceil

object MobCatcherHandler {
    private const val RELEASE_REACH = 5.0

    fun tryCapture(player: EntityPlayer, entity: EntityLivingBase): EnumActionResult {
        val stack = player.heldItemMainhand
        if (stack.item !is BackpackItem) {
            return EnumActionResult.PASS
        }
        val backpackWrapper = stack.getCapability(Capabilities.BACKPACK_CAPABILITY, null) ?: return EnumActionResult.PASS
        val upgradeWrapper = getBestUpgrade(backpackWrapper) ?: return EnumActionResult.PASS
        if (player.world.isRemote) {
            return EnumActionResult.SUCCESS
        }

        val result = capture(player as EntityPlayerMP, backpackWrapper, entity, upgradeWrapper.isAdvanced)
        player.sendStatusMessage(TextComponentTranslation(result.messageKey.asTranslationKey(), *result.args), true)
        return if (result.success) EnumActionResult.SUCCESS else EnumActionResult.FAIL
    }

    private fun capture(
        player: EntityPlayerMP,
        backpackWrapper: BackpackWrapper,
        entity: EntityLivingBase,
        advanced: Boolean
    ): CaptureResult {
        getEligibilityError(player, entity, advanced)?.let { return fail(player, it) }

        val hostile = isHostile(entity)
        val slotCost = getSlotCost(entity, hostile)
        val maxSlotCost = if (advanced) Config.mobCatcherUpgrade.advancedMaxSlotCost else Config.mobCatcherUpgrade.basicMaxSlotCost
        if (slotCost > maxSlotCost) {
            return fail(player, "gui.status.mob_catcher_too_large", slotCost, maxSlotCost)
        }

        val footprint = MobCatcherStorage.getFootprint(entity, slotCost)
        val slot = MobCatcherStorage.findEmptyRectangle(backpackWrapper, footprint)
            ?: return fail(player, "gui.status.mob_catcher_no_space", footprint.width, footprint.height)
        val entityType = EntityList.getKey(entity)
            ?: return fail(player, "gui.status.mob_catcher_invalid_entity")
        val entityTag = NBTTagCompound()
        entity.writeToNBT(entityTag)
        entityTag.setString("id", entityType.toString())
        entityTag.removeTag("UUIDMost")
        entityTag.removeTag("UUIDLeast")

        val capturedMob = CapturedMob(
            UUID.randomUUID(),
            entityType,
            entityTag,
            slot,
            footprint.width,
            footprint.height,
            slotCost,
            hostile,
            getCapturedMobDisplayName(entity),
            ceil(entity.health.toDouble()).toInt(),
            ceil(getEffectiveMaxHealth(entity)).toInt()
        )
        MobCatcherStorage.addCapturedMob(backpackWrapper, capturedMob)
        entity.setDead()
        syncBackpack(player, backpackWrapper)
        playMobCatcherSound(player, true, 0.7f)
        return CaptureResult(true, "gui.status.mob_catcher_captured", arrayOf(capturedMob.displayName))
    }

    private fun getEligibilityError(player: EntityPlayerMP, entity: EntityLivingBase, advanced: Boolean): String? {
        if (entity is EntityPlayer) {
            return "gui.status.mob_catcher_players_blocked"
        }
        if (entity is EntityDragon || entity is EntityWither) {
            return "gui.status.mob_catcher_boss_blocked"
        }
        if (entity.isRiding || entity.isBeingRidden) {
            return "gui.status.mob_catcher_passengers_blocked"
        }
        val entityType = EntityList.getKey(entity)
        if (entityType == null || entityType in configuredEntityTypes(Config.mobCatcherUpgrade.entityBlockList)) {
            return "gui.status.mob_catcher_blocklisted"
        }
        if (entity is IEntityOwnable && entity.ownerId != null && entity.ownerId != player.uniqueID) {
            return "gui.status.mob_catcher_not_owner"
        }
        if (Config.mobCatcherUpgrade.disallowInventoryEntities && entity is IInventory) {
            return "gui.status.mob_catcher_inventory_blocked"
        }
        if (!advanced && isHostile(entity)) {
            return "gui.status.mob_catcher_hostile_needs_advanced"
        }
        return null
    }

    fun release(player: EntityPlayerMP, capturedMobId: UUID) {
        val container = player.openContainer as? BackpackContainer ?: return
        val backpackWrapper = container.backpackWrapper
        val capturedMob = MobCatcherStorage.getCapturedMob(backpackWrapper, capturedMobId) ?: return
        val entity = createEntity(player.world, capturedMob) as? EntityLivingBase ?: run {
            player.sendStatusMessage(TextComponentTranslation("gui.status.mob_catcher_release_failed".asTranslationKey()), true)
            return
        }
        val target = getReleasePosition(player, entity) ?: run {
            player.sendStatusMessage(TextComponentTranslation("gui.status.mob_catcher_no_release_space".asTranslationKey()), true)
            playMobCatcherSound(player, false, 0.8f)
            return
        }
        entity.setLocationAndAngles(target.x, target.y, target.z, player.rotationYaw, 0f)
        entity.setUniqueId(UUID.randomUUID())
        if (!player.world.spawnEntity(entity)) {
            player.sendStatusMessage(TextComponentTranslation("gui.status.mob_catcher_release_failed".asTranslationKey()), true)
            return
        }
        MobCatcherStorage.removeCapturedMob(backpackWrapper, capturedMobId)
        syncBackpack(player, backpackWrapper)
        player.sendStatusMessage(TextComponentTranslation("gui.status.mob_catcher_released".asTranslationKey(), capturedMob.displayName), true)
        playMobCatcherSound(player, true, 1.2f)
    }

    private fun createEntity(world: World, capturedMob: CapturedMob): Entity? {
        val entity = EntityList.createEntityByIDFromName(capturedMob.entityType, world) ?: return null
        entity.readFromNBT(capturedMob.entityNbt)
        return entity
    }

    private fun getReleasePosition(player: EntityPlayerMP, entity: EntityLivingBase): Vec3d? {
        val eye = player.getPositionEyes(1f)
        val look = player.lookVec
        val hit = player.world.rayTraceBlocks(eye, eye.add(look.scale(RELEASE_REACH)), false, true, false)
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            getValidReleasePosition(player, entity, hit.blockPos.offset(hit.sideHit))?.let { return it }
            getValidReleasePosition(player, entity, hit.blockPos.up())?.let { return it }
        }

        var horizontal = Vec3d(look.x, 0.0, look.z)
        if (horizontal.lengthSquared() < 1.0E-4) {
            horizontal = Vec3d.fromPitchYaw(0f, player.rotationYaw)
        }
        val direction = horizontal.normalize()
        val maxFallbackDistance = maxOf(
            2,
            minOf(RELEASE_REACH.toInt(), ceil(entity.width / 2.0 + player.width / 2.0 + 1.0).toInt())
        )
        for (distance in 1..maxFallbackDistance) {
            val candidate = player.positionVector.add(direction.scale(distance.toDouble()))
            getValidReleasePosition(player, entity, BlockPos(candidate.x, player.posY, candidate.z))?.let { return it }
        }
        return null
    }

    private fun getValidReleasePosition(player: EntityPlayerMP, entity: EntityLivingBase, spawnPos: BlockPos): Vec3d? {
        val pos = getReleasePositionOnGround(player, entity, spawnPos) ?: return null
        val bounds = AxisAlignedBB(
            pos.x - entity.width / 2.0,
            pos.y,
            pos.z - entity.width / 2.0,
            pos.x + entity.width / 2.0,
            pos.y + entity.height,
            pos.z + entity.width / 2.0
        )
        return if (player.world.getCollisionBoxes(entity, bounds).isEmpty() &&
            player.world.checkNoEntityCollision(bounds, entity)
        ) pos else null
    }

    private fun getReleasePositionOnGround(player: EntityPlayerMP, entity: EntityLivingBase, spawnPos: BlockPos): Vec3d? {
        if (canReleaseWithoutGround(entity)) {
            return Vec3d(spawnPos.x + 0.5, spawnPos.y.toDouble(), spawnPos.z + 0.5)
        }
        val groundPos = spawnPos.down()
        val bounds = player.world.getBlockState(groundPos).getCollisionBoundingBox(player.world, groundPos)
        if (bounds == null || bounds == Block.NULL_AABB) {
            return null
        }
        return Vec3d(spawnPos.x + 0.5, groundPos.y + bounds.maxY, spawnPos.z + 0.5)
    }

    private fun canReleaseWithoutGround(entity: EntityLivingBase): Boolean =
        entity is EntityFlying || entity is EntityWaterMob || entity is net.minecraft.entity.passive.EntityFlying || entity.hasNoGravity()

    fun getBestUpgrade(backpackWrapper: BackpackWrapper): MobCatcherUpgradeWrapper? {
        val upgrades = backpackWrapper.gatherCapabilityUpgrades(Capabilities.MOB_CATCHER_UPGRADE_CAPABILITY)
        return upgrades.firstOrNull { it.isAdvanced } ?: upgrades.firstOrNull()
    }

    fun isHostile(entity: EntityLivingBase): Boolean {
        val entityType = EntityList.getKey(entity) ?: return entity is IMob
        if (entityType in configuredEntityTypes(Config.mobCatcherUpgrade.passiveOverrides)) {
            return false
        }
        return entityType in configuredEntityTypes(Config.mobCatcherUpgrade.hostileOverrides) || entity is IMob
    }

    fun getSlotCost(entity: EntityLivingBase, hostile: Boolean): Int {
        val maxHealth = getEffectiveMaxHealth(entity)
        val currentHealth = maxOf(0.0, entity.health.toDouble())
        val baseCost = maxHealth / 2.0 + minOf(currentHealth, maxHealth) / 2.0
        val multiplier = if (hostile) Config.mobCatcherUpgrade.hostileMultiplier else Config.mobCatcherUpgrade.animalMultiplier
        return maxOf(1, ceil(baseCost * multiplier).toInt())
    }

    fun getEffectiveMaxHealth(entity: EntityLivingBase): Double =
        maxOf(
            1.0,
            entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).attributeValue,
            entity.maxHealth.toDouble(),
            entity.health.toDouble()
        )

    private fun getCapturedMobDisplayName(entity: EntityLivingBase): String =
        if (entity.hasCustomName()) entity.customNameTag else entity.displayName.unformattedText

    private fun configuredEntityTypes(configuredEntityTypes: Array<String>): Set<ResourceLocation> =
        configuredEntityTypes.mapNotNull {
            try {
                ResourceLocation(it)
            } catch (_: RuntimeException) {
                null
            }
        }.toSet()

    private fun fail(player: EntityPlayerMP, messageKey: String, vararg args: Any): CaptureResult {
        playMobCatcherSound(player, false, 0.8f)
        return CaptureResult(false, messageKey, args)
    }

    private fun playMobCatcherSound(player: EntityPlayerMP, success: Boolean, basePitch: Float) {
        val sound = if (success) SoundEvents.ENTITY_ITEM_PICKUP else SoundEvents.BLOCK_NOTE_BASS
        val pitch = basePitch + (player.rng.nextFloat() - 0.5f) * 0.16f
        player.world.playSound(null, player.position, sound, SoundCategory.PLAYERS, 0.7f, pitch)
    }

    private fun syncBackpack(player: EntityPlayerMP, backpackWrapper: BackpackWrapper) {
        player.openContainer.detectAndSendChanges()
        player.inventoryContainer.detectAndSendChanges()
        CapabilityHandler.updateBackpackInventory(backpackWrapper)
        syncCapturedMobsToViewers(player, backpackWrapper)
    }

    private fun syncCapturedMobsToViewers(player: EntityPlayerMP, backpackWrapper: BackpackWrapper) {
        player.server.playerList.players
            .filter {
                it == player || (it.openContainer as? BackpackContainer)?.backpackWrapper?.uuid == backpackWrapper.uuid
            }
            .forEach {
                NetworkHandler.INSTANCE.sendTo(S2CMobCatcherContentsPacket(backpackWrapper), it)
            }
    }

    private data class CaptureResult(val success: Boolean, val messageKey: String, val args: Array<out Any> = emptyArray())
}
