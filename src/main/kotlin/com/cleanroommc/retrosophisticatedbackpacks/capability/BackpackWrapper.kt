package com.cleanroommc.retrosophisticatedbackpacks.capability

import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.UpgradeItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.ExponentialStackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.BackpackItemStackHelper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import java.util.*

class BackpackWrapper(
    var backpackInventorySize: () -> Int = { 27 },
    var upgradeSlotsSize: () -> Int = { 1 },
    var uuid: UUID = UUID.randomUUID(),
) : IItemHandler, ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "BackpackInventory"
        private const val UPGRADE_SLOTS_TAG = "UpgradeSlots"
        private const val BACKPACK_INVENTORY_SIZE_TAG = "BackpackInventorySize"
        private const val UPGRADE_SLOTS_SIZE_TAG = "UpgradeSlotsSize"

        private const val MEMORY_STACK_ITEMS_TAG = "MemoryItems"
        private const val SORT_TYPE_TAG = "SortType"
        private const val LOCKED_SLOTS_TAG = "LockedSlots"

        private const val UUID_TAG = "UUID"

        const val DEFAULT_MAIN_COLOR: Int = -0x339ec6
        const val DEFAULT_ACCENT_COLOR: Int = -0x9dd1e6
    }

    var isCached: Boolean = false
    var backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), this)
    var upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize())
    var sortType: SortType = SortType.BY_NAME

    var mainColor = DEFAULT_MAIN_COLOR
    var accentColor = DEFAULT_ACCENT_COLOR

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
            if (stack.isEmpty)
                continue

            if (stack.count > stack.maxStackSize * newStackMultiplier)
                return false
        }

        return true
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
            if (stack.isEmpty)
                continue

            if (stack.count > stack.maxStackSize * byAddMultiplier)
                return false
        }

        return true
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

    fun getFeedingStack(foodLevel: Int, health: Float, maxHealth: Float): ItemStack {
        val feedingUpgrades = gatherCapabilityUpgrades(Capabilities.IFEEDING_UPGRADE_CAPABILITY)

        for (upgrade in feedingUpgrades) {
            val feedingStack = upgrade.getFeedingStack(this, foodLevel, health, maxHealth)

            if (!feedingStack.isEmpty)
                return feedingStack
        }

        return ItemStack.EMPTY
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

    fun setMemoryStack(slotIndex: Int) {
        val currentStack = getStackInSlot(slotIndex)

        if (currentStack.isEmpty)
            return

        val copiedStack = currentStack.copy()
        copiedStack.count = 1

        backpackItemStackHandler.memorizedSlotStack[slotIndex] = copiedStack
    }

    fun unsetMemoryStack(slotIndex: Int) {
        backpackItemStackHandler.memorizedSlotStack[slotIndex] = ItemStack.EMPTY
    }

    fun isSlotLocked(slotIndex: Int): Boolean =
        backpackItemStackHandler.sortLockedSlots[slotIndex]

    fun setSlotLocked(slotIndex: Int, locked: Boolean) {
        backpackItemStackHandler.sortLockedSlots[slotIndex] = locked
    }

    // Overrides

    fun getDisplayName(): ITextComponent =
        TextComponentTranslation("container.backpack".asTranslationKey())

    private fun <T> gatherCapabilityUpgrades(capability: Capability<T>): List<T> =
        upgradeItemStackHandler.inventory
            .mapNotNull { it.getCapability(capability, null) }

    override fun getSlots(): Int =
        backpackItemStackHandler.slots

    override fun getStackInSlot(index: Int): ItemStack =
        backpackItemStackHandler.getStackInSlot(index)

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack =
        backpackItemStackHandler.insertItem(slot, stack, simulate)

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack =
        backpackItemStackHandler.extractItem(slot, amount, simulate)

    override fun getSlotLimit(slot: Int): Int =
        backpackItemStackHandler.getSlotLimit(slot)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability == Capabilities.BACKPACK_CAPABILITY ||
                capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY

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

        // Settings
        val memoryNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(memoryNbt, backpackItemStackHandler.memorizedSlotStack)
        nbt.setTag(MEMORY_STACK_ITEMS_TAG, memoryNbt)
        nbt.setByte(SORT_TYPE_TAG, sortType.ordinal.toByte())

        nbt.setByteArray(
            LOCKED_SLOTS_TAG,
            backpackItemStackHandler.sortLockedSlots.map { if (it) 1 else 0 }.map(Int::toByte).toByteArray()
        )

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
        upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize())

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

        nbt.getByteArray(LOCKED_SLOTS_TAG).forEachIndexed { index, b ->
            setSlotLocked(index, b.toInt() != 0)
        }

        sortType = SortType.entries[nbt.getByte(SORT_TYPE_TAG).toInt()]
    }
}