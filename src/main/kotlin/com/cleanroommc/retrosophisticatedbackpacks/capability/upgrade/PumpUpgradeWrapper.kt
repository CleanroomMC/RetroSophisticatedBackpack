package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackFluidHandler
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.Capabilities
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.PumpUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler
import java.util.LinkedList

open class PumpUpgradeWrapper(
    private val filterSlots: Int = 0,
    interactWithHandDefault: Boolean = false,
    interactWithWorldDefault: Boolean = false,
    interactWithFluidHandlersDefault: Boolean = true
) : UpgradeWrapper<PumpUpgradeItem>(), IPumpUpgrade, IToggleable {
    companion object {
        private const val ENABLED_TAG = "Enabled"
        private const val INPUT_TAG = "Input"
        private const val HAND_TAG = "InteractWithHand"
        private const val WORLD_TAG = "InteractWithWorld"
        private const val FLUID_HANDLERS_TAG = "InteractWithFluidHandlers"
        private const val COOLDOWN_UNTIL_TAG = "CooldownUntil"
        private const val LAST_HAND_ACTION_TAG = "LastHandAction"
        private const val FILTER_TAG = "FluidFilter"
        private const val DID_NOTHING_COOLDOWN = 40L
        private const val HAND_COOLDOWN = 3L
        private const val WORLD_COOLDOWN = 20L
        private const val FLUID_HANDLER_COOLDOWN = 20L
        private const val PLAYER_SEARCH_RANGE = 3.0
        private const val WORLD_RANGE = 4
    }

    override val settingsLangKey = "gui.pump_settings".asTranslationKey()
    override var enabled = true
    override var isInput = true
    override var interactWithHand = interactWithHandDefault
    override var interactWithWorld = interactWithWorldDefault
    override var interactWithFluidHandlers = interactWithFluidHandlersDefault
    override val fluidFilters: MutableList<FluidStack?> = MutableList(filterSlots) { null }
    private var cooldownUntil = 0L
    private var lastHandAction = -1L

    override fun tick(player: EntityPlayer?, wrapper: BackpackWrapper, world: World, pos: BlockPos) {
        if (!enabled || world.isRemote || world.totalWorldTime < cooldownUntil || !wrapper.hasTankUpgrade()) {
            return
        }
        val storage = BackpackFluidHandler(wrapper)
        cooldownUntil = world.totalWorldTime + tick(storage, player, wrapper, world, pos)
    }

    private fun tick(storage: IFluidHandler, player: EntityPlayer?, wrapper: BackpackWrapper, world: World, pos: BlockPos): Long {
        if (interactWithHand && handlePlayers(player, world, pos, storage)) {
            lastHandAction = world.totalWorldTime
            return HAND_COOLDOWN
        }
        if (interactWithWorld) {
            val cooldown = if (isInput) fillFromBlockInRange(world, pos, storage) else placeFluidInWorld(world, pos, storage)
            if (cooldown != null) {
                return cooldown
            }
        }
        if (interactWithFluidHandlers && interactWithAttachedFluidHandlers(world, pos, storage, wrapper)) {
            return FLUID_HANDLER_COOLDOWN
        }
        return if (lastHandAction + HAND_COOLDOWN * 10 > world.totalWorldTime) HAND_COOLDOWN else DID_NOTHING_COOLDOWN
    }

    override fun setFluidFilter(slot: Int, fluid: FluidStack?) {
        if (slot !in fluidFilters.indices) {
            return
        }
        fluidFilters[slot] = fluid?.copy()?.also { it.amount = Fluid.BUCKET_VOLUME }
    }

    override fun fluidMatches(fluid: FluidStack): Boolean =
        fluidFilters.isEmpty() || fluidFilters.filterNotNull().let { filters ->
            filters.isEmpty() || filters.any { it.isFluidEqual(fluid) }
        }

    private fun handlePlayers(player: EntityPlayer?, world: World, pos: BlockPos, storage: IFluidHandler): Boolean {
        if (player != null) {
            return handlePlayer(player, storage)
        }
        val players = world.getEntitiesWithinAABB(
            EntityPlayer::class.java,
            AxisAlignedBB(
                pos.x - PLAYER_SEARCH_RANGE, pos.y - PLAYER_SEARCH_RANGE, pos.z - PLAYER_SEARCH_RANGE,
                pos.x + 1 + PLAYER_SEARCH_RANGE, pos.y + 1 + PLAYER_SEARCH_RANGE, pos.z + 1 + PLAYER_SEARCH_RANGE
            )
        )
        return players.any { handlePlayer(it, storage) }
    }

    private fun handlePlayer(player: EntityPlayer, storage: IFluidHandler): Boolean =
        handleHand(player, EnumHand.MAIN_HAND, storage) || handleHand(player, EnumHand.OFF_HAND, storage)

    private fun handleHand(player: EntityPlayer, hand: EnumHand, storage: IFluidHandler): Boolean {
        val stack = player.getHeldItem(hand)
        if (stack.isEmpty || stack.count != 1) {
            return false
        }
        if (stack.item is BackpackItem) {
            return false
        }
        val itemHandler = FluidUtil.getFluidHandler(stack.copy()) ?: return false
        val moved = if (isInput) fillFromFluidHandler(itemHandler, storage, Fluid.BUCKET_VOLUME)
        else fillFluidHandler(itemHandler, storage, Fluid.BUCKET_VOLUME)
        if (moved) {
            player.setHeldItem(hand, itemHandler.container)
        }
        return moved
    }

    private fun interactWithAttachedFluidHandlers(world: World, pos: BlockPos, storage: IFluidHandler, wrapper: BackpackWrapper): Boolean {
        for (side in EnumFacing.values()) {
            val te = world.getTileEntity(pos.offset(side)) ?: continue
            val fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.opposite) ?: continue
            if (if (isInput) fillFromFluidHandler(fluidHandler, storage, getMaxInOut(wrapper))
                else fillFluidHandler(fluidHandler, storage, getMaxInOut(wrapper))
            ) {
                return true
            }
        }
        return false
    }

    private fun fillFromBlockInRange(world: World, basePos: BlockPos, storage: IFluidHandler): Long? {
        val queue = LinkedList<BlockPos>()
        val searched = mutableSetOf<BlockPos>()
        queue.add(basePos)
        searched.add(basePos)
        while (queue.isNotEmpty()) {
            val pos = queue.poll()
            if (fillFromBlock(world, pos, storage)) {
                return maxOf(1L, Math.sqrt(distanceSq(basePos, pos).toDouble()).toLong()) * WORLD_COOLDOWN
            }
            for (side in EnumFacing.values()) {
                val next = pos.offset(side)
                if (searched.add(next) && distanceSq(basePos, next) < WORLD_RANGE * WORLD_RANGE) {
                    queue.add(next)
                }
            }
        }
        return null
    }

    private fun fillFromBlock(world: World, pos: BlockPos, storage: IFluidHandler): Boolean {
        val fluidHandler = FluidUtil.getFluidHandler(world, pos, null) ?: return false
        return fillFromFluidHandler(fluidHandler, storage, Fluid.BUCKET_VOLUME)
    }

    private fun placeFluidInWorld(world: World, pos: BlockPos, storage: IFluidHandler): Long? {
        for (side in EnumFacing.values()) {
            if (side == EnumFacing.UP) {
                continue
            }
            val offsetPos = pos.offset(side)
            if (!isValidForFluidPlacement(world, offsetPos)) {
                continue
            }
            for (tank in storage.tankProperties) {
                val fluid = tank.contents ?: continue
                if (fluid.amount <= 0 || !fluidMatches(fluid)) {
                    continue
                }
                val resource = FluidStack(fluid.fluid, minOf(Fluid.BUCKET_VOLUME, fluid.amount), fluid.tag?.copy())
                if (FluidUtil.tryPlaceFluid(null, world, offsetPos, storage, resource)) {
                    return WORLD_COOLDOWN
                }
            }
        }
        return null
    }

    private fun isValidForFluidPlacement(world: World, pos: BlockPos): Boolean {
        val state = world.getBlockState(pos)
        return world.isAirBlock(pos) || state.material.isLiquid && state.getValue(BlockLiquid.LEVEL) != 0
    }

    private fun fillFromFluidHandler(source: IFluidHandler, storage: IFluidHandler, maxDrain: Int): Boolean {
        val contained = source.drain(maxDrain, false) ?: return false
        if (contained.amount <= 0 || !fluidMatches(contained)) {
            return false
        }
        return FluidUtil.tryFluidTransfer(storage, source, contained, true) != null
    }

    private fun fillFluidHandler(destination: IFluidHandler, storage: IFluidHandler, maxFill: Int): Boolean {
        for (tank in storage.tankProperties) {
            val fluid = tank.contents ?: continue
            if (fluid.amount <= 0 || !fluidMatches(fluid)) {
                continue
            }
            val resource = FluidStack(fluid.fluid, minOf(maxFill, fluid.amount), fluid.tag?.copy())
            if (FluidUtil.tryFluidTransfer(destination, storage, resource, true) != null) {
                return true
            }
        }
        return false
    }

    private fun getMaxInOut(wrapper: BackpackWrapper): Int =
        maxOf(Fluid.BUCKET_VOLUME, (getSlotRows(wrapper) * Config.pumpUpgrade.maxInputOutput * getAdjustedStackMultiplier(wrapper)).toInt())

    private fun getSlotRows(wrapper: BackpackWrapper): Int =
        maxOf(1, (wrapper.backpackInventorySize() + 8) / 9)

    private fun getAdjustedStackMultiplier(wrapper: BackpackWrapper): Double =
        1.0 + Config.pumpUpgrade.stackMultiplierRatio * (wrapper.getTotalStackMultiplier() - 1)

    private fun distanceSq(first: BlockPos, second: BlockPos): Int {
        val dx = first.x - second.x
        val dy = first.y - second.y
        val dz = first.z - second.z
        return dx * dx + dy * dy + dz * dz
    }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setBoolean(ENABLED_TAG, enabled)
        nbt.setBoolean(INPUT_TAG, isInput)
        nbt.setBoolean(HAND_TAG, interactWithHand)
        nbt.setBoolean(WORLD_TAG, interactWithWorld)
        nbt.setBoolean(FLUID_HANDLERS_TAG, interactWithFluidHandlers)
        nbt.setLong(COOLDOWN_UNTIL_TAG, cooldownUntil)
        nbt.setLong(LAST_HAND_ACTION_TAG, lastHandAction)
        fluidFilters.forEachIndexed { index, fluid ->
            fluid?.let { nbt.setTag("$FILTER_TAG$index", it.writeToNBT(NBTTagCompound())) }
        }
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        enabled = if (nbt.hasKey(ENABLED_TAG)) nbt.getBoolean(ENABLED_TAG) else enabled
        isInput = if (nbt.hasKey(INPUT_TAG)) nbt.getBoolean(INPUT_TAG) else isInput
        interactWithHand = if (nbt.hasKey(HAND_TAG)) nbt.getBoolean(HAND_TAG) else interactWithHand
        interactWithWorld = if (nbt.hasKey(WORLD_TAG)) nbt.getBoolean(WORLD_TAG) else interactWithWorld
        interactWithFluidHandlers = if (nbt.hasKey(FLUID_HANDLERS_TAG)) nbt.getBoolean(FLUID_HANDLERS_TAG) else interactWithFluidHandlers
        if (nbt.hasKey(COOLDOWN_UNTIL_TAG))
            cooldownUntil = nbt.getLong(COOLDOWN_UNTIL_TAG)
        if (nbt.hasKey(LAST_HAND_ACTION_TAG))
            lastHandAction = nbt.getLong(LAST_HAND_ACTION_TAG)
        for (slot in fluidFilters.indices) {
            fluidFilters[slot] = if (nbt.hasKey("$FILTER_TAG$slot")) {
                FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("$FILTER_TAG$slot"))
            } else null
        }
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.PUMP_UPGRADE_CAPABILITY ||
                capability == Capabilities.IPUMP_UPGRADE_CAPABILITY ||
                super<IToggleable>.hasCapability(capability, facing) ||
                super<UpgradeWrapper>.hasCapability(capability, facing)
}

class AdvancedPumpUpgradeWrapper : PumpUpgradeWrapper(
    filterSlots = Config.pumpUpgrade.filterSlots,
    interactWithHandDefault = true,
    interactWithWorldDefault = false,
    interactWithFluidHandlersDefault = true
) {
    override val settingsLangKey = "gui.advanced_pump_settings".asTranslationKey()

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.ADVANCED_PUMP_UPGRADE_CAPABILITY || super.hasCapability(capability, facing)
}
