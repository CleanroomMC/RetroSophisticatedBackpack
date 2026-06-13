package com.cleanroommc.retrosophisticatedbackpacks.capability

import com.cleanroommc.retrosophisticatedbackpacks.backpack.DisplaySide
import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.ICompactingUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IMagnetUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedCompactingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedVoidUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CompactingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.VoidUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.CapturedMob
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.mobcatcher.MobCatcherStorage
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.UpgradeItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.ExponentialStackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.mixin.EntityItemAccessor
import com.cleanroommc.retrosophisticatedbackpacks.util.BackpackItemStackHelper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.Container
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.ItemHandlerHelper
import java.util.*

class BackpackWrapper(
    var backpackInventorySize: () -> Int = { 27 },
    var upgradeSlotsSize: () -> Int = { 1 },
    var uuid: UUID = UUID.randomUUID(),
    private val containerStack: ItemStack = ItemStack.EMPTY,
) : IItemHandler, ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "BackpackInventory"
        private const val UPGRADE_SLOTS_TAG = "UpgradeSlots"
        private const val BACKPACK_INVENTORY_SIZE_TAG = "BackpackInventorySize"
        private const val UPGRADE_SLOTS_SIZE_TAG = "UpgradeSlotsSize"

        private const val MAIN_COLOR_TAG = "MainColor"
        private const val ACCENT_COLOR_TAG = "AccentColor"
        private const val CUSTOM_NAME_TAG = "CustomName"

        private const val MEMORY_STACK_ITEMS_TAG = "MemoryItems"
        private const val MEMORY_STACK_RESPECT_NBT_TAG = "MemoryRespectNBT"
        private const val SORT_TYPE_TAG = "SortType"
        private const val LOCKED_SLOTS_TAG = "LockedSlots"
        private const val MAIN_SETTINGS_TAG = "MainSettings"
        private const val MAIN_SETTINGS_CONTEXT_TAG = "Context"
        private const val MAIN_SETTINGS_SHIFT_CLICK_INTO_OPEN_TAB_TAG = "ShiftClickIntoOpenTab"
        private const val MAIN_SETTINGS_KEEP_TAB_OPEN_TAG = "KeepTabOpen"
        private const val MAIN_SETTINGS_KEEP_SEARCH_PHRASE_TAG = "KeepSearchPhrase"
        private const val MAIN_SETTINGS_SEARCH_PHRASE_TAG = "SearchPhrase"
        private const val MAIN_SETTINGS_ANOTHER_PLAYER_CAN_OPEN_TAG = "AnotherPlayerCanOpen"
        private const val ITEM_DISPLAY_SETTINGS_TAG = "ItemDisplay"
        private const val ITEM_DISPLAY_SLOTS_TAG = "Slots"
        private const val ITEM_DISPLAY_ROTATIONS_TAG = "Rotations"
        private const val ITEM_DISPLAY_COLOR_TAG = "Color"
        private const val ITEM_DISPLAY_SIDE_TAG = "DisplaySide"
        private const val UUID_TAG = "UUID"

        const val DEFAULT_MAIN_COLOR: Int = -0x339ec6
        const val DEFAULT_ACCENT_COLOR: Int = -0x9dd1e6
    }

    var isCached: Boolean = false
    var backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), this)
    var upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize(), this)
    var sortType: SortType = SortType.BY_NAME

    var mainColor = DEFAULT_MAIN_COLOR
    var accentColor = DEFAULT_ACCENT_COLOR
    var customName: String? = null
    var isGuiInteractionInProgress = false
    var settingsContext: SettingsContext = SettingsContext.PLAYER
    var shiftClickIntoOpenTab = false
    var keepTabOpen = true
    var keepSearchPhrase = false
    var searchPhrase = ""
    var anotherPlayerCanOpen = Config.allowOpeningOtherPlayerBackpacks
    var itemDisplayColor: EnumDyeColor = EnumDyeColor.RED
    var itemDisplaySide: DisplaySide = DisplaySide.FRONT
    private val itemDisplaySlots = linkedSetOf<Int>()
    private val itemDisplayRotations = mutableMapOf<Int, Int>()
    private val slotsToCompact = mutableSetOf<Int>()
    private var compactingSlotsInitialized = false
    private val slotsToVoid = mutableSetOf<Int>()
    val capturedMobs: MutableList<CapturedMob> = mutableListOf()
    var capturedMobsColumns = 0
    private var updatingCapturedMobLayout = false

    enum class SettingsContext {
        PLAYER,
        STORAGE;

        fun next(): SettingsContext =
            if (this == PLAYER) STORAGE else PLAYER
    }

    fun isStackedByMultiplication(): Boolean =
        upgradeItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<ExponentialStackUpgradeItem>().any()

    private fun getStackMultiplyFunction(condition: Boolean): (Int, Int) -> Int =
        if (condition) Int::times
        else Int::plus

    fun getTotalStackMultiplier(): Int =
        getTotalStackMultiplier(isStackedByMultiplication())

    fun getTotalStackMultiplier(condition: Boolean): Int {
        val base = if (condition) 1 else 0
        val stackUpgradeItems = upgradeItemStackHandler.inventory
            .map(ItemStack::getItem)
            .filterIsInstance<StackUpgradeItem>()
        val func = getStackMultiplyFunction(condition)

        if (!condition && stackUpgradeItems.isEmpty())
            return 1

        return stackUpgradeItems.fold(base) { acc, item -> func(acc, item.multiplier()) }
    }

    fun getTotalStackMultiplier(stack: ItemStack): Int =
        if (Config.canStackWithStackUpgrade(stack)) getTotalStackMultiplier() else 1

    fun getStackLimit(stack: ItemStack): Int =
        stack.maxStackSize * getTotalStackMultiplier(stack)

    fun canAddStackUpgrade(newMultiplier: Int): Boolean {
        // Ensures no overflow for vanilla itemstack, no guarantee for modded itemstack
        val currentMultiplier = getTotalStackMultiplier() * 64

        try {
            Math.multiplyExact(currentMultiplier, newMultiplier)

            return true
        } catch (_: ArithmeticException) {
            return false
        }
    }

    fun canRemoveStackUpgrade(originalMultiplier: Int): Boolean =
        canReplaceStackUpgrade(originalMultiplier, 1)

    fun canReplaceStackUpgrade(oldMultiplier: Int, newMultiplier: Int): Boolean {
        val newStackMultiplier = getTotalStackMultiplier() / oldMultiplier * newMultiplier

        for (stack in backpackItemStackHandler.inventory) {
            if (stack.isEmpty || !Config.canStackWithStackUpgrade(stack))
                continue

            if (stack.count > stack.maxStackSize * newStackMultiplier)
                return false
        }

        return canFitBatteryEnergyWithMultiplier(newStackMultiplier)
    }

    fun canAddExponentialStackUpgrade(): Boolean {
        try {
            upgradeItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<StackUpgradeItem>()
                .fold(64) { acc, item -> Math.multiplyExact(acc, item.multiplier()) }

            return true
        } catch (_: ArithmeticException) {
            return false
        }
    }

    fun canRemoveExponentialStackUpgrade(): Boolean {
        val byAddMultiplier = getTotalStackMultiplier(false)

        for (stack in backpackItemStackHandler.inventory) {
            if (stack.isEmpty || !Config.canStackWithStackUpgrade(stack))
                continue

            if (stack.count > stack.maxStackSize * byAddMultiplier)
                return false
        }

        return canFitBatteryEnergyWithMultiplier(byAddMultiplier)
    }

    fun canNestBackpack(): Boolean =
        upgradeItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<InceptionUpgradeItem>().any()

    fun canRemoveInceptionUpgrade(): Boolean =
        !backpackItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<BackpackItem>().any() ||
                upgradeItemStackHandler.inventory.map(ItemStack::getItem).filterIsInstance<InceptionUpgradeItem>()
                    .count() > 1

    fun canPickupItem(stack: ItemStack): Boolean =
        gatherCapabilityUpgrades(Capabilities.IPICKUP_UPGRADE_CAPABILITY)
            .any { it.canPickup(stack) }

    fun feed(entity: EntityPlayer, handler: IItemHandler): Boolean {
        val feedingUpgrades = gatherCapabilityUpgrades(Capabilities.IFEEDING_UPGRADE_CAPABILITY)

        for (upgrade in feedingUpgrades)
            return upgrade.feed(entity, handler)

        return false
    }

    fun canDeposit(slotIndex: Int): Boolean {
        val stack = getStackInSlot(slotIndex)
        return gatherCapabilityUpgrades(Capabilities.IDEPOSIT_UPGRADE_CAPABILITY)
            .any { it.canDeposit(stack) }
    }

    fun canRestock(stack: ItemStack): Boolean =
        gatherCapabilityUpgrades(Capabilities.IRESTOCK_UPGRADE_CAPABILITY)
            .any { it.canRestock(stack) }

    fun canInsert(stack: ItemStack): Boolean {
        val filterUpgrades = gatherCapabilityUpgrades(Capabilities.IFILTER_UPGRADE_CAPABILITY)
            .filter { it.enabled }

        return if (filterUpgrades.isEmpty()) true
        else filterUpgrades.any { it.canInsert(stack) }
    }

    fun ensureCapturedMobLayoutCurrent() {
        if (updatingCapturedMobLayout)
            return

        updatingCapturedMobLayout = true
        try {
            MobCatcherStorage.ensureLayoutCurrent(this)
        } finally {
            updatingCapturedMobLayout = false
        }
    }

    fun isSlotBlockedByMobCatcher(slotIndex: Int): Boolean {
        ensureCapturedMobLayoutCurrent()
        return MobCatcherStorage.isSlotBlocked(this, slotIndex)
    }

    fun onBeforeInsert(stack: ItemStack): ItemStack =
        if (shouldVoid(stack, false, false)) ItemStack.EMPTY else stack

    fun onSlotChanged(slotIndex: Int, fromGui: Boolean = false) {
        val stack = getStackInSlot(slotIndex)
        if (!fromGui || shouldVoidInGui(slotIndex, stack)) {
            slotsToVoid.add(slotIndex)
        }
        if (!fromGui || shouldCompactInGui()) {
            slotsToCompact.add(slotIndex)
        }
    }

    fun onGuiSlotChanged(slotIndex: Int) {
        onSlotChanged(slotIndex, true)
    }

    fun shouldHandleSlotChangeFromGui(): Boolean =
        isGuiInteractionInProgress

    fun insertStack(stack: ItemStack, simulate: Boolean, processInsertUpgrades: Boolean = false): ItemStack {
        val stack = if (processInsertUpgrades) onBeforeInsert(stack) else stack
        if (stack.isEmpty) {
            return ItemStack.EMPTY
        }

        var remaining = backpackItemStackHandler.insertItemToMemorySlots(stack, simulate)
        for (slot in 0 until slots) {
            if (remaining.isEmpty) {
                return ItemStack.EMPTY
            }
            remaining = insertItem(slot, remaining, simulate)
        }
        return if (processInsertUpgrades) onInsertRemainder(remaining) else remaining
    }

    fun onInsertRemainder(remaining: ItemStack): ItemStack =
        if (shouldVoid(remaining, true, hasMatchingStack(remaining))) ItemStack.EMPTY else remaining

    fun extractMatching(filter: ItemStack, amount: Int, simulate: Boolean): ItemStack {
        var remaining = amount
        var extracted = ItemStack.EMPTY

        for (slot in 0 until slots) {
            val stack = getStackInSlot(slot)
            if (stack.isEmpty || !ItemHandlerHelper.canItemStacksStack(stack, filter)) {
                continue
            }

            val slotExtracted = extractItem(slot, minOf(stack.count, remaining), simulate)
            if (extracted.isEmpty) {
                extracted = slotExtracted.copy()
            } else {
                extracted.grow(slotExtracted.count)
            }
            remaining -= slotExtracted.count

            if (remaining <= 0) {
                break
            }
        }

        return extracted
    }

    fun tickUpgrades(player: EntityPlayer?, world: World, x: Double, y: Double, z: Double, pos: BlockPos = BlockPos(x, y, z)) {
        if (world.totalWorldTime % 5L == 0L) {
            if (player != null) {
                gatherCapabilityUpgrades(Capabilities.IREFILL_UPGRADE_CAPABILITY).forEach { it.refill(player, this) }
            } else if (gatherCapabilityUpgrades(Capabilities.IREFILL_UPGRADE_CAPABILITY).isNotEmpty()) {
                val players = world.getEntitiesWithinAABB(EntityPlayer::class.java, AxisAlignedBB(pos).grow(3.0))
                players.forEach { nearbyPlayer ->
                    gatherCapabilityUpgrades(Capabilities.IREFILL_UPGRADE_CAPABILITY).forEach { it.refill(nearbyPlayer, this) }
                }
            }
        }

        if (world.totalWorldTime % 10L == 0L) {
            magnetItems(player, world, pos)
        }

        gatherCapabilityUpgrades(Capabilities.ITANK_UPGRADE_CAPABILITY).forEach { it.tick(this, world) }
        gatherCapabilityUpgrades(Capabilities.IBATTERY_UPGRADE_CAPABILITY).forEach { it.tick(this, world) }
        gatherCapabilityUpgrades(Capabilities.IPUMP_UPGRADE_CAPABILITY).forEach { it.tick(player, this, world, pos) }
        gatherCapabilityUpgrades(Capabilities.IJUKEBOX_UPGRADE_CAPABILITY)
            .forEach { it.tick(world, pos) }

        val compactingUpgrades = gatherCapabilityUpgrades(Capabilities.ICOMPACTING_UPGRADE_CAPABILITY)
        if (compactingUpgrades.isNotEmpty()) {
            if (!compactingSlotsInitialized) {
                slotsToCompact.addAll(0 until slots)
                compactingSlotsInitialized = true
            }
            if (world.totalWorldTime % 5L == 0L && slotsToCompact.isNotEmpty()) {
                compactingUpgrades.forEach { it.compact(this, world) }
                slotsToCompact.clear()
            }
        } else {
            compactingSlotsInitialized = false
            slotsToCompact.clear()
        }

        if (slotsToVoid.isNotEmpty()) {
            for (slot in slotsToVoid.toSet()) {
                val stack = getStackInSlot(slot)
                if (!stack.isEmpty && shouldVoidInGui(slot, stack)) {
                    extractItem(slot, stack.count, false)
                }
            }
            slotsToVoid.clear()
        }
    }

    fun compactChangedSlots(
        world: World,
        upgrade: ICompactingUpgrade,
        shouldCompactThreeByThree: Boolean,
        compactNonUncraftable: Boolean,
        stackFilter: (ItemStack) -> Boolean
    ) {
        val slots = if (slotsToCompact.isEmpty()) (0 until this.slots).toSet() else slotsToCompact.toSet()
        for (slot in slots) {
            compactSlot(world, slot, shouldCompactThreeByThree, compactNonUncraftable, stackFilter)
        }
    }

    private fun magnetItems(player: EntityPlayer?, world: World, pos: BlockPos) {
        val magnetUpgrades = gatherCapabilityUpgrades(Capabilities.IMAGNET_UPGRADE_CAPABILITY)
        if (magnetUpgrades.isEmpty()) {
            return
        }

        val range = magnetUpgrades.map(IMagnetUpgrade::range).max()
        val items = world.getEntitiesWithinAABB(EntityItem::class.java, AxisAlignedBB(pos).grow(range))

        for (entityItem in items) {
            if (entityItem.isDead || (entityItem as EntityItemAccessor).`rsb$getPickupDelay`() == 32767 || magnetUpgrades.none { it.canPickup(entityItem.item, this) }) {
                continue
            }

            val original = entityItem.item.copy()
            val remaining = insertStack(original, false, true)
            if (remaining.count != original.count) {
                entityItem.item = remaining
                player?.world?.playSound(
                    null,
                    player.posX,
                    player.posY,
                    player.posZ,
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS,
                    0.2f,
                    (world.rand.nextFloat() - world.rand.nextFloat()) * 1.4f + 2.0f
                )
                if (remaining.isEmpty) {
                    entityItem.setDead()
                } else {
                    entityItem.setNoPickupDelay()
                }
                player?.onItemPickup(entityItem, original.count - remaining.count)
            }
        }
    }

    private fun compactSlot(
        world: World,
        slot: Int,
        shouldCompactThreeByThree: Boolean,
        compactNonUncraftable: Boolean,
        stackFilter: (ItemStack) -> Boolean
    ) {
        val stack = getStackInSlot(slot)
        if (stack.isEmpty || !stackFilter(stack)) {
            return
        }

        if (shouldCompactThreeByThree && tryCompact(world, stack, 3, 3, compactNonUncraftable)) {
            return
        }
        tryCompact(world, stack, 2, 2, compactNonUncraftable)
    }

    private fun tryCompact(world: World, stack: ItemStack, width: Int, height: Int, compactNonUncraftable: Boolean): Boolean {
        val count = width * height
        val compactingResult = getCompactingResult(world, stack, width, height)
        val result = compactingResult.result
        if (result.isEmpty || (!compactNonUncraftable && !canUncompact(world, result, stack, count))) {
            return false
        }

        var compacted = false
        val stacksToInsert = listOf(result) + compactingResult.remainingItems
        while (extractMatching(stack, count, true).count == count && canInsertAll(stacksToInsert)) {
            extractMatching(stack, count, false)
            stacksToInsert.forEach { insertStack(it.copy(), false) }
            compacted = true
        }
        return compacted
    }

    private fun getCompactingResult(world: World, stack: ItemStack, width: Int, height: Int): CompactingResult {
        val inventory = InventoryCrafting(DummyContainer, width, height)
        for (slot in 0 until width * height) {
            inventory.setInventorySlotContents(slot, ItemHandlerHelper.copyStackWithSize(stack, 1))
        }
        val recipe = CraftingManager.findMatchingRecipe(inventory, world) ?: return CompactingResult.EMPTY
        return CompactingResult(
            recipe.getCraftingResult(inventory),
            recipe.getRemainingItems(inventory).filterNot(ItemStack::isEmpty).map(ItemStack::copy)
        )
    }

    private fun canInsertAll(stacks: List<ItemStack>): Boolean {
        val simulation = ExposedItemStackHandler(slots)
        for (slot in 0 until slots) {
            simulation.setStackInSlot(slot, getStackInSlot(slot).copy())
        }

        for (stack in stacks) {
            if (!insertIntoSimulation(simulation, stack.copy()).isEmpty) {
                return false
            }
        }
        return true
    }

    private fun insertIntoSimulation(simulation: ExposedItemStackHandler, stack: ItemStack): ItemStack {
        var remaining = stack
        for (slot in 0 until simulation.slots) {
            if (isSlotBlockedByMobCatcher(slot)) {
                continue
            }
            val existing = simulation.getStackInSlot(slot)
            if (existing.isEmpty) {
                val moved = minOf(remaining.count, getStackLimit(remaining))
                simulation.setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(remaining, moved))
                remaining = ItemHandlerHelper.copyStackWithSize(remaining, remaining.count - moved)
                if (remaining.isEmpty) {
                    return ItemStack.EMPTY
                }
                continue
            }
            if (!ItemHandlerHelper.canItemStacksStack(existing, remaining)) {
                continue
            }
            val moved = minOf(remaining.count, getStackLimit(existing) - existing.count)
            if (moved > 0) {
                existing.grow(moved)
                remaining = ItemHandlerHelper.copyStackWithSize(remaining, remaining.count - moved)
                if (remaining.isEmpty) {
                    return ItemStack.EMPTY
                }
            }
        }
        return remaining
    }

    private fun canUncompact(world: World, result: ItemStack, input: ItemStack, expectedCount: Int): Boolean {
        val inventory = InventoryCrafting(DummyContainer, 3, 3)
        inventory.setInventorySlotContents(0, ItemHandlerHelper.copyStackWithSize(result, 1))
        val recipe = CraftingManager.findMatchingRecipe(inventory, world) ?: return false
        val uncompactResult = recipe.getCraftingResult(inventory)
        return ItemHandlerHelper.canItemStacksStack(uncompactResult, input) && uncompactResult.count >= expectedCount
    }

    private fun shouldVoid(stack: ItemStack, storageFull: Boolean, hasMatchingStack: Boolean): Boolean {
        if (stack.isEmpty) {
            return false
        }

        return gatherCapabilityUpgrades(Capabilities.IVOID_UPGRADE_CAPABILITY).any {
            when (it) {
                is VoidUpgradeWrapper -> it.shouldVoidOverflow(stack, storageFull, hasMatchingStack)
                is AdvancedVoidUpgradeWrapper -> it.shouldVoidOverflow(stack, storageFull, hasMatchingStack)
                else -> it.shouldVoid(stack)
            }
        }
    }

    private fun shouldVoidInGui(slotIndex: Int, stack: ItemStack): Boolean =
        gatherCapabilityUpgrades(Capabilities.IVOID_UPGRADE_CAPABILITY).any {
            when (it) {
                is VoidUpgradeWrapper -> it.shouldWorkInGui && it.shouldVoidOverflow(stack, false, hasMatchingStack(slotIndex, stack))
                is AdvancedVoidUpgradeWrapper -> it.shouldWorkInGui && it.shouldVoidOverflow(stack, false, hasMatchingStack(slotIndex, stack))
                else -> false
            }
        }

    private fun shouldCompactInGui(): Boolean =
        gatherCapabilityUpgrades(Capabilities.ICOMPACTING_UPGRADE_CAPABILITY).any {
            when (it) {
                is CompactingUpgradeWrapper -> it.enabled && it.shouldWorkInGui
                is AdvancedCompactingUpgradeWrapper -> it.enabled && it.shouldWorkInGui
                else -> false
            }
        }

    private fun hasMatchingStack(stack: ItemStack): Boolean =
        backpackItemStackHandler.inventory.any { !it.isEmpty && ItemHandlerHelper.canItemStacksStack(it, stack) }

    private fun hasMatchingStack(changedSlot: Int, stack: ItemStack): Boolean =
        backpackItemStackHandler.inventory.withIndex()
            .any { (slot, storedStack) -> slot != changedSlot && !storedStack.isEmpty && ItemHandlerHelper.canItemStacksStack(storedStack, stack) }

    fun matchesStorageContents(stack: ItemStack, matcher: (ItemStack, ItemStack) -> Boolean): Boolean =
        (0 until slots).any {
            val storedStack = getStackInSlot(it)
            (!storedStack.isEmpty && matcher(stack, storedStack)) || matchesMemoryStack(it, stack, matcher)
        }

    private fun matchesMemoryStack(slotIndex: Int, stack: ItemStack, matcher: (ItemStack, ItemStack) -> Boolean): Boolean {
        val memoryStack = getMemorizedStack(slotIndex)
        return !memoryStack.isEmpty && if (isMemoryStackRespectNBT(slotIndex)) {
            stack.isItemEqual(memoryStack) && stack.tagCompound == memoryStack.tagCompound
        } else {
            matcher(stack, memoryStack)
        }
    }

    private fun isFull(): Boolean =
        (0 until slots).all {
            val stack = getStackInSlot(it)
            !stack.isEmpty && stack.count >= getSlotLimit(it)
        }

    private data class CompactingResult(val result: ItemStack, val remainingItems: List<ItemStack>) {
        companion object {
            val EMPTY = CompactingResult(ItemStack.EMPTY, emptyList())
        }
    }

    fun canExtract(slotIndex: Int): Boolean {
        val stack = getStackInSlot(slotIndex)
        val filterUpgrades = gatherCapabilityUpgrades(Capabilities.IFILTER_UPGRADE_CAPABILITY)
            .filter { it.enabled }

        return if (filterUpgrades.isEmpty()) true
        else filterUpgrades.any { it.canInsert(stack) }
    }

    // Setting related

    fun isSlotMemorized(slotIndex: Int): Boolean =
        !backpackItemStackHandler.memorizedSlotStack[slotIndex].isEmpty

    fun getMemorizedStack(slotIndex: Int): ItemStack =
        backpackItemStackHandler.memorizedSlotStack[slotIndex]

    fun setMemoryStack(slotIndex: Int, respectNBT: Boolean) {
        val currentStack = getStackInSlot(slotIndex)

        if (currentStack.isEmpty)
            return

        val copiedStack = currentStack.copy()
        copiedStack.count = 1

        backpackItemStackHandler.memorizedSlotStack[slotIndex] = copiedStack
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex] = respectNBT
    }

    fun unsetMemoryStack(slotIndex: Int) {
        backpackItemStackHandler.memorizedSlotStack[slotIndex] = ItemStack.EMPTY
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex] = false
    }

    fun isMemoryStackRespectNBT(slotIndex: Int): Boolean =
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex]

    fun setMemoryStackRespectNBT(slotIndex: Int, respect: Boolean) {
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex] = respect
    }

    fun isSlotLocked(slotIndex: Int): Boolean =
        backpackItemStackHandler.sortLockedSlots[slotIndex]

    fun setSlotLocked(slotIndex: Int, locked: Boolean) {
        backpackItemStackHandler.sortLockedSlots[slotIndex] = locked
    }

    fun toggleSettingsContext() {
        settingsContext = settingsContext.next()
    }

    fun toggleShiftClickIntoOpenTab() {
        shiftClickIntoOpenTab = !shiftClickIntoOpenTab
    }

    fun toggleKeepTabOpen() {
        keepTabOpen = !keepTabOpen
    }

    fun toggleKeepSearchPhrase() {
        keepSearchPhrase = !keepSearchPhrase
        if (!keepSearchPhrase) {
            searchPhrase = ""
        }
    }

    fun toggleAnotherPlayerCanOpen() {
        if (!Config.allowOpeningOtherPlayerBackpacks) {
            anotherPlayerCanOpen = false
            return
        }
        anotherPlayerCanOpen = !anotherPlayerCanOpen
    }

    fun isItemDisplaySlotSelected(slotIndex: Int): Boolean =
        !Config.itemDisplayDisabled && slotIndex in itemDisplaySlots

    fun selectItemDisplaySlot(slotIndex: Int) {
        if (Config.itemDisplayDisabled || slotIndex !in 0 until backpackInventorySize() || slotIndex in itemDisplaySlots) {
            return
        }
        if (itemDisplaySlots.size + 1 > 1) {
            return
        }
        itemDisplaySlots.add(slotIndex)
    }

    fun unselectItemDisplaySlot(slotIndex: Int) {
        itemDisplaySlots.remove(slotIndex)
        itemDisplayRotations.remove(slotIndex)
    }

    fun getFirstItemDisplaySlot(): Int =
        if (Config.itemDisplayDisabled) -1 else itemDisplaySlots.firstOrNull() ?: -1

    fun getItemDisplaySlots(): Set<Int> =
        if (Config.itemDisplayDisabled) emptySet() else itemDisplaySlots

    fun getItemDisplayRotation(slotIndex: Int): Int =
        itemDisplayRotations[slotIndex] ?: 0

    fun rotateItemDisplaySlot(slotIndex: Int, clockwise: Boolean) {
        if (Config.itemDisplayDisabled || slotIndex !in itemDisplaySlots) {
            return
        }
        itemDisplayRotations[slotIndex] = (getItemDisplayRotation(slotIndex) + if (clockwise) 45 else -45 + 360) % 360
    }

    fun getDisplayItem(): DisplayItem? {
        if (Config.itemDisplayDisabled) {
            return null
        }
        val slotIndex = getFirstItemDisplaySlot()
        if (slotIndex !in 0 until backpackInventorySize()) {
            return null
        }
        val stack = getStackInSlot(slotIndex).takeIf { !it.isEmpty }
            ?: getMemorizedStack(slotIndex).takeIf { !it.isEmpty }
            ?: return null
        return DisplayItem(ItemHandlerHelper.copyStackWithSize(stack, 1), getItemDisplayRotation(slotIndex), itemDisplaySide)
    }

    data class DisplayItem(val stack: ItemStack, val rotation: Int, val side: DisplaySide)

    // This is only meant to used for bogosorter as RSB already implemented a sorting mechanism
    fun getSortableSlotIndexes(): List<Int> =
        (0..<backpackInventorySize()).filter {
            !isSlotBlockedByMobCatcher(it) &&
                    !backpackItemStackHandler.sortLockedSlots[it] &&
                    backpackItemStackHandler.memorizedSlotStack[it].isEmpty
        }

    fun getDisplayName(): ITextComponent =
        if (customName != null) TextComponentString(customName!!)
        else TextComponentTranslation("container.backpack".asTranslationKey())

    fun <T> gatherCapabilityUpgrades(capability: Capability<T>): List<T> =
        upgradeItemStackHandler.inventory
            .mapNotNull { it.getCapability(capability, null) }

    fun tankUpgradeSlots(): List<Int> =
        upgradeItemStackHandler.inventory.mapIndexedNotNull { slot, upgrade ->
            if (upgrade.getCapability(Capabilities.TANK_UPGRADE_CAPABILITY, null) != null) slot else null
        }

    fun hasTankUpgrade(): Boolean =
        tankUpgradeSlots().isNotEmpty()

    fun batteryUpgradeSlots(): List<Int> =
        upgradeItemStackHandler.inventory.mapIndexedNotNull { slot, upgrade ->
            if (upgrade.getCapability(Capabilities.BATTERY_UPGRADE_CAPABILITY, null) != null) slot else null
        }

    fun hasBatteryUpgrade(): Boolean =
        batteryUpgradeSlots().isNotEmpty()

    fun canAddBatteryUpgrade(): Boolean =
        !hasBatteryUpgrade()

    fun hasMobCatcherUpgrade(): Boolean =
        upgradeItemStackHandler.inventory.any { it.getCapability(Capabilities.MOB_CATCHER_UPGRADE_CAPABILITY, null) != null }

    fun canAddMobCatcherUpgrade(): Boolean =
        !hasMobCatcherUpgrade()

    fun canFitBatteryEnergyWithMultiplier(stackMultiplier: Int): Boolean =
        gatherCapabilityUpgrades(Capabilities.IBATTERY_UPGRADE_CAPABILITY)
            .all { it.energyStored <= it.getMaxEnergyStored(this, stackMultiplier) }

    fun tankRenderSides(): Pair<Boolean, Boolean> {
        val tankSlots = tankUpgradeSlots()
        return (tankSlots.size > 1) to tankSlots.isNotEmpty()
    }

    private object DummyContainer : Container() {
        override fun canInteractWith(playerIn: EntityPlayer): Boolean = false
    }

    override fun getSlots(): Int =
        backpackItemStackHandler.slots

    override fun getStackInSlot(index: Int): ItemStack =
        backpackItemStackHandler.getStackInSlot(index)

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack =
        if (isSlotBlockedByMobCatcher(slot)) stack else backpackItemStackHandler.insertItem(slot, stack, simulate)

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack =
        if (isSlotBlockedByMobCatcher(slot)) ItemStack.EMPTY else backpackItemStackHandler.extractItem(slot, amount, simulate)

    override fun getSlotLimit(slot: Int): Int =
        if (isSlotBlockedByMobCatcher(slot)) 0 else backpackItemStackHandler.getSlotLimit(slot)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.BACKPACK_CAPABILITY ||
                capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
                capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && Config.itemFluidHandlerEnabled && hasTankUpgrade() ||
                capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY &&
                Config.itemFluidHandlerEnabled && hasTankUpgrade() && !containerStack.isEmpty ||
                capability == CapabilityEnergy.ENERGY && hasBatteryUpgrade()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        when {
            capability == Capabilities.BACKPACK_CAPABILITY -> this as T
            capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> this as T
            capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY &&
                    Config.itemFluidHandlerEnabled && hasTankUpgrade() -> BackpackFluidHandler(this) as T
            capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY &&
                    Config.itemFluidHandlerEnabled && hasTankUpgrade() && !containerStack.isEmpty ->
                BackpackFluidItemHandler(containerStack, this) as T
            capability == CapabilityEnergy.ENERGY && hasBatteryUpgrade() -> BackpackEnergyStorage(this) as T
            else -> null
        }

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        val backpackNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(backpackNbt, backpackItemStackHandler.inventory)
        nbt.setTag(BACKPACK_INVENTORY_TAG, backpackNbt)
        val upgradesNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(upgradesNbt, upgradeItemStackHandler.inventory)
        nbt.setTag(UPGRADE_SLOTS_TAG, upgradesNbt)
        nbt.setInteger(BACKPACK_INVENTORY_SIZE_TAG, backpackInventorySize())
        nbt.setInteger(UPGRADE_SLOTS_SIZE_TAG, upgradeSlotsSize())

        nbt.setInteger(MAIN_COLOR_TAG, mainColor)
        nbt.setInteger(ACCENT_COLOR_TAG, accentColor)

        customName?.let { nbt.setString(CUSTOM_NAME_TAG, it) }

        // Settings
        val memoryNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(memoryNbt, backpackItemStackHandler.memorizedSlotStack)
        nbt.setTag(MEMORY_STACK_ITEMS_TAG, memoryNbt)
        nbt.setByteArray(
            MEMORY_STACK_RESPECT_NBT_TAG,
            backpackItemStackHandler.memorizedSlotRespectNbtList.map { if (it) 1.toByte() else 0 }.toByteArray()
        )
        nbt.setByte(SORT_TYPE_TAG, sortType.ordinal.toByte())

        nbt.setByteArray(
            LOCKED_SLOTS_TAG,
            backpackItemStackHandler.sortLockedSlots.map { if (it) 1 else 0 }.map(Int::toByte).toByteArray()
        )

        val mainSettingsNbt = NBTTagCompound()
        mainSettingsNbt.setByte(MAIN_SETTINGS_CONTEXT_TAG, settingsContext.ordinal.toByte())
        mainSettingsNbt.setBoolean(MAIN_SETTINGS_SHIFT_CLICK_INTO_OPEN_TAB_TAG, shiftClickIntoOpenTab)
        mainSettingsNbt.setBoolean(MAIN_SETTINGS_KEEP_TAB_OPEN_TAG, keepTabOpen)
        mainSettingsNbt.setBoolean(MAIN_SETTINGS_KEEP_SEARCH_PHRASE_TAG, keepSearchPhrase)
        if (keepSearchPhrase && searchPhrase.isNotEmpty()) {
            mainSettingsNbt.setString(MAIN_SETTINGS_SEARCH_PHRASE_TAG, searchPhrase)
        }
        mainSettingsNbt.setBoolean(MAIN_SETTINGS_ANOTHER_PLAYER_CAN_OPEN_TAG, anotherPlayerCanOpen)
        nbt.setTag(MAIN_SETTINGS_TAG, mainSettingsNbt)

        val itemDisplayNbt = NBTTagCompound()
        itemDisplayNbt.setIntArray(ITEM_DISPLAY_SLOTS_TAG, itemDisplaySlots.toIntArray())
        val rotationsNbt = NBTTagCompound()
        itemDisplayRotations.forEach { (slot, rotation) -> rotationsNbt.setInteger(slot.toString(), rotation) }
        itemDisplayNbt.setTag(ITEM_DISPLAY_ROTATIONS_TAG, rotationsNbt)
        itemDisplayNbt.setByte(ITEM_DISPLAY_COLOR_TAG, itemDisplayColor.ordinal.toByte())
        itemDisplayNbt.setString(ITEM_DISPLAY_SIDE_TAG, itemDisplaySide.serializedName)
        nbt.setTag(ITEM_DISPLAY_SETTINGS_TAG, itemDisplayNbt)
        ensureCapturedMobLayoutCurrent()
        nbt.setTag(MobCatcherStorage.CAPTURED_MOBS_TAG, MobCatcherStorage.serialize(capturedMobs))
        nbt.setInteger(MobCatcherStorage.CAPTURED_MOBS_COLUMNS_TAG, capturedMobsColumns)

        nbt.setUniqueId(UUID_TAG, uuid)
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        if (nbt.hasKey(BACKPACK_INVENTORY_SIZE_TAG))
            backpackInventorySize = { nbt.getInteger(BACKPACK_INVENTORY_SIZE_TAG) }
        if (nbt.hasKey(UPGRADE_SLOTS_SIZE_TAG))
            upgradeSlotsSize = { nbt.getInteger(UPGRADE_SLOTS_SIZE_TAG) }

        uuid = nbt.getUniqueId(UUID_TAG)!!

        backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), this)
        upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize(), this)

        mainColor = nbt.getInteger(MAIN_COLOR_TAG)
        accentColor = nbt.getInteger(ACCENT_COLOR_TAG)

        if (nbt.hasKey(CUSTOM_NAME_TAG))
            customName = nbt.getString(CUSTOM_NAME_TAG)

        if (nbt.hasKey(BACKPACK_INVENTORY_TAG))
            BackpackItemStackHelper.loadAllItemsExtended(
                nbt.getCompoundTag(BACKPACK_INVENTORY_TAG),
                backpackItemStackHandler.inventory
            )

        if (nbt.hasKey(UPGRADE_SLOTS_TAG))
            BackpackItemStackHelper.loadAllItemsExtended(
                nbt.getCompoundTag(UPGRADE_SLOTS_TAG),
                upgradeItemStackHandler.inventory
            )

        // Settings
        BackpackItemStackHelper.loadAllItemsExtended(
            nbt.getCompoundTag(MEMORY_STACK_ITEMS_TAG),
            backpackItemStackHandler.memorizedSlotStack
        )

        nbt.getByteArray(MEMORY_STACK_RESPECT_NBT_TAG).forEachIndexed { index, b ->
            setMemoryStackRespectNBT(index, b.toInt() != 0)
        }

        nbt.getByteArray(LOCKED_SLOTS_TAG).forEachIndexed { index, b ->
            setSlotLocked(index, b.toInt() != 0)
        }

        if (nbt.hasKey(MAIN_SETTINGS_TAG)) {
            val mainSettingsNbt = nbt.getCompoundTag(MAIN_SETTINGS_TAG)
            settingsContext = SettingsContext.entries.getOrElse(mainSettingsNbt.getByte(MAIN_SETTINGS_CONTEXT_TAG).toInt()) {
                SettingsContext.PLAYER
            }
            shiftClickIntoOpenTab = mainSettingsNbt.getBoolean(MAIN_SETTINGS_SHIFT_CLICK_INTO_OPEN_TAB_TAG)
            keepTabOpen = if (mainSettingsNbt.hasKey(MAIN_SETTINGS_KEEP_TAB_OPEN_TAG))
                mainSettingsNbt.getBoolean(MAIN_SETTINGS_KEEP_TAB_OPEN_TAG)
            else true
            keepSearchPhrase = mainSettingsNbt.getBoolean(MAIN_SETTINGS_KEEP_SEARCH_PHRASE_TAG)
            searchPhrase =
                if (keepSearchPhrase && mainSettingsNbt.hasKey(MAIN_SETTINGS_SEARCH_PHRASE_TAG))
                    mainSettingsNbt.getString(MAIN_SETTINGS_SEARCH_PHRASE_TAG)
                else ""
            anotherPlayerCanOpen = Config.allowOpeningOtherPlayerBackpacks &&
                    (!mainSettingsNbt.hasKey(MAIN_SETTINGS_ANOTHER_PLAYER_CAN_OPEN_TAG) ||
                            mainSettingsNbt.getBoolean(MAIN_SETTINGS_ANOTHER_PLAYER_CAN_OPEN_TAG))
        }

        itemDisplaySlots.clear()
        itemDisplayRotations.clear()
        if (nbt.hasKey(ITEM_DISPLAY_SETTINGS_TAG)) {
            val itemDisplayNbt = nbt.getCompoundTag(ITEM_DISPLAY_SETTINGS_TAG)
            itemDisplayNbt.getIntArray(ITEM_DISPLAY_SLOTS_TAG)
                .filter { it in 0 until backpackInventorySize() }
                .forEach(itemDisplaySlots::add)
            val rotationsNbt = itemDisplayNbt.getCompoundTag(ITEM_DISPLAY_ROTATIONS_TAG)
            rotationsNbt.keySet.forEach { key ->
                key.toIntOrNull()
                    ?.takeIf { it in itemDisplaySlots }
                    ?.let { itemDisplayRotations[it] = rotationsNbt.getInteger(key) }
            }
            itemDisplayColor = EnumDyeColor.entries.getOrElse(itemDisplayNbt.getByte(ITEM_DISPLAY_COLOR_TAG).toInt()) {
                EnumDyeColor.RED
            }
            itemDisplaySide = DisplaySide.fromName(itemDisplayNbt.getString(ITEM_DISPLAY_SIDE_TAG))
        }

        capturedMobs.clear()
        capturedMobs.addAll(MobCatcherStorage.deserialize(nbt.getTagList(MobCatcherStorage.CAPTURED_MOBS_TAG, 10)))
        capturedMobsColumns = if (nbt.hasKey(MobCatcherStorage.CAPTURED_MOBS_COLUMNS_TAG)) {
            nbt.getInteger(MobCatcherStorage.CAPTURED_MOBS_COLUMNS_TAG)
        } else {
            MobCatcherStorage.getColumns(this)
        }

        sortType = SortType.entries[nbt.getByte(SORT_TYPE_TAG).toInt()]
    }
}
