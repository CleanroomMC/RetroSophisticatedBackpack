package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemRecord
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Collections

interface IDiscHandler {
    fun supports(stack: ItemStack): Boolean
    fun playDisc(world: World, pos: BlockPos, stack: ItemStack): Boolean
    fun stopDisc(world: World, pos: BlockPos) {
        world.playEvent(null, DiscHandlerRegistry.RECORD_PLAY_EVENT, pos, 0)
    }

    fun getMusicLengthInTicks(stack: ItemStack, world: World): Long?
}

object DiscHandlerRegistry {
    const val RECORD_PLAY_EVENT = 1010

    private val handlers = mutableListOf<IDiscHandler>(VanillaDiscHandler)

    @JvmStatic
    fun getHandlers(): List<IDiscHandler> = Collections.unmodifiableList(handlers)

    @JvmStatic
    fun registerHandler(handler: IDiscHandler) {
        handlers.add(handler)
    }

    @JvmStatic
    fun findHandler(stack: ItemStack): IDiscHandler? =
        if (stack.isEmpty) null else handlers.firstOrNull { it.supports(stack) }

    @JvmStatic
    fun isSupported(stack: ItemStack): Boolean = findHandler(stack) != null

    @JvmStatic
    fun playDisc(world: World, pos: BlockPos, stack: ItemStack): Boolean =
        findHandler(stack)?.playDisc(world, pos, stack) == true

    @JvmStatic
    fun stopDisc(world: World, pos: BlockPos) {
        handlers.firstOrNull()?.stopDisc(world, pos)
            ?: world.playEvent(null, RECORD_PLAY_EVENT, pos, 0)
    }

    @JvmStatic
    fun stopDisc(world: World, pos: BlockPos, stack: ItemStack) {
        findHandler(stack)?.stopDisc(world, pos) ?: stopDisc(world, pos)
    }

    @JvmStatic
    fun getMusicLengthInTicks(stack: ItemStack, world: World): Long? =
        findHandler(stack)?.getMusicLengthInTicks(stack, world)
}

object VanillaDiscHandler : IDiscHandler {
    private const val DEFAULT_DISC_LENGTH = 3600L

    private val vanillaDiscLengths = mapOf(
        Items.RECORD_13 to 1780L,
        Items.RECORD_CAT to 3700L,
        Items.RECORD_BLOCKS to 6900L,
        Items.RECORD_CHIRP to 3700L,
        Items.RECORD_FAR to 3480L,
        Items.RECORD_MALL to 3940L,
        Items.RECORD_MELLOHI to 1920L,
        Items.RECORD_STAL to 3000L,
        Items.RECORD_STRAD to 3760L,
        Items.RECORD_WARD to 5020L,
        Items.RECORD_11 to 1420L,
        Items.RECORD_WAIT to 4760L
    )

    override fun supports(stack: ItemStack): Boolean = stack.item is ItemRecord

    override fun playDisc(world: World, pos: BlockPos, stack: ItemStack): Boolean {
        if (stack.item !is ItemRecord) {
            return false
        }
        world.playEvent(null, DiscHandlerRegistry.RECORD_PLAY_EVENT, pos, Item.getIdFromItem(stack.item))
        return true
    }

    override fun getMusicLengthInTicks(stack: ItemStack, world: World): Long? =
        if (supports(stack)) vanillaDiscLengths[stack.item] ?: DEFAULT_DISC_LENGTH else null
}
